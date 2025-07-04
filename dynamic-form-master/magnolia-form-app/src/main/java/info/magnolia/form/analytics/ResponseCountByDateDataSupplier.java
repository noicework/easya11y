/**
 * This file Copyright (c) 2021 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.form.analytics;

import info.magnolia.analytics.AnalyticsModule;
import info.magnolia.analytics.datasource.AbstractExternalDataSupplier;
import info.magnolia.form.service.ResponseService;
import info.magnolia.ui.jdbc.bean.filter.ComplexCriterion;
import info.magnolia.ui.jdbc.bean.filter.Criterion;
import info.magnolia.ui.jdbc.bean.filter.LogicalOperator;
import info.magnolia.ui.jdbc.bean.filter.Operator;
import info.magnolia.ui.jdbc.bean.filter.SimpleCriterion;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Supplies data for showing number of form responses per date (day).
 */
public class ResponseCountByDateDataSupplier extends AbstractExternalDataSupplier {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseCountByDateDataSupplier.class);
    private final ResponseService responseService;
    private final ObjectMapper mapper;

    @Inject
    public ResponseCountByDateDataSupplier(AnalyticsModule analyticsModule,
                                           ResponseService responseService) {
        super(analyticsModule);
        this.responseService = responseService;
        this.mapper = new ObjectMapper();
    }

    @Override
    public String getType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void setDimensionParameter(String s) {
    }

    @Override
    public void setMetricParameter(String s) {
    }

    @Override
    public Optional<JsonNode> fetch() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.getFromDateFormat());
        List<ResponseCount> result = this.responseService.list(buildCriteria())
                .stream()
                .collect(Collectors.groupingBy(
                        d -> d.getCreated().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map( e -> new ResponseCount(e.getKey().format(formatter), e.getValue()))
                .collect(Collectors.toList());

        JsonNode node = mapper.valueToTree(result);
        return Optional.of(node);
    }

    private Criterion buildCriteria() {

        SimpleDateFormat dateFormat = new SimpleDateFormat(this.getToDateFormat());
        Map<String,String> filterParameters = this.getFilterParameters();

        if (filterParameters.size() == 0) {
            return null;
        }

        List<Criterion> filters = new ArrayList<>();

        for (Map.Entry<String, String> f : filterParameters.entrySet()) {
            try {
                if (f.getKey().equals("startDate")) {

                    Date dt = dateFormat.parse(f.getValue());

                    filters.add(SimpleCriterion.builder()
                            .fieldName("created")
                            .parameterKey("created")
                            .operator(Operator.GREATER_OR_EQUAL)
                            .values(Arrays.asList(dt))
                            .build());

                } else if (f.getKey().equals("endDate")) {

                    Date dt = dateFormat.parse(f.getValue());
                    Calendar c = Calendar.getInstance();
                    c.setTime(dt);
                    c.add(Calendar.DATE, 1);
                    dt = c.getTime();

                    filters.add(SimpleCriterion.builder()
                            .fieldName("created")
                            .parameterKey("created")
                            .operator(Operator.LOWER)
                            .values(Arrays.asList(dt))
                            .build());
                } else if (f.getKey().equals("id")) {

                    filters.add(SimpleCriterion.builder()
                            .fieldName("form_id")
                            .parameterKey("form_id")
                            .operator(Operator.EQUAL)
                            .values(Arrays.asList(this.responseService.parseStringToId(f.getValue())))
                            .build());
                }
            } catch (Exception e) {
                LOG.error("error parsing graph filters" , e);
            }
        }


        Criterion result = ComplexCriterion.builder()
                .logicalOperator(LogicalOperator.AND)
                .criteria(filters)
                .build();

        return result;
    }

    @Data
    @AllArgsConstructor
    private class ResponseCount {
        private String date;
        private Long count;
    }
}
