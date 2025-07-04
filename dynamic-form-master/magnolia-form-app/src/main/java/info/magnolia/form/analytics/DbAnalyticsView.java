/**
 * This file Copyright (c) 2019-2021 Magnolia International
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
package info.magnolia.form.analytics;

import info.magnolia.analytics.DashboardComposer;
import info.magnolia.analytics.common.ChartDefinition;
import info.magnolia.analytics.datasource.AbstractExternalDataSupplier;
import info.magnolia.analytics.datasource.DataSupplier;
import info.magnolia.analytics.ui.app.view.extension.AnalyticsViewDefinition;
import info.magnolia.analytics.vaadin.ChartComponent;
import info.magnolia.analytics.vaadin.NoDataAvailableComponent;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.domain.Form;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.contentapp.browser.ContentView;
import info.magnolia.ui.field.FieldDefinition;
import info.magnolia.ui.field.FieldFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.HasValue;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Composite;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Analytics module implementation of {@link ContentView}.
 */
@StyleSheet("vaadin://themes/resurface/analytics-ui-styles.css")
public class DbAnalyticsView extends Composite implements ContentView {
    /** Normal chart row height.*/
    private static final float NORMAL_HEIGHT = 300.0f;
    /** Reduced chart row height.*/
    private static final float REDUCED_HEIGHT = 100.0f;

    /** Simple translator. */
    private final SimpleTranslator i18n;
    /** Definition. */
    private final AnalyticsViewDefinition definition;
    /** Component provider. */
    private final ComponentProvider componentProvider;
    /** Dashboard composer. */
    private final DashboardComposer dashboardComposer;
    /** Value context. */
    private final ValueContext<BaseModel> valueContext;
    /** Filters map. */
    private final Map<String, String> filteredParameters;

    /** Chart selector. */
    private ComboBox<ChartDefinition> chartsComboBox;
    /** Chart row. */
    private HorizontalLayout chartRow;

    /** Current chart definition. */
    private ChartDefinition currentChartDefinition;

    private UUID selectedId = null;

    @Inject
    public DbAnalyticsView(AnalyticsViewDefinition definition, SimpleTranslator i18n,
                         ComponentProvider componentProvider, DashboardComposer dashboardComposer,
                         ValueContext<BaseModel> valueContext) {
        this.i18n = i18n;
        this.definition = definition;
        this.componentProvider = componentProvider;
        this.dashboardComposer = dashboardComposer;
        this.valueContext = valueContext;
        this.filteredParameters = new HashMap<>();

        VerticalLayout root = new VerticalLayout();
        root.addStyleName("dashboard-app");
        root.setMargin(false);
        root.setSpacing(true);

        // Initialise the component
        this.chartRow = new HorizontalLayout();
        this.chartRow.setMargin(false);
        this.chartRow.setSpacing(false);
        this.chartRow.setWidth(100.0f, Unit.PERCENTAGE);
        this.chartRow.setHeight(NORMAL_HEIGHT, Unit.PIXELS);
        root.addComponents(this.createFilterRow(), this.chartRow);

        // Load the first chart definition per default
        if (CollectionUtils.isNotEmpty(this.definition.getChartDefinitions())) {
            this.currentChartDefinition = this.definition.getChartDefinitions().stream().findFirst().get();
            this.chartsComboBox.setValue(this.currentChartDefinition);
        }

        // Observe change on the workbench
        valueContext.observe(this::onItemSelectionChange);

        this.setCompositionRoot(root);
    }

