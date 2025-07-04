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
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.domain.Form;
import info.magnolia.form.domain.FormLocalized;
import info.magnolia.form.domain.Question;
import info.magnolia.form.domain.Section;
import info.magnolia.form.domain.query.QForm;
import info.magnolia.form.service.DropLocation;
import info.magnolia.form.service.FormLocalizedService;
import info.magnolia.form.service.FormService;
import info.magnolia.form.service.IdNonExistsException;
import info.magnolia.form.service.QuestionService;
import info.magnolia.form.service.SectionService;

import java.util.Arrays;
import java.util.Collection;
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
 * Form service implementation class.
 */
public class FormServiceImpl extends AbstractBaseServiceImpl<Form> implements FormService, IdNonExistsException<Form> {

    private final SectionService sectionService;
    private final QuestionService questionService;
    private final FormLocalizedService formLocalizedService;

    @Inject
    public FormServiceImpl(final Provider<FormCoreModule> formModuleProvider,
                           final SectionService sectionService,
                           final QuestionService questionService,
                           final FormLocalizedService formLocalizedService) {
        super(formModuleProvider);
        this.sectionService = sectionService;
        this.questionService = questionService;
        this.formLocalizedService = formLocalizedService;
    }

    @Override
    public Class<Form> getModelClass() {
        return Form.class;
    }

    @Override
    public String getEntityName() {
        return Form.class.getSimpleName();
    }

