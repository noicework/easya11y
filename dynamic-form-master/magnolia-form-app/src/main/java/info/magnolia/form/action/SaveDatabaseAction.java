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

import info.magnolia.form.data.DbItemResolver;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.contentapp.action.CloseAction;
import info.magnolia.ui.contentapp.action.CommitActionDefinition;
import info.magnolia.ui.editor.EditorView;
import info.magnolia.ui.jdbc.service.DatabaseService;
import info.magnolia.ui.observation.DatasourceObservation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.vaadin.data.BinderValidationStatus;
import com.vaadin.data.BindingValidationStatus;
import com.vaadin.ui.Component;

/**
 * Save database entity action.
 * @param <T>
 */
public class SaveDatabaseAction<T extends SaveDatabaseActionDefinition> extends CloseAction<CommitActionDefinition> {

    private final ValueContext<BaseModel> valueContext;
    private final EditorView<BaseModel> form;
    private final DbItemResolver itemResolver;
    private final DatasourceObservation.Manual datasourceObservation;

    @Inject
    public SaveDatabaseAction(SaveDatabaseActionDefinition definition,
                              CloseHandler closeHandler,
                              ValueContext<BaseModel> valueContext,
                              EditorView<BaseModel> form,
                              DbItemResolver itemResolver,
                              DatasourceObservation.Manual datasourceObservation) {
        super(definition, closeHandler);
        this.valueContext = valueContext;
        this.form = form;
        this.datasourceObservation = datasourceObservation;
        this.itemResolver = itemResolver;
    }

    @Override
    public void execute() throws ActionExecutionException {
        List<BinderValidationStatus<?>> validationStatuses = form.validate();
        boolean hasValidationErrors = validationStatuses.stream().anyMatch(BinderValidationStatus::hasErrors);

        // If there are any validation errors, focus on the first focusable parent and return
        if (hasValidationErrors) {
            // Flatten the list of FieldValidationErrors from both the form and the subforms
            List<BindingValidationStatus<?>> bindingValidationStatuses = validationStatuses.stream()
                    .map(BinderValidationStatus::getFieldValidationErrors)
                    .flatMap(Collection::stream).collect(Collectors.toList());

            // Fetch all the form validation errors for the first locale
            for (BindingValidationStatus<?> validationStatus : bindingValidationStatuses) {
                Component field = (Component) validationStatus.getField();
                Component parent = field.getParent();

                while ((parent != null) && !(parent instanceof Component.Focusable)) {
                    parent = parent.getParent();
                }

                if (parent != null) {
                    ((Component.Focusable) parent).focus();
                    break;
                }
            }
            return;
        }

        write();

        super.execute();
    }

    protected void write() {
        valueContext.getSingle().ifPresent(item -> {

            form.write(item);
            BaseModel savedItem;
            Optional<BaseModel> itemForContext = Optional.empty();

            DatabaseService service = itemResolver.getServiceByItemType(item.getClass().getSimpleName());

            if (service != null) {

                if (item.getId() == null) {
                    savedItem = (BaseModel) service.save(item);
                } else {
                    savedItem = (BaseModel) service.update(item);
                }
                datasourceObservation.trigger();
                itemForContext = service.getById(savedItem.getId());
            }

            itemForContext.ifPresent(valueContext::set);
        });
    }
}
