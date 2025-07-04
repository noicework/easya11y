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
import info.magnolia.form.domain.AnswerOption;
import info.magnolia.form.domain.Question;
import info.magnolia.form.service.QuestionService;
import info.magnolia.form.service.ResponseItemService;
import info.magnolia.ui.jdbc.bean.filter.ComplexCriterion;
import info.magnolia.ui.jdbc.bean.filter.Criterion;
import info.magnolia.ui.jdbc.bean.filter.LogicalOperator;
import info.magnolia.ui.jdbc.bean.filter.Operator;
import info.magnolia.ui.jdbc.bean.filter.SimpleCriterion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Supplies data for showing number of form responses per question answer.
 */
public class ResponseCountByAnswerDataSupplier extends AbstractExternalDataSupplier {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseCountByAnswerDataSupplier.class);
    private final ResponseItemService responseItemService;
    private final QuestionService questionService;
    private final ObjectMapper mapper;
    private final String QUESTION_ID = "questionId";

    @Inject
    public ResponseCountByAnswerDataSupplier(AnalyticsModule analyticsModule,
                                             ResponseItemService responseItemService,
                                             QuestionService questionService) {
        super(analyticsModule);
        this.responseItemService = responseItemService;
        this.questionService = questionService;
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

        String questionIdStr = this.getParameters().get(QUESTION_ID);
        if (StringUtils.isBlank(questionIdStr)) {
            LOG.error("question id parameter not set for form supplier");
            return Optional.empty();
        }

        try {
            UUID.fromString(questionIdStr);
        } catch (IllegalArgumentException exception){
            LOG.error("question id parameter not valid UUID for form supplier");
            return Optional.empty();
        }

        Optional<Question> question = questionService.getById(questionService.parseStringToId(questionIdStr));

        if (!question.isPresent()) {
            return Optional.empty();
        }

        List<ResponseCount> responseCountList = this.responseItemService.list(buildCriteria(this.responseItemService.parseStringToId(questionIdStr)))
                .stream()
                .collect(Collectors.groupingBy(
                        d -> d.getValue(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map( e -> new ResponseCount(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        List<ResponseCount> result = new ArrayList<>();
        for(AnswerOption ao: question.get().getAnswerOptions()) {

            long count = 0;
            Optional<Long> res = responseCountList.stream().filter(rc->rc.getAnswer().equals(ao.getValue())).map(rc -> rc.getCount()).findFirst();
            if (res.isPresent()) {
                count = res.get();
            }
            ResponseCount responseCount = new ResponseCount(ao.getLabel(), count);
            result.add(responseCount);
        }

        JsonNode node = mapper.valueToTree(result);
        return Optional.of(node);
    }

    private Criterion buildCriteria(UUID questionId) {

        List<Criterion> filters = new ArrayList<>();

        filters.add(SimpleCriterion.builder()
                .fieldName("question_id")
                .parameterKey("question_id")
                .operator(Operator.EQUAL)
                .values(Arrays.asList(questionId))
                .build());

        Criterion result = ComplexCriterion.builder()
                .logicalOperator(LogicalOperator.AND)
                .criteria(filters)
                .build();

        return result;
    }

    @Data
    @AllArgsConstructor
    private class ResponseCount {
        private String answer;
        private Long count;
    }
}
