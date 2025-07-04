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
 * Supplies data for showing average form responses per question answer.
 */
public class ResponseAverageByAnswerDataSupplier extends AbstractExternalDataSupplier {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseAverageByAnswerDataSupplier.class);
    private final ResponseItemService responseItemService;
    private final QuestionService questionService;
    private final ObjectMapper mapper;
    private final String QUESTION_ID = "questionId";

    @Inject
    public ResponseAverageByAnswerDataSupplier(AnalyticsModule analyticsModule,
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

        Optional<Question> question = questionService.getById(questionService.parseStringToId(questionIdStr));

        if (!question.isPresent()) {
            return Optional.empty();
        }

        Double average = this.responseItemService.list(buildCriteria(responseItemService.parseStringToId(questionIdStr)))
                .stream()
                .collect(
                        Collectors.averagingDouble(d -> Double.parseDouble(d.getValue()))
                );

        Long count = this.responseItemService.list(buildCriteria(responseItemService.parseStringToId(questionIdStr)))
                .stream()
                .count();

        Double total = this.responseItemService.list(buildCriteria(responseItemService.parseStringToId(questionIdStr)))
                .stream()
                .collect(
                        Collectors.summingDouble(d -> Double.parseDouble(d.getValue()))
                );

        List<ResponseAverage> result = new ArrayList<>();
        ResponseAverage responseCount = new ResponseAverage(
                String.valueOf(average),
                average,
                count,
                total);
        result.add(responseCount);

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
    private class ResponseAverage {
        private String answer;
        private Double average;
        private Long count;
        private Double total;
    }
}
