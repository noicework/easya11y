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
import info.magnolia.form.domain.AnswerOption;
import info.magnolia.form.domain.AnswerOptionLocalized;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.domain.Question;
import info.magnolia.form.domain.query.QAnswerOption;
import info.magnolia.form.service.AnswerOptionLocalizedService;
import info.magnolia.form.service.AnswerOptionService;
import info.magnolia.form.service.DropLocation;
import info.magnolia.form.service.IdNonExistsException;
import info.magnolia.form.service.QuestionService;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import io.ebean.ExpressionList;
import io.ebean.Query;
import io.ebean.Transaction;

/**
 * Answer option service implementation class.
 */
public class AnswerOptionServiceImpl extends AbstractBaseServiceImpl<AnswerOption> implements AnswerOptionService, IdNonExistsException<AnswerOption> {

    private final AnswerOptionLocalizedService answerOptionLocalizedService;
    private final QuestionService questionService;

    @Inject
    public AnswerOptionServiceImpl(final Provider<FormCoreModule> formModuleProvider,
                                   final AnswerOptionLocalizedService answerOptionLocalizedService,
                                   final QuestionService questionService) {
        super(formModuleProvider);
        this.answerOptionLocalizedService = answerOptionLocalizedService;
        this.questionService = questionService;
    }

    @Override
    public Class<AnswerOption> getModelClass() {
        return AnswerOption.class;
    }

    @Override
    public String getEntityName() {
        return AnswerOption.class.getSimpleName();
    }

    @Override
    public AnswerOption prepareForDbUpdate(AnswerOption source, AnswerOption destination) {

        if (source == null || destination == null) {
            return null;
        }

        if (source.getId() != null) {
            destination.setId(source.getId());
        }
        destination.setTitle(source.getTitle());
        destination.setLabel(source.getLabel());
        destination.setValue(source.getValue());
        destination.setImage(source.getImage());
        destination.setFreeText(source.getFreeText());
        destination.setFreeTextLabel(source.getFreeTextLabel());
        destination.setOrderIndex(source.getOrderIndex());
        destination.setContent(source.getContent());

        if (source.getQuestion() != null && source.getQuestion().getId() != null) {
            // set reference
            destination.setQuestion(this.getDb().reference(Question.class, source.getQuestion().getId()));
        } else {
            destination.setQuestion(null);
        }

        // remap localizations
        if (source.getLocalizations().size() == 0 && source.getLocalisations().size() > 0) {
            source.setLocalizations(source.getLocalisations().stream()
                    .collect(Collectors.toMap(t->t.getLocale() , t->t)));
        }

        destination.getLocalizations().entrySet().forEach(r -> {
            if (!source.getLocalizations().containsKey(r.getKey())) {
                getDb().delete(r.getValue());
            }
        });

        destination.setLocalizations(source.getLocalizations().entrySet().stream()
                .map(i -> {

                    AnswerOptionLocalized item;

                    if (i.getValue().getId() == null) {
                        item = new AnswerOptionLocalized();
                    } else {
                        item = getDb().find(AnswerOptionLocalized.class, i.getValue().getId());
                    }

                    i.getValue().setLocale(i.getKey());
                    i.getValue().setParent(destination);

                    item = answerOptionLocalizedService.prepareForDbUpdate(i.getValue(), item);

                    // bug with ebean and Map collection
                    // changes are not propagated from parent
                    // we have to save/update localizations manually
                    return destination.getId() == null ?
                            item : answerOptionLocalizedService.upsert(item);
                })
                .collect(Collectors.toMap(t->t.getLocale() , t->t)));

        return destination;
    }

    @Override
    public Optional getParent(UUID id) {

        Optional<AnswerOption> entity = getById(id);
        if (entity.isPresent()) {
           Question parent = entity.get().getQuestion();
           return Optional.ofNullable(parent);
        }

        return Optional.empty();
    }

    @Override
    public Optional<AnswerOption> getAnswerOptionByQuestionIdAndValue(UUID questionId, String value) {

        Query<AnswerOption> query = getDb().find(AnswerOption.class).setDisableLazyLoading(true);
        // define related entities that should be eager loaded
        query = eagerFetchFullGraph(query);
        ExpressionList<AnswerOption> expression = query.where();
        expression = expression.eq(QAnswerOption.alias().question.id.toString(), questionId);
        expression = expression.eq(QAnswerOption.alias().value.toString(), value);

        return  expression
                .query()
                .findOneOrEmpty();
    }

    @Override
    public Query<AnswerOption> eagerFetchFlatGraph(Query<AnswerOption> query) {
        query = query.fetch(QAnswerOption.alias().localisations.toString());
        return query;
    }

    @Override
    public Query<AnswerOption> eagerFetchFullGraph(Query<AnswerOption> query) {
        query = query.fetch(QAnswerOption.alias().localizations.toString());
        query = query.fetch(QAnswerOption.alias().localisations.toString());
        return query;
    }

    @Override
    public void prepareForDbImport(AnswerOption destination) {

        // remap localizations
        destination.setLocalizations(destination.getLocalisations().stream()
                .collect(Collectors.toMap(t->t.getLocale() , t->t)));
    }

