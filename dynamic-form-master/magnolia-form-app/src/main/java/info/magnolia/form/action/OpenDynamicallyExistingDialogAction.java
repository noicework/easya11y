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
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import info.magnolia.ui.dialog.actions.OpenDialogActionDefinition;
import info.magnolia.ui.editor.LocaleContext;
import info.magnolia.ui.jdbc.service.DatabaseService;

import java.util.Optional;

import javax.inject.Inject;

/**
 * Opens a dialog, sets dialog id based on type, and sets value to new entity.
 */
public class OpenDynamicallyExistingDialogAction extends AbstractOpenDialogAction {

    @Inject
    public OpenDynamicallyExistingDialogAction(OpenDialogActionDefinition definition,
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

    @Override
    public void execute() throws ActionExecutionException {
        this.setDialogIdDynamically();
        super.execute();
    }

    private void setDialogIdDynamically(){
        if (this.getValueContext().getSingle().isPresent()) {
            BaseModel item = this.getValueContext().getSingle().get();
            if(item instanceof Form){
                this.getDefinition().setDialogId("form-app:form-edit");
            } else if(item instanceof Question){
                this.getDefinition().setDialogId("form-app:question-edit");
            } else if(item instanceof AnswerOption) {
                this.getDefinition().setDialogId("form-app:answer-edit");
            } else if(item instanceof Section) {
                this.getDefinition().setDialogId("form-app:section-edit");
            }
        }
    }
}