    @Override
    public Form prepareForDbUpdate(Form source, Form destination) {

        if (source == null || destination == null) {
            return null;
        }

        if (source.getId() != null) {
            destination.setId(source.getId());
        }
        destination.setTitle(source.getTitle());
        destination.setDescription(source.getDescription());
        destination.setImage(source.getImage());
        destination.setRules(source.getRules());
        destination.setAnonymize(source.isAnonymize());
        destination.setOnlyAuthenticated(source.isOnlyAuthenticated());
        destination.setPublicResults(source.isPublicResults());
        destination.setOrderIndex(source.getOrderIndex());
        destination.setContent(source.getContent());

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

                    FormLocalized item;

                    if (i.getValue().getId() == null) {
                        item = new FormLocalized();
                    } else {
                        item = getDb().find(FormLocalized.class, i.getValue().getId());
                    }

                    i.getValue().setLocale(i.getKey());
                    i.getValue().setParent(destination);

                    item = formLocalizedService.prepareForDbUpdate(i.getValue(), item);

                    // bug with ebean and Map collection
                    // changes are not propagated from parent
                    // we have to save/update localizations manually
                    return destination.getId() == null ?
                            item : formLocalizedService.upsert(item);
                })
                .collect(Collectors.toMap(t->t.getLocale() , t->t)));

        // remap sections
        destination.getSections().forEach(r -> {
            if (!source.getSections().stream().map((i -> i.getId())).collect(Collectors.toList()).contains(r.getId())) {
                this.getDb().delete(r);
            }
        });

        destination.setSections(source.getSections().stream()
                .map(i -> {
                    Section item;
                    if (i.getId() == null) {
                        item = new Section();
                    } else {
                        item = sectionService.getById(i.getId()).orElseGet(Section::new);
                    }
                    item = sectionService.prepareForDbUpdate(i, item);
                    return item;
                })
                .collect(Collectors.toList()));


        // remap questions
        destination.getQuestions().forEach(r -> {
            if (!source.getQuestions().stream().map((i -> i.getId())).collect(Collectors.toList()).contains(r.getId())) {
                this.getDb().delete(r);
            }
        });

        destination.setQuestions(source.getQuestions().stream()
                .map(i -> {
                    Question item;
                    if (i.getId() == null) {
                        item = new Question();
                    } else {
                        item = questionService.getById(i.getId()).orElseGet(Question::new);
                    }
                    item = questionService.prepareForDbUpdate(i, item);
                    return item;
                })
                .collect(Collectors.toList()));

        return destination;
    }

    @Override
    public Collection<String> getChildrenEntityNames() {
        return Arrays.asList(Section.class.getSimpleName(), Question.class.getSimpleName());
    }

    @Override
    public boolean hasChildren(UUID id) {
        Optional<Form> entity = getById(id);
        if (entity.isPresent()) {
           return (entity.get().getSections().size() > 0) || (entity.get().getQuestions().size() > 0);
        }
        return false;
    }

    @Override
    public Query<Form> eagerFetchFlatGraph(Query<Form> query) {
        query = query.fetch(QForm.alias().localisations.toString());
        return query;
    }

    @Override
    public Query<Form> eagerFetchFullGraph(Query<Form> query) {
        query = query.fetch(QForm.alias().localizations.toString());
        query = query.fetch(QForm.alias().localisations.toString());
        query = query.fetch(QForm.alias().sections.toString());
        query = query.fetch(QForm.alias().sections.localizations.toString());
        query = query.fetch(QForm.alias().sections.localisations.toString());
        query = query.fetch(QForm.alias().sections.questions.toString());
        query = query.fetch(QForm.alias().sections.questions.localizations.toString());
        query = query.fetch(QForm.alias().sections.questions.localisations.toString());
        query = query.fetch(QForm.alias().sections.questions.answerOptions.toString());
        query = query.fetch(QForm.alias().sections.questions.answerOptions.localizations.toString());
        query = query.fetch(QForm.alias().sections.questions.answerOptions.localisations.toString());
        query = query.fetch(QForm.alias().questions.toString());
        query = query.fetch(QForm.alias().questions.localizations.toString());
        query = query.fetch(QForm.alias().questions.localisations.toString());
        query = query.fetch(QForm.alias().questions.answerOptions.toString());
        query = query.fetch(QForm.alias().questions.answerOptions.localizations.toString());
        query = query.fetch(QForm.alias().questions.answerOptions.localisations.toString());
        return query;
    }

    @Override
    public void prepareForDbImport(Form destination) {

        // remap localizations
        destination.setLocalizations(destination.getLocalisations().stream()
                .collect(Collectors.toMap(t->t.getLocale() , t->t)));

        // remap sections
        destination.getSections().forEach(r -> {
            sectionService.prepareForDbImport(r);
        });

        // remap questions
        destination.getQuestions().forEach(r -> {
            questionService.prepareForDbImport(r);
        });
    }

    @Override
    public Form save(Form entityToInsert) {

        try (Transaction transaction = getDb().beginTransaction()) {
            entityToInsert.setOrderIndex(list(null).size() + 1);
            Form savedEntity = super.save(entityToInsert);
            transaction.commit();
            return savedEntity;
        }
    }

    @Override
    public void delete(UUID id) {

        try (Transaction transaction = getDb().beginTransaction()) {
            super.delete(id);
            List<Form> forms = list(null, null, null, "order_index");
            for (int i = 0; i < forms.size(); i++) {
                Form entityToOrder = getById(forms.get(i).getId()).get();
                entityToOrder.setOrderIndex(i + 1);
                update(entityToOrder);
            }
            transaction.commit();
        }
    }

    @Override
    public void move(Form entity, BaseModel target, DropLocation dropLocation) {


        try (Transaction transaction = getDb().beginTransaction()) {

            if (DropLocation.ABOVE.equals(dropLocation)) {
                if (!(target instanceof Form)) {
                    throw new UnsupportedOperationException("Section can not be moved above " + target.getClass().getSimpleName());
                }
            } else if (DropLocation.BELOW.equals(dropLocation)) {
                if (!(target instanceof Form)) {
                    throw new UnsupportedOperationException("Section can not be moved below " + target.getClass().getSimpleName());
                }
            } else if (DropLocation.ON_TOP.equals(dropLocation)) {
                throw new UnsupportedOperationException("Section can not be moved on top of " + target.getClass().getSimpleName());
            } else {
                throw new UnsupportedOperationException("Invalid drop location");
            }

            Form entityToMove = getById(entity.getId()).get();
            int index = 0;

            List<Form> allForms = list(null, null, null, "order_index");

            List<Form> targetItems = allForms
                    .stream()
                    .filter(i-> !i.getId().equals(entity.getId()) )
                    .sorted(Comparator.comparingInt(Form::getOrderIndex))
                    .collect(Collectors.toList());

            if (DropLocation.ABOVE.equals(dropLocation)) {
                Form targetItem = targetItems.stream().filter(s->target.getId().equals(s.getId())).findFirst().get();
                index = targetItems.indexOf(targetItem);
            } else if (DropLocation.BELOW.equals(dropLocation)) {
                Form targetItem = targetItems.stream().filter(s->target.getId().equals(s.getId())).findFirst().get();
                index = targetItems.indexOf(targetItem) + 1;
            }


            for (int i = 0; i < index; i ++) {
                Form tmp = getById(targetItems.get(i).getId()).get();
                tmp.setOrderIndex(i + 1);
                update(tmp);
            }

            for (int i = index; i < targetItems.size(); i ++) {
                Form tmp = getById(targetItems.get(i).getId()).get();
                tmp.setOrderIndex(i + 2);
                update(tmp);
            }

            entityToMove.setOrderIndex(index + 1);
            update(entityToMove);

            transaction.commit();
        }
    }

    @Override
    public boolean logExceptionIfIdExists(Form entity) {
        boolean result = false;
        Optional<Form> res = getById(entity.getId());
        if (res.isPresent()) {
            this.logIdExistsException(entity);
            result = true;
        }
        for(FormLocalized pl: entity.getLocalisations()) {
            if (formLocalizedService.logExceptionIfIdExists(pl)) {
                result = true;
            }
        }
        for(Section s: entity.getSections()) {
            if (sectionService.logExceptionIfIdExists(s)) {
                result = true;
            }
        }
        for(Question q: entity.getQuestions()) {
            if (questionService.logExceptionIfIdExists(q)) {
                result = true;
            }
        }
        return result;
    }
}
