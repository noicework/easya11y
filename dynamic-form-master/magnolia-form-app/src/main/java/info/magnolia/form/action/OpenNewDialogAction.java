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
import info.magnolia.form.domain.AnswerOption;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.domain.Form;
import info.magnolia.form.domain.Question;
import info.magnolia.form.domain.Section;
import info.magnolia.ui.UIComponent;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import info.magnolia.ui.editor.LocaleContext;
import info.magnolia.ui.jdbc.service.DatabaseService;

import java.util.Optional;

import javax.inject.Inject;

/**
 * Opens a dialog and sets value to new entity.
 */
public class OpenNewDialogAction extends AbstractOpenDialogAction {

    @Inject
    public OpenNewDialogAction(OpenNewDialogActionDbDefinition definition,
                               LocaleContext localeContext,
                               ValueContext<BaseModel> valueContext,
                               UIComponent parentView,
                               I18NAuthoringSupport<BaseModel> i18NAuthoringSupport,
                               DialogDefinitionRegistry dialogDefinitionRegistry,
                               I18nizer i18nizer,
                               DbItemResolver itemResolver) {

        super(definition, localeContext, valueContext, parentView, i18NAuthoringSupport, dialogDefinitionRegistry, i18nizer, itemResolver);

        Optional<BaseModel> currentItem = Optional.empty();
        if (valueContext.getSingle().isPresent()) {
            BaseModel item = valueContext.getSingle().get();
            DatabaseService service = itemResolver.getServiceByItemType(item.getClass().getSimpleName());
            if (service != null) {
                currentItem = service.getById(item.getId());
            }
        }

        DatabaseService service = itemResolver.getServiceByItemType(definition.getEntityName());
        if (service != null) {
            BaseModel newItem = (BaseModel) service.newItem();

            if (currentItem.isPresent()) {
                if (definition.getEntityName().equals(Section.class.getSimpleName())) {
                    ((Section) newItem).setForm((Form) currentItem.get());
                } else if (definition.getEntityName().equals(Question.class.getSimpleName())) {
                    if (currentItem.get() instanceof Form) {
                        ((Question) newItem).setForm((Form) currentItem.get());
                    } else if (currentItem.get() instanceof Section) {
                        ((Question) newItem).setSection((Section) currentItem.get());
                    }
                } else if (definition.getEntityName().equals(AnswerOption.class.getSimpleName())) {
                    ((AnswerOption) newItem).setQuestion((Question) currentItem.get());
                }
            }
            getValueContext().set(newItem);
        }
    }
}
