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
package info.magnolia.form.service.impl;

import info.magnolia.form.FormCoreModule;
import info.magnolia.form.domain.Question;
import info.magnolia.form.domain.Response;
import info.magnolia.form.domain.ResponseItem;
import info.magnolia.form.domain.query.QResponseItem;
import info.magnolia.form.service.IdNonExistsException;
import info.magnolia.form.service.QuestionService;
import info.magnolia.form.service.ResponseItemService;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang.IncompleteArgumentException;

import io.ebean.Query;

/**
 * Response item service implementation class.
 */
public class ResponseItemServiceImpl extends AbstractBaseServiceImpl<ResponseItem> implements ResponseItemService, IdNonExistsException<ResponseItem> {

    private final QuestionService questionService;
    @Inject
    public ResponseItemServiceImpl(final Provider<FormCoreModule> formModuleProvider,
                                   final QuestionService questionService) {
        super(formModuleProvider);
        this.questionService = questionService;
    }

    @Override
    public Class<ResponseItem> getModelClass() {
        return ResponseItem.class;
    }

    @Override
    public String getEntityName() {
        return ResponseItem.class.getSimpleName();
    }

    @Override
    public ResponseItem prepareForDbUpdate(ResponseItem source, ResponseItem destination) {

        if (source == null || destination == null) {
            return null;
        }

        if (source.getResponse() != null) {
            // set reference
            destination.setResponse(this.getDb().reference(Response.class, source.getResponse().getId()));
        } else {
            destination.setResponse(null);
        }

        if (source.getQuestion() != null) {
            // set reference
            destination.setQuestion(this.getDb().reference(Question.class, source.getQuestion().getId()));
        } else {
            throw new IncompleteArgumentException("response item missing valid question relation");
        }

        Question question = questionService.getById(source.getQuestion().getId()).get();
        if (Arrays.asList("single", "multi").contains(question.getQuestionType())) {
            if (question.getAnswerOptions().stream().map(ao -> ao.getValue()).noneMatch(v -> v.equals(source.getValue()))) {
                throw new IncompleteArgumentException("response item value not matching any answer options");
            }
        }

        destination.setValue(source.getValue());
        destination.setFreeTextValue(source.getFreeTextValue());

        if (source.getId() != null) {
            destination.setId(source.getId());
        }

        return destination;
    }

    @Override
    public Optional getParent(UUID id) {

        Optional<ResponseItem> entity = getById(id);
        if (entity.isPresent()) {
            Response parent = entity.get().getResponse();
            return Optional.ofNullable(parent);
        }

        return Optional.empty();
    }

    @Override
    public Query<ResponseItem> eagerFetchFlatGraph(Query<ResponseItem> query) {
        query = query.fetch(QResponseItem.alias().question.toString());
        query = query.fetch(QResponseItem.alias().question.answerOptions.toString());
        return query;
    }

    @Override
    public Query<ResponseItem> eagerFetchFullGraph(Query<ResponseItem> query) {
        query = query.fetch(QResponseItem.alias().question.toString());
        query = query.fetch(QResponseItem.alias().question.answerOptions.toString());
        return query;
    }
}
