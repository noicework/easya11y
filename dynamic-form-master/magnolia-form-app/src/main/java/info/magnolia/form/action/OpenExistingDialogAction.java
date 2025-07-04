/**
 * This file Copyright (c) 2010-2018 Magnolia International
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
package info.magnolia.form.action;

import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.form.data.DbItemResolver;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.ui.UIComponent;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import info.magnolia.ui.dialog.actions.OpenDialogActionDefinition;
import info.magnolia.ui.editor.LocaleContext;
import info.magnolia.ui.jdbc.service.DatabaseService;

import java.util.Optional;

import javax.inject.Inject;

/**
 * Opens a dialog and sets value to new entity.
 */
public class OpenExistingDialogAction extends AbstractOpenDialogAction {

    @Inject
    public OpenExistingDialogAction(OpenDialogActionDefinition definition,
                                    LocaleContext localeContext,
                                    ValueContext<BaseModel> valueContext,
                                    UIComponent parentView,
                                    I18NAuthoringSupport<BaseModel> i18NAuthoringSupport,
                                    DialogDefinitionRegistry dialogDefinitionRegistry,
                                    I18nizer i18nizer,
                                    DbItemResolver itemResolver) {

        super(definition, localeContext, valueContext, parentView, i18NAuthoringSupport, dialogDefinitionRegistry, i18nizer, itemResolver);

        if (valueContext.getSingle().isPresent()) {
            BaseModel item = valueContext.getSingle().get();
            DatabaseService service = itemResolver.getServiceByItemType(item.getClass().getSimpleName());
            if (service != null) {
                Optional<BaseModel> detailedItem = service.getById(item.getId());
                if(detailedItem.isPresent()) {
                    getValueContext().set(detailedItem.get());
                }
            }
        }
    }
}
