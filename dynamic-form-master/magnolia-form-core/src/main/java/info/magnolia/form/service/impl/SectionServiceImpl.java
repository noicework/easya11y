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
import info.magnolia.form.domain.Question;
import info.magnolia.form.domain.Section;
import info.magnolia.form.domain.SectionLocalized;
import info.magnolia.form.domain.query.QSection;
import info.magnolia.form.service.DropLocation;
import info.magnolia.form.service.FormService;
import info.magnolia.form.service.IdNonExistsException;
import info.magnolia.form.service.QuestionService;
import info.magnolia.form.service.SectionLocalizedService;
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
 * Section service implementation class.
 */
public class SectionServiceImpl extends AbstractBaseServiceImpl<Section> implements SectionService, IdNonExistsException<Section> {

    private final QuestionService questionService;
    private final SectionLocalizedService sectionLocalizedService;
    private final FormService formService;

    @Inject
    public SectionServiceImpl(final Provider<FormCoreModule> formModuleProvider,
                              final QuestionService questionService,
                              final SectionLocalizedService sectionLocalizedService,
                              final FormService formService) {
        super(formModuleProvider);
        this.questionService = questionService;
        this.sectionLocalizedService = sectionLocalizedService;
        this.formService = formService;
    }

    @Override
    public Class<Section> getModelClass() {
        return Section.class;
    }

    @Override
    public String getEntityName() {
        return Section.class.getSimpleName();
    }

