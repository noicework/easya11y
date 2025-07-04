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

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.form.data.DbItemResolver;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.ui.AlertBuilder;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.jdbc.service.DatabaseService;
import info.magnolia.ui.observation.DatasourceObservation;

import java.util.HashSet;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Notification;

/**
 * Deletes database entity from the db using the delete command.
 */
public class DeleteDatabaseAction extends AbstractDatabaseMultiItemAction<DeleteDatabaseActionDefinition> {


    private final Logger log = LoggerFactory.getLogger(DeleteDatabaseAction.class);

    private final SimpleTranslator i18n;
    private final DbItemResolver itemResolver;
    private final DatasourceObservation.Manual datasourceObservation;
    private final ValueContext<BaseModel> valueContext;

    @Inject
    public DeleteDatabaseAction(DeleteDatabaseActionDefinition definition,
                                ValueContext<BaseModel> valueContext,
                                SimpleTranslator i18n,
                                DbItemResolver itemResolver,
                                DatasourceObservation.Manual datasourceObservation) {

        super(definition, valueContext);
        this.i18n = i18n;
        this.itemResolver = itemResolver;
        this.datasourceObservation = datasourceObservation;
        this.valueContext = valueContext;
    }

    @Override
    public void execute() throws ActionExecutionException {

        AlertBuilder.confirmDialog(i18n.translate("ui-framework.actions.deleteItem.warningText"))
                .withLevel(Notification.Type.HUMANIZED_MESSAGE)
                .withBody(getConfirmationQuestion())
                .withOkButtonCaption(i18n.translate("ui-framework.actions.deleteItem.confirmText"))
                .withDeclineButtonCaption(i18n.translate("ui-framework.actions.deleteItem.cancelText"))
                .withConfirmationHandler(() -> DeleteDatabaseAction.this.executeAfterConfirmation())
                .buildAndOpen();
    }

    private String getConfirmationQuestion() {
        if (getItems().size() == 1) {
            return i18n.translate("ui-framework.actions.deleteItem.confirmationQuestionOneItem");
        }
        return String.format(i18n.translate("ui-framework.actions.deleteItem.confirmationQuestionManyItems"), getItems().size());
    }

    protected void executeAfterConfirmation() {
        try {
            super.execute();
            this.valueContext.set(new HashSet());
        } catch (ActionExecutionException e) {
            log.error("Problem occurred during deleting items.", e);
        }
    }

    @Override
    protected void executeOnItem(BaseModel item) throws Exception {

        try {
            DatabaseService service = itemResolver.getServiceByItemType(item.getClass().getSimpleName());
            if(service != null) {
                service.delete(item.getId());
                datasourceObservation.trigger();
            }
        } catch (Exception e) {
            String msg = String.format("can not delete client with uuid: %s", itemResolver.getId(item));
            log.error(msg, e);
            throw new ActionExecutionException(msg, e);
        }
    }

    @Override
    protected String getSuccessMessage() {
        if (getItems().size() == 1) {
            return i18n.translate("ui-framework.actions.deleteItem.successOneItemDeleted");
        } else {
            return i18n.translate("ui-framework.actions.deleteItem.successManyItemsDeleted", getItems().size());
        }
    }

    @Override
    protected String getFailureMessage() {
        return i18n.translate("ui-framework.actions.deleteItem.deletionfailure", getFailedItems().size(), getItems().size());
    }
}
