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
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.UIComponent;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import info.magnolia.ui.dialog.actions.OpenDialogAction;
import info.magnolia.ui.dialog.actions.OpenDialogActionDefinition;
import info.magnolia.ui.editor.LocaleContext;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import com.vaadin.ui.Window;

/**
 * Opens a dialog and sets value to new entity.
 */
public abstract class AbstractOpenDialogAction extends OpenDialogAction<BaseModel> {

    private final BaseModel listItem;
    private final DbItemResolver itemResolver;

    @Inject
    public AbstractOpenDialogAction(OpenDialogActionDefinition definition,
                                    LocaleContext localeContext,
                                    ValueContext<BaseModel> valueContext,
                                    UIComponent parentView,
                                    I18NAuthoringSupport<BaseModel> i18NAuthoringSupport,
                                    DialogDefinitionRegistry dialogDefinitionRegistry,
                                    I18nizer i18nizer,
                                    DbItemResolver itemResolver) {

        super(definition, localeContext, valueContext, parentView, i18NAuthoringSupport, dialogDefinitionRegistry, i18nizer);
        listItem = getValueContext().getSingle().orElse(null);
        this.itemResolver = itemResolver;
    }

    @Override
    protected CloseHandler getCloseHandler(Window dialog) {

        dialog.addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                // set selected item in the grid view
                HashSet<BaseModel> valueContext = new HashSet<>();

                BaseModel flatItem = null;
                if(getValueContext().getSingle().isPresent()) {
                    BaseModel item = getValueContext().getSingle().get();
                    UUID itemId = item.getId();
                    if (itemId != null) {
                        Optional<BaseModel> optionalItem = itemResolver.getServiceByItemType(item.getClass().getSimpleName()).getById(itemId);
                        if(optionalItem.isPresent()) {
                            flatItem = optionalItem.get();
                        }
                    } else {
                        if (listItem != null) {
                            flatItem = listItem;
                        }
                    }
                }

                if (flatItem != null) {
                    valueContext.add(flatItem);
                }

                getValueContext().set(valueContext);
            }
        });

        return dialog::close;
    }
}
