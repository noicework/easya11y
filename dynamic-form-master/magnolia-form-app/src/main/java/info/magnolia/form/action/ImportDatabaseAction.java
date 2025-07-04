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
package info.magnolia.form.action;

import info.magnolia.form.data.DbItemResolver;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.service.BaseService;
import info.magnolia.ui.CloseHandler;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.datasource.optionlist.Option;
import info.magnolia.ui.editor.FormView;
import info.magnolia.ui.observation.DatasourceObservation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.jcr.Node;

import com.google.common.io.Files;
import com.vaadin.data.BinderValidationStatus;
import com.vaadin.data.BindingValidationStatus;
import com.vaadin.ui.Component;

import lombok.SneakyThrows;

/**
 * Action for importing form related entity to database;
 */
public class ImportDatabaseAction extends AbstractAction<ImportDatabaseActionDefinition> {
    private final ValueContext<BaseModel> valueContext;
    private final DbItemResolver itemResolver;
    private final FormView<Node> form;
    private final CloseHandler closeHandler;
    private final DatasourceObservation.Manual datasourceObservation;

    @Inject
    public ImportDatabaseAction(ImportDatabaseActionDefinition definition,
                                ValueContext<BaseModel> valueContext,
                                DbItemResolver itemResolver,
                                FormView<Node> form,
                                CloseHandler closeHandler,
                                DatasourceObservation.Manual datasourceObservation) {
        super(definition);
        this.valueContext = valueContext;
        this.itemResolver = itemResolver;
        this.form = form;
        this.closeHandler = closeHandler;
        this.datasourceObservation = datasourceObservation;
    }

    @SneakyThrows
    public void execute() {

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

        this.executeImport();
    }

    private void executeImport() throws IOException {
        BaseModel selectedEntity = null;

        if (this.getUploadedFile().isPresent() && this.getBehaviour().isPresent()) {

            if (valueContext.getSingle().isPresent()) {
                BaseModel item = valueContext.getSingle().get();
                BaseService service = (BaseService) itemResolver.getServiceByItemType(item.getClass().getSimpleName());
                if (service != null) {
                    Optional<BaseModel> detailedItem = service.getById(item.getId());
                    if (detailedItem.isPresent()) {
                        selectedEntity = detailedItem.get();
                    }
                }
            }

            try (Stream<String> stream = Files.readLines(this.getUploadedFile().get(), Charset.forName("UTF-8")).stream()) {
                // convert stream into a string
                String yaml = stream.collect(Collectors.joining(System.lineSeparator()));
                BaseService formService = (BaseService) itemResolver.getServiceByItemType("Form");
                String entityType = formService.entityTypeFromYaml(yaml);
                BaseService service = (BaseService) itemResolver.getServiceByItemType(entityType);
                service.importFromYaml(yaml, selectedEntity, this.getBehaviour().get().getValue());
                datasourceObservation.trigger();
            }
        }

        this.closeHandler.close();
    }

    private Optional<File> getUploadedFile() {
        return this.form.getPropertyValue("contentStream");
    }

    private Optional<Option> getBehaviour() {
        return this.form.getPropertyValue("behaviour");
    }
}
