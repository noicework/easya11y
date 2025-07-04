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
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.domain.Form;
import info.magnolia.form.domain.Question;
import info.magnolia.form.domain.QuestionLocalized;
import info.magnolia.form.domain.Section;
import info.magnolia.form.domain.query.QQuestion;
import info.magnolia.form.service.AnswerOptionService;
import info.magnolia.form.service.DropLocation;
import info.magnolia.form.service.FormService;
import info.magnolia.form.service.IdNonExistsException;
import info.magnolia.form.service.QuestionLocalizedService;
import info.magnolia.form.service.QuestionService;
import info.magnolia.form.service.SectionService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import io.ebean.Query;
import io.ebean.Transaction;

/**
 * Question service implementation class.
 */
public class QuestionServiceImpl extends AbstractBaseServiceImpl<Question> implements QuestionService, IdNonExistsException<Question> {

    private final AnswerOptionService answerOptionService;
    private final QuestionLocalizedService questionLocalizedService;
    private final FormService formService;
    private final SectionService sectionService;

    @Inject
    public QuestionServiceImpl(final Provider<FormCoreModule> formModuleProvider,
                               final AnswerOptionService answerOptionService,
                               final QuestionLocalizedService questionLocalizedService,
                               final FormService formService,
                               final SectionService sectionService) {
        super(formModuleProvider);
        this.answerOptionService = answerOptionService;
        this.questionLocalizedService = questionLocalizedService;
        this.formService = formService;
        this.sectionService = sectionService;
    }

    @Override
    public Class<Question> getModelClass() {
        return Question.class;
    }

    @Override
    public String getEntityName() {
        return Question.class.getSimpleName();
    }