    @Override
    public AnswerOption save(AnswerOption entityToInsert) {

        try (Transaction transaction = getDb().beginTransaction()) {
            Question question = questionService.getById(entityToInsert.getQuestion().getId()).get();
            entityToInsert.setOrderIndex(question.getAnswerOptions().size() + 1);
            AnswerOption savedEntity = super.save(entityToInsert);
            transaction.commit();
            return savedEntity;
        }
    }

    @Override
    public void delete(UUID id) {

        try (Transaction transaction = getDb().beginTransaction()) {
            UUID entityParentId = getById(id).get().getQuestion().getId();
            super.delete(id);
            Question question = questionService.getById(entityParentId).get();
            List<AnswerOption> answers = question.getAnswerOptions()
                    .stream()
                    .sorted(Comparator.comparingInt(AnswerOption::getOrderIndex))
                    .collect(Collectors.toList());
            for (int i = 0; i < answers.size(); i++) {
                AnswerOption entityToOrder = getById(answers.get(i).getId()).get();
                entityToOrder.setOrderIndex(i + 1);
                update(entityToOrder);
            }
            transaction.commit();
        }
    }

    @Override
    public void move(AnswerOption entity, BaseModel target, DropLocation dropLocation) {

        Question targetParent;

        try (Transaction transaction = getDb().beginTransaction()) {

            if (DropLocation.ABOVE.equals(dropLocation)) {
                if (!(target instanceof AnswerOption)) {
                    throw new UnsupportedOperationException("Answer Option can not be moved above " + target.getClass().getSimpleName());
                }
                targetParent = this.questionService.getById(((AnswerOption) target).getQuestion().getId()).get();
            } else if (DropLocation.BELOW.equals(dropLocation)) {
                if (!(target instanceof AnswerOption)) {
                    throw new UnsupportedOperationException("Answer Option can not be moved below " + target.getClass().getSimpleName());
                }
                targetParent = this.questionService.getById(((AnswerOption) target).getQuestion().getId()).get();
            } else if (DropLocation.ON_TOP.equals(dropLocation)) {
                if (!(target instanceof Question)) {
                    throw new UnsupportedOperationException("Answer Option can not be moved on top of " + target.getClass().getSimpleName());
                }
                targetParent = this.questionService.getById((target).getId()).get();
            } else {
                throw new UnsupportedOperationException("Invalid drop location");
            }

            AnswerOption entityToMove = getById(entity.getId()).get();
            Question entityParent = this.questionService.getById(entity.getQuestion().getId()).get();
            int index = 0;

            Question finalEntityParent = entityParent;
            List<AnswerOption> targetItems = targetParent.getAnswerOptions()
                    .stream()
                    .filter(i-> !finalEntityParent.getId().equals(targetParent.getId()) || (finalEntityParent.getId().equals(targetParent.getId()) && !i.getId().equals(entity.getId())) )
                    .sorted(Comparator.comparingInt(AnswerOption::getOrderIndex))
                    .collect(Collectors.toList());

            if (DropLocation.ABOVE.equals(dropLocation)) {
                AnswerOption targetItem = targetItems.stream().filter(s->target.getId().equals(s.getId())).findFirst().get();
                index = targetItems.indexOf(targetItem);
            } else if (DropLocation.BELOW.equals(dropLocation)) {
                AnswerOption targetItem = targetItems.stream().filter(s->target.getId().equals(s.getId())).findFirst().get();
                index = targetItems.indexOf(targetItem) + 1;
            } else if (DropLocation.ON_TOP.equals(dropLocation)) {
                index = targetItems.size();
            }


            for (int i = 0; i < index; i ++) {
                AnswerOption tmp = getById(targetItems.get(i).getId()).get();
                tmp.setOrderIndex(i + 1);
                update(tmp);
            }

            for (int i = index; i < targetItems.size(); i ++) {
                AnswerOption tmp = getById(targetItems.get(i).getId()).get();
                tmp.setOrderIndex(i + 2);
                update(tmp);
            }

            entityToMove.setOrderIndex(index + 1);
            entityToMove.setQuestion(targetParent);
            update(entityToMove);

            if ((!entityParent.getId().equals(targetParent.getId()))) {

                entityParent = this.questionService.getById(entity.getQuestion().getId()).get();

                targetItems = entityParent.getAnswerOptions()
                        .stream()
                        .filter(i -> !i.getId().equals(entity.getId()))
                        .sorted(Comparator.comparingInt(AnswerOption::getOrderIndex))
                        .collect(Collectors.toList());

                for(int i = 0; i < targetItems.size(); i ++) {
                    AnswerOption tmp = getById(targetItems.get(i).getId()).get();
                    tmp.setOrderIndex(i + 1);
                    update(tmp);
                }
            }

            transaction.commit();
        }
    }

    @Override
    public boolean logExceptionIfIdExists(AnswerOption entity) {
        Optional<AnswerOption> res = getById(entity.getId());
        if (res.isPresent()) {
            this.logIdExistsException(entity);
            return true;
        }
        return false;
    }
}
