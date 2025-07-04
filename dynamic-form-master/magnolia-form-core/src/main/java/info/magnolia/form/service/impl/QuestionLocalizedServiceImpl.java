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
import info.magnolia.form.domain.QuestionLocalized;
import info.magnolia.form.service.IdNonExistsException;
import info.magnolia.form.service.QuestionLocalizedService;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Question Localized service implementation class.
 */
public class QuestionLocalizedServiceImpl extends AbstractBaseServiceImpl<QuestionLocalized> implements QuestionLocalizedService, IdNonExistsException<QuestionLocalized> {

    @Inject
    public QuestionLocalizedServiceImpl(final Provider<FormCoreModule> formModuleProvider) {
        super(formModuleProvider);
    }

    @Override
    public Class<QuestionLocalized> getModelClass() {
        return QuestionLocalized.class;
    }

    @Override
    public String getEntityName() {
        return QuestionLocalized.class.getSimpleName();
    }

    @Override
    public QuestionLocalized prepareForDbUpdate(QuestionLocalized source, QuestionLocalized destination) {

        if (source == null || destination == null) {
            return null;
        }

        if (source.getId() != null) {
            destination.setId(source.getId());
        }
        destination.setTitle(source.getTitle());
        destination.setQuestion(source.getQuestion());
        destination.setLocale(source.getLocale());
        destination.setParent(source.getParent());
        destination.setContent(source.getContent());

        return destination;
    }

    @Override
    public boolean logExceptionIfIdExists(QuestionLocalized entity) {
        Optional<QuestionLocalized> res = getById(entity.getId());
        if (res.isPresent()) {
            this.logIdExistsException(entity);
            return true;
        }
        return false;
    }
}
