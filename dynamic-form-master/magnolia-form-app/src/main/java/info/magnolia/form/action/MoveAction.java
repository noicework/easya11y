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
import info.magnolia.form.service.BaseService;
import info.magnolia.form.service.DropLocation;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.chooser.definition.ChooserDefinition;
import info.magnolia.ui.dialog.DialogDefinitionRegistry;
import info.magnolia.ui.framework.overlay.ChooserController;
import info.magnolia.ui.jdbc.service.DatabaseService;
import info.magnolia.ui.observation.DatasourceObservation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

/**
 * Move action.
 * @param <T> type.
 */
public class MoveAction<T> extends AbstractAction<MoveActionDefinition<T>> {

    private final ChooserController chooserController;
    private final ValueContext<T> valueContext;
    private final DialogDefinitionRegistry dialogDefinitionRegistry;
    private final I18nizer i18nizer;
    private final DbItemResolver itemResolver;
    private final DatasourceObservation.Manual datasourceObservation;

    @Inject
    MoveAction(MoveActionDefinition<T> actionDefinition,
               ChooserController chooserController,
               ValueContext<T> valueContext,
               DialogDefinitionRegistry dialogDefinitionRegistry,
               I18nizer i18nizer,
               DbItemResolver itemResolver,
               DatasourceObservation.Manual datasourceObservation) {

        super(actionDefinition);
        this.chooserController = chooserController;
        this.valueContext = valueContext;
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
        this.i18nizer = i18nizer;
        this.itemResolver = itemResolver;
        this.datasourceObservation = datasourceObservation;
    }

    @Override
    public void execute() throws ActionExecutionException {
        ChooserDefinition<T, ?> chooser = (ChooserDefinition<T, ?>) i18nizer.decorate(dialogDefinitionRegistry.getProvider(getDefinition().getDialogId()).get());

        final ChooserController.OnItemChosen<T> onItemChosen = chooserController.openChooser(chooser);
        onItemChosen
                .whenComplete((chooseResult, err) -> {
                    if (err != null) {
                        throw new RuntimeException(err);
                    }
                    final List<T> items = valueContext.get().collect(Collectors.toList());
                    if (items.size() > 0) {
                        final T target = chooseResult.getChoice().orElseThrow(IllegalArgumentException::new);
                        final DropLocation dropLocation = Optional.of(onItemChosen.getAction())
                                .flatMap(action -> Stream.of(DropLocation.values()).filter(location -> StringUtils.equalsIgnoreCase(location.name(), action)).findFirst())
                                .orElse(DropLocation.ON_TOP);

                        for (T item : items) {
                            DatabaseService service = itemResolver.getServiceByItemType(item.getClass().getSimpleName());
                            ((BaseService) service).move((BaseModel) item, (BaseModel) target, dropLocation);
                        }
                        this.datasourceObservation.trigger();
                    }
                });
    }
}
