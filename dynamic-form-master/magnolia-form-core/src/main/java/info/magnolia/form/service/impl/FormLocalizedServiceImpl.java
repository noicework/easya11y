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
import info.magnolia.form.domain.FormLocalized;
import info.magnolia.form.service.FormLocalizedService;
import info.magnolia.form.service.IdNonExistsException;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Form Localized service implementation class.
 */
public class FormLocalizedServiceImpl extends AbstractBaseServiceImpl<FormLocalized> implements FormLocalizedService, IdNonExistsException<FormLocalized> {

    @Inject
    public FormLocalizedServiceImpl(final Provider<FormCoreModule> formModuleProvider) {
        super(formModuleProvider);
    }

    @Override
    public Class<FormLocalized> getModelClass() {
        return FormLocalized.class;
    }

    @Override
    public String getEntityName() {
        return FormLocalized.class.getSimpleName();
    }

    @Override
    public FormLocalized prepareForDbUpdate(FormLocalized source, FormLocalized destination) {

        if (source == null || destination == null) {
            return null;
        }

        if (source.getId() != null) {
            destination.setId(source.getId());
        }
        destination.setTitle(source.getTitle());
        destination.setDescription(source.getDescription());
        destination.setLocale(source.getLocale());
        destination.setParent(source.getParent());
        destination.setContent(source.getContent());

        return destination;
    }

    @Override
    public boolean logExceptionIfIdExists(FormLocalized entity) {
        Optional<FormLocalized> res = getById(entity.getId());
        if (res.isPresent()) {
            this.logIdExistsException(entity);
            return true;
        }
        return false;
    }
}