    /**
     * Creates the filters row.
     *
     * @return The filters row
     */
    protected Component createFilterRow() {
        HorizontalLayout filterRow = new HorizontalLayout();
        filterRow.setSpacing(false);
        filterRow.addStyleNames("periscope-filter-options-wrapper", "filter-bar", "extension-view-filter-bar");

        // Chart definition combobox
        this.chartsComboBox = new ComboBox<>();
        this.chartsComboBox.setItems(this.definition.getChartDefinitions());
        this.chartsComboBox.setItemCaptionGenerator(item -> this.i18n.translate(item.getLabel()));
        this.chartsComboBox.setEmptySelectionAllowed(false);
        this.chartsComboBox.setTextInputAllowed(false);
        this.chartsComboBox.addValueChangeListener(this::onChartDefinitionChange);
        filterRow.addComponent(this.chartsComboBox);

        if (CollectionUtils.isNotEmpty(this.definition.getFilterFields())) {
            this.definition.getFilterFields().forEach(fieldDefinition ->
                    filterRow.addComponent(this.createFilterItem(fieldDefinition)));
        }

        return filterRow;
    }

    /**
     * Triggered when nodes are selected on the workbench.
     *
     * @param items The list of selected db items
     */
    protected void onItemSelectionChange(Set<BaseModel> items) {
        String paramName = "id";
        UUID tmpSelectedId = null;

        DataSupplier dataSupplier = this.resolveDataSupplier();
        if (dataSupplier instanceof AbstractExternalDataSupplier) {
            if (this.valueContext.getSingle().isPresent()) {

                BaseModel currentItem = this.valueContext.getSingle().get();

                if (currentItem instanceof Form) {
                    tmpSelectedId = currentItem.getId();
                }

                if (tmpSelectedId != null) {
                    this.filteredParameters.put(paramName, String.valueOf(tmpSelectedId));
                }
            }

            if (tmpSelectedId == null) {
                this.filteredParameters.remove(paramName);
            }

            if (!Objects.equals(this.selectedId, tmpSelectedId)) {
                this.selectedId = tmpSelectedId;
                this.refreshChartRow();
            }
        }
    }

    /**
     * Intercept the changes on the chart definition combobox.
     *
     * @param event The change event
     */
    protected void onChartDefinitionChange(HasValue.ValueChangeEvent<ChartDefinition> event) {
        this.currentChartDefinition = event.getValue();

        this.refreshChartRow();
    }

    /**
     * Instantiate a filter field.
     *
     * @param fieldDefinition The filter field definition
     * @return The filter field instance
     */
    private Component createFilterItem(FieldDefinition fieldDefinition) {
        FieldFactory fieldFactory = (FieldFactory) this.componentProvider.newInstance(fieldDefinition.getFactoryClass(), fieldDefinition);
        HasValue field = fieldFactory.createField();
        field.addValueChangeListener(event -> {
            Object propertyValue = event.getValue();

            if (propertyValue != null) {
                this.filteredParameters.put(fieldDefinition.getName(), propertyValue.toString());
            }

            this.refreshChartRow();
        });
        ((Component) field).addStyleNames("filter-item", "periscope-filter-item");

        return (Component) field;
    }

    /**
     * Refresh the chart row.
     */
    private void refreshChartRow() {
        ChartComponent chartComponent;
        if (StringUtils.isNotEmpty(this.currentChartDefinition.getDataSupplier())) {
            chartComponent = (ChartComponent) this.dashboardComposer.initialiseComponent(this.currentChartDefinition,
                    this.resolveDataSupplier(), this.filteredParameters);
        } else {
            chartComponent = (ChartComponent) this.dashboardComposer.initialiseComponent(this.currentChartDefinition);
        }

        this.chartRow.removeAllComponents();
        if (!chartComponent.isEmpty()) {
            this.chartRow.setHeight(NORMAL_HEIGHT, Unit.PIXELS);
            this.chartRow.addComponent(chartComponent);
        } else {
            this.chartRow.setHeight(REDUCED_HEIGHT, Unit.PIXELS);
            this.chartRow.addComponent(new NoDataAvailableComponent(this.i18n));
        }
    }

    /**
     * Return the decorated {@link DataSupplier} in case a multisite definition is found, otherwise the original @{@link DataSupplier}.
     *
     * @return The {@link DataSupplier} to be used
     */
    private DataSupplier resolveDataSupplier() {
        DataSupplier dataSupplier = this.definition.getDataSuppliers().get(this.currentChartDefinition.getDataSupplier());
        return dataSupplier;
    }
}