    @Override
    public Section prepareForDbUpdate(Section source, Section destination) {

        if (source == null || destination == null) {
            return null;
        }

        if (source.getId() != null) {
            destination.setId(source.getId());
        }
        destination.setTitle(source.getTitle());
        destination.setDescription(source.getDescription());
        destination.setOrderIndex(source.getOrderIndex());
        destination.setContent(source.getContent());

        if (source.getForm() != null && source.getForm().getId() != null) {
            // set reference
            destination.setForm(this.getDb().reference(Form.class, source.getForm().getId()));
        } else {
            destination.setForm(null);
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

                    SectionLocalized item;

                    if (i.getValue().getId() == null) {
                        item = new SectionLocalized();
                    } else {
                        item = getDb().find(SectionLocalized.class, i.getValue().getId());
                    }

                    i.getValue().setLocale(i.getKey());
                    i.getValue().setParent(destination);

                    item = sectionLocalizedService.prepareForDbUpdate(i.getValue(), item);

                    // bug with ebean and Map collection
                    // changes are not propagated from parent
                    // we have to save/update localizations manually
                    return destination.getId() == null ?
                            item : sectionLocalizedService.upsert(item);
                })
                .collect(Collectors.toMap(t->t.getLocale() , t->t)));


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
    public Optional getParent(UUID id) {

        Optional<Section> entity = getById(id);
        if (entity.isPresent()) {
            Form parent = entity.get().getForm();
            return Optional.ofNullable(parent);
        }

        return Optional.empty();
    }

    @Override
    public Collection<String> getChildrenEntityNames() {
        return Arrays.asList(Question.class.getSimpleName());
    }

    @Override
    public boolean hasChildren(UUID id) {
        Optional<Section> entity = getById(id);
        if (entity.isPresent()) {
            return (entity.get().getQuestions().size() > 0);
        }
        return false;
    }

    @Override
    public Query<Section> eagerFetchFlatGraph(Query<Section> query) {
        query = query.fetch(QSection.alias().localisations.toString());
        return query;
    }

    @Override
    public Query<Section> eagerFetchFullGraph(Query<Section> query) {
        query = query.fetch(QSection.alias().localizations.toString());
        query = query.fetch(QSection.alias().localisations.toString());
        query = query.fetch(QSection.alias().questions.toString());
        query = query.fetch(QSection.alias().questions.localizations.toString());
        query = query.fetch(QSection.alias().questions.localisations.toString());
        query = query.fetch(QSection.alias().questions.answerOptions.toString());
        query = query.fetch(QSection.alias().questions.answerOptions.localizations.toString());
        query = query.fetch(QSection.alias().questions.answerOptions.localisations.toString());
        return query;
    }

    @Override
    public void prepareForDbImport(Section destination) {

        // remap localizations
        destination.setLocalizations(destination.getLocalisations().stream()
                .collect(Collectors.toMap(t->t.getLocale() , t->t)));

        // remap questions
        destination.getQuestions().forEach(r -> {
            questionService.prepareForDbImport(r);
        });
    }

    @Override
    public Section save(Section entityToInsert) {

        try (Transaction transaction = getDb().beginTransaction()) {
            Form form = formService.getById(entityToInsert.getForm().getId()).get();
            entityToInsert.setOrderIndex(form.getSections().size() + 1);
            Section savedEntity = super.save(entityToInsert);
            transaction.commit();
            return savedEntity;
        }
    }

    @Override
    public void delete(UUID id) {

        try (Transaction transaction = getDb().beginTransaction()) {
            UUID entityParentId = getById(id).get().getForm().getId();
            super.delete(id);
            Form form = formService.getById(entityParentId).get();
            List<Section> sections = form.getSections()
                    .stream()
                    .sorted(Comparator.comparingInt(Section::getOrderIndex))
                    .collect(Collectors.toList());
            for (int i = 0; i < sections.size(); i++) {
                Section entityToOrder = getById(sections.get(i).getId()).get();
                entityToOrder.setOrderIndex(i + 1);
                update(entityToOrder);
            }
            transaction.commit();
        }
    }

    @Override
    public void move(Section entity, BaseModel target, DropLocation dropLocation) {

        Form targetParent;

        try (Transaction transaction = getDb().beginTransaction()) {

            if (DropLocation.ABOVE.equals(dropLocation)) {
                if (!(target instanceof Section)) {
                    throw new UnsupportedOperationException("Section can not be moved above " + target.getClass().getSimpleName());
                }
                targetParent = this.formService.getById(((Section) target).getForm().getId()).get();
            } else if (DropLocation.BELOW.equals(dropLocation)) {
                if (!(target instanceof Section)) {
                    throw new UnsupportedOperationException("Section can not be moved below " + target.getClass().getSimpleName());
                }
                targetParent = this.formService.getById(((Section) target).getForm().getId()).get();
            } else if (DropLocation.ON_TOP.equals(dropLocation)) {
                if (!(target instanceof Form)) {
                    throw new UnsupportedOperationException("Section can not be moved on top of " + target.getClass().getSimpleName());
                }
                targetParent = this.formService.getById((target).getId()).get();
            } else {
                throw new UnsupportedOperationException("Invalid drop location");
            }

            Section entityToMove = getById(entity.getId()).get();
            Form entityParent = this.formService.getById(entity.getForm().getId()).get();
            int index = 0;

            Form finalEntityParent = entityParent;
            List<Section> targetItems = targetParent.getSections()
                    .stream()
                    .filter(i-> !finalEntityParent.getId().equals(targetParent.getId()) || (finalEntityParent.getId().equals(targetParent.getId()) && !i.getId().equals(entity.getId())) )
                    .sorted(Comparator.comparingInt(Section::getOrderIndex))
                    .collect(Collectors.toList());

            if (DropLocation.ABOVE.equals(dropLocation)) {
                Section targetItem = targetItems.stream().filter(s->target.getId().equals(s.getId())).findFirst().get();
                index = targetItems.indexOf(targetItem);
            } else if (DropLocation.BELOW.equals(dropLocation)) {
                Section targetItem = targetItems.stream().filter(s->target.getId().equals(s.getId())).findFirst().get();
                index = targetItems.indexOf(targetItem) + 1;
            } else if (DropLocation.ON_TOP.equals(dropLocation)) {
                index = targetItems.size();
            }


            for (int i = 0; i < index; i ++) {
                Section tmp = getById(targetItems.get(i).getId()).get();
                tmp.setOrderIndex(i + 1);
                update(tmp);
            }

            for (int i = index; i < targetItems.size(); i ++) {
                Section tmp = getById(targetItems.get(i).getId()).get();
                tmp.setOrderIndex(i + 2);
                update(tmp);
            }

            entityToMove.setOrderIndex(index + 1);
            entityToMove.setForm(targetParent);
            update(entityToMove);

            if ((!entityParent.getId().equals(targetParent.getId()))) {

                entityParent = this.formService.getById(entity.getForm().getId()).get();

                targetItems = entityParent.getSections()
                        .stream()
                        .filter(i -> !i.getId().equals(entity.getId()))
                        .sorted(Comparator.comparingInt(Section::getOrderIndex))
                        .collect(Collectors.toList());

                for(int i = 0; i < targetItems.size(); i ++) {
                    Section tmp = getById(targetItems.get(i).getId()).get();
                    tmp.setOrderIndex(i + 1);
                    update(tmp);
                }
            }

            transaction.commit();
        }
    }

    @Override
    public boolean logExceptionIfIdExists(Section entity) {
        boolean result = false;
        Optional<Section> res = getById(entity.getId());
        if (res.isPresent()) {
            this.logIdExistsException(entity);
            result = true;
        }
        for(Question q: entity.getQuestions()) {
            if (questionService.logExceptionIfIdExists(q)) {
                result = true;
            }
        }
        return result;
    }
}
