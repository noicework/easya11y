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
import info.magnolia.form.domain.AnswerOptionLocalized;
import info.magnolia.form.service.AnswerOptionLocalizedService;
import info.magnolia.form.service.IdNonExistsException;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * AnswerOption Localized service implementation class.
 */
public class AnswerOptionLocalizedServiceImpl extends AbstractBaseServiceImpl<AnswerOptionLocalized> implements AnswerOptionLocalizedService, IdNonExistsException<AnswerOptionLocalized> {

    @Inject
    public AnswerOptionLocalizedServiceImpl(final Provider<FormCoreModule> formModuleProvider) {
        super(formModuleProvider);
    }

    @Override
    public Class<AnswerOptionLocalized> getModelClass() {
        return AnswerOptionLocalized.class;
    }

    @Override
    public String getEntityName() {
        return AnswerOptionLocalized.class.getSimpleName();
    }

    @Override
    public AnswerOptionLocalized prepareForDbUpdate(AnswerOptionLocalized source, AnswerOptionLocalized destination) {

        if (source == null || destination == null) {
            return null;
        }

        if (source.getId() != null) {
            destination.setId(source.getId());
        }
        destination.setTitle(source.getTitle());
        destination.setLabel(source.getLabel());
        destination.setFreeTextLabel(source.getFreeTextLabel());
        destination.setLocale(source.getLocale());
        destination.setParent(source.getParent());
        destination.setContent(source.getContent());

        return destination;
    }

    @Override
    public boolean logExceptionIfIdExists(AnswerOptionLocalized entity) {
        Optional<AnswerOptionLocalized> res = getById(entity.getId());
        if (res.isPresent()) {
            this.logIdExistsException(entity);
            return true;
        }
        return false;
    }
}