    @Override
    public Question prepareForDbUpdate(Question source, Question destination) {

        if (source == null || destination == null) {
            return null;
        }

        if (source.getId() != null) {
            destination.setId(source.getId());
        }
        destination.setTitle(source.getTitle());
        destination.setQuestion(source.getQuestion());
        destination.setQuestionType(source.getQuestionType());
        destination.setImage(source.getImage());
        destination.setRangeFrom(source.getRangeFrom());
        destination.setRangeFromLabel(source.getRangeFromLabel());
        destination.setRangeFromImage(source.getRangeFromImage());
        destination.setRangeTo(source.getRangeTo());
        destination.setRangeToLabel(source.getRangeToLabel());
        destination.setRangeToImage(source.getRangeToImage());
        destination.setCustomChartId(source.getCustomChartId());
        destination.setOrderIndex(source.getOrderIndex());
        destination.setContent(source.getContent());

        if (source.getForm() != null && source.getForm().getId() != null) {
            // set reference
            destination.setForm(this.getDb().reference(Form.class, source.getForm().getId()));
        } else {
            destination.setForm(null);
        }

        if (source.getSection() != null && source.getSection().getId() != null) {
            // set reference
            destination.setSection(this.getDb().reference(Section.class, source.getSection().getId()));
        } else {
            destination.setSection(null);
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

                    QuestionLocalized item;

                    if (i.getValue().getId() == null) {
                        item = new QuestionLocalized();
                    } else {
                        item = getDb().find(QuestionLocalized.class, i.getValue().getId());
                    }

                    i.getValue().setLocale(i.getKey());
                    i.getValue().setParent(destination);

                    item = questionLocalizedService.prepareForDbUpdate(i.getValue(), item);

                    // bug with ebean and Map collection
                    // changes are not propagated from parent
                    // we have to save/update localizations manually
                    return destination.getId() == null ?
                            item : questionLocalizedService.upsert(item);
                })
                .collect(Collectors.toMap(t->t.getLocale() , t->t)));

        // remap answer options
        destination.getAnswerOptions().forEach(r -> {
            if (!source.getAnswerOptions().stream().map((i -> i.getId())).collect(Collectors.toList()).contains(r.getId())) {
                this.getDb().delete(r);
            }
        });

        destination.setAnswerOptions(source.getAnswerOptions().stream()
                .map(i -> {
                    AnswerOption item;
                    if (i.getId() == null) {
                        item = new AnswerOption();
                    } else {
                        item = answerOptionService.getById(i.getId()).orElseGet(AnswerOption::new);
                    }
                    item = answerOptionService.prepareForDbUpdate(i, item);
                    return item;
                })
                .collect(Collectors.toList()));

        return destination;
    }

    @Override
    public Optional getParent(UUID id) {

        Optional<Question> entity = getById(id);
        if (entity.isPresent()) {
            BaseModel parent = entity.get().getSection();
            if (parent != null) {
                return Optional.of(parent);
            } else {
                parent = entity.get().getForm();
                return Optional.ofNullable(parent);
            }
        }

        return Optional.empty();
    }

    @Override
    public Collection<String> getChildrenEntityNames() {
        return Arrays.asList(AnswerOption.class.getSimpleName());
    }

    @Override
    public boolean hasChildren(UUID id) {
        Optional<Question> entity = getById(id);
        if (entity.isPresent()) {
            return (entity.get().getAnswerOptions().size() > 0);
        }
        return false;
    }

    @Override
    public Query<Question> eagerFetchFlatGraph(Query<Question> query) {
        query = query.fetch(QQuestion.alias().localisations.toString());
        return query;
    }

    @Override
    public Query<Question> eagerFetchFullGraph(Query<Question> query) {
        query = query.fetch(QQuestion.alias().localizations.toString());
        query = query.fetch(QQuestion.alias().localisations.toString());
        query = query.fetch(QQuestion.alias().answerOptions.toString());
        query = query.fetch(QQuestion.alias().answerOptions.localizations.toString());
        query = query.fetch(QQuestion.alias().answerOptions.localisations.toString());
        return query;
    }

    @Override
    public void prepareForDbImport(Question destination) {

        // remap localizations
        destination.setLocalizations(destination.getLocalisations().stream()
                .collect(Collectors.toMap(t->t.getLocale() , t->t)));

        // remap questions
        destination.getAnswerOptions().forEach(r -> {
            answerOptionService.prepareForDbImport(r);
        });
    }

    @Override
    public Question save(Question entityToInsert) {

        try (Transaction transaction = getDb().beginTransaction()) {
            if (entityToInsert.getForm() != null) {
                Form form = formService.getById(entityToInsert.getForm().getId()).get();
                entityToInsert.setOrderIndex(form.getQuestions().size() + 1);
            }
            if (entityToInsert.getSection() != null) {
                Section section = sectionService.getById(entityToInsert.getSection().getId()).get();
                entityToInsert.setOrderIndex(section.getQuestions().size() + 1);
            }
            Question savedEntity = super.save(entityToInsert);
            transaction.commit();
            return savedEntity;
        }
    }

    @Override
    public void delete(UUID id) {

        try (Transaction transaction = getDb().beginTransaction()) {
            UUID entityParentFormId = null;
            UUID entityParentSectionId = null;
            Question entityToDelete = getById(id).get();
            if (entityToDelete.getForm() != null) {
                entityParentFormId = entityToDelete.getForm().getId();
            }
            if (entityToDelete.getSection() != null) {
                entityParentSectionId = entityToDelete.getSection().getId();
            }

            super.delete(id);

            List<Question> questions = Collections.EMPTY_LIST;
            if (entityParentFormId != null) {
                Form form = formService.getById(entityParentFormId).get();
                questions = form.getQuestions()
                        .stream()
                        .sorted(Comparator.comparingInt(Question::getOrderIndex))
                        .collect(Collectors.toList());
            }
            if (entityParentSectionId != null) {
                Section section = sectionService.getById(entityParentSectionId).get();
                questions = section.getQuestions()
                        .stream()
                        .sorted(Comparator.comparingInt(Question::getOrderIndex))
                        .collect(Collectors.toList());
            }
            for (int i = 0; i < questions.size(); i++) {
                Question entityToOrder = getById(questions.get(i).getId()).get();
                entityToOrder.setOrderIndex(i + 1);
                update(entityToOrder);
            }
            transaction.commit();
        }
    }

    @Override
    public void move(Question entity, BaseModel target, DropLocation dropLocation) {

        BaseModel targetParent;

        try (Transaction transaction = getDb().beginTransaction()) {

            if (DropLocation.ABOVE.equals(dropLocation)) {
                if (!(target instanceof Question)) {
                    throw new UnsupportedOperationException("Question can not be moved above " + target.getClass().getSimpleName());
                }
                if (((Question) target).getSection() != null) {
                    targetParent = this.sectionService.getById(((Question) target).getSection().getId()).get();
                } else {
                    targetParent = this.formService.getById(((Question) target).getForm().getId()).get();
                }
            } else if (DropLocation.BELOW.equals(dropLocation)) {
                if (!(target instanceof Question)) {
                    throw new UnsupportedOperationException("Question can not be moved below " + target.getClass().getSimpleName());
                }
                if (((Question) target).getSection() != null) {
                    targetParent = this.sectionService.getById(((Question) target).getSection().getId()).get();
                } else {
                    targetParent = this.formService.getById(((Question) target).getForm().getId()).get();
                }
            } else if (DropLocation.ON_TOP.equals(dropLocation)) {
                if (!(target instanceof Section) && !(target instanceof Form)) {
                    throw new UnsupportedOperationException("Question can not be moved on top of " + target.getClass().getSimpleName());
                }
                if (target instanceof Section) {
                    targetParent = this.sectionService.getById((target).getId()).get();
                } else {
                    targetParent = this.formService.getById((target).getId()).get();
                }
            } else {
                throw new UnsupportedOperationException("Invalid drop location");
            }

            Question entityToMove = getById(entity.getId()).get();
            BaseModel entityParent;
            if (entity.getSection() != null) {
                entityParent = this.sectionService.getById(entity.getSection().getId()).get();
            } else {
                entityParent = this.formService.getById(entity.getForm().getId()).get();
            }
            int index = 0;

            BaseModel finalEntityParent = entityParent;
            List<Question> targetItems = targetParent instanceof Section ? ((Section) targetParent).getQuestions() : ((Form) targetParent).getQuestions()
                    .stream()
                    .filter(i-> !finalEntityParent.getId().equals(targetParent.getId()) || (finalEntityParent.getId().equals(targetParent.getId()) && !i.getId().equals(entity.getId())) )
                    .sorted(Comparator.comparingInt(Question::getOrderIndex))
                    .collect(Collectors.toList());

            if (DropLocation.ABOVE.equals(dropLocation)) {
                Question targetItem = targetItems.stream().filter(s->target.getId().equals(s.getId())).findFirst().get();
                index = targetItems.indexOf(targetItem);
            } else if (DropLocation.BELOW.equals(dropLocation)) {
                Question targetItem = targetItems.stream().filter(s->target.getId().equals(s.getId())).findFirst().get();
                index = targetItems.indexOf(targetItem) + 1;
            } else if (DropLocation.ON_TOP.equals(dropLocation)) {
                index = targetItems.size();
            }


            for (int i = 0; i < index; i ++) {
                Question tmp = getById(targetItems.get(i).getId()).get();
                tmp.setOrderIndex(i + 1);
                update(tmp);
            }

            for (int i = index; i < targetItems.size(); i ++) {
                Question tmp = getById(targetItems.get(i).getId()).get();
                tmp.setOrderIndex(i + 2);
                update(tmp);
            }

            entityToMove.setOrderIndex(index + 1);
            if (targetParent instanceof Section) {
                entityToMove.setSection((Section) targetParent);
                entityToMove.setForm(null);
            } else {
                entityToMove.setSection(null);
                entityToMove.setForm((Form) targetParent);
            }
            update(entityToMove);

            if ((!entityParent.getId().equals(targetParent.getId()))) {

                if (entity.getSection() != null) {
                    entityParent = this.sectionService.getById(entity.getSection().getId()).get();
                } else {
                    entityParent = this.formService.getById(entity.getForm().getId()).get();
                }

                targetItems = entityParent instanceof Section ? ((Section)entityParent).getQuestions() : ((Form)entityParent).getQuestions()
                        .stream()
                        .filter(i -> !i.getId().equals(entity.getId()))
                        .sorted(Comparator.comparingInt(Question::getOrderIndex))
                        .collect(Collectors.toList());

                for(int i = 0; i < targetItems.size(); i ++) {
                    Question tmp = getById(targetItems.get(i).getId()).get();
                    tmp.setOrderIndex(i + 1);
                    update(tmp);
                }
            }

            transaction.commit();
        }
    }

    @Override
    public boolean logExceptionIfIdExists(Question entity) {
        boolean result = false;
        Optional<Question> res = getById(entity.getId());
        if (res.isPresent()) {
            this.logIdExistsException(entity);
            result = true;
        }
        for(AnswerOption q: entity.getAnswerOptions()) {
            if (answerOptionService.logExceptionIfIdExists(q)) {
                result = true;
            }
        }
        return result;
    }
}
