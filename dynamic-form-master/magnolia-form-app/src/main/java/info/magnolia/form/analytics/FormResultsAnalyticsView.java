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

import info.magnolia.analytics.AnalyticsModule;
import info.magnolia.analytics.ChartDefinitionRegistry;
import info.magnolia.analytics.DashboardComposer;
import info.magnolia.analytics.common.ChartDefinition;
import info.magnolia.analytics.vaadin.ChartComponent;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.form.domain.AnswerOption;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.domain.Form;
import info.magnolia.form.domain.Question;
import info.magnolia.form.domain.ResponseItem;
import info.magnolia.form.service.QuestionService;
import info.magnolia.form.service.ResponseItemService;
import info.magnolia.ui.contentapp.browser.ContentView;
import info.magnolia.ui.jdbc.bean.filter.ComplexCriterion;
import info.magnolia.ui.jdbc.bean.filter.Criterion;
import info.magnolia.ui.jdbc.bean.filter.LogicalOperator;
import info.magnolia.ui.jdbc.bean.filter.Operator;
import info.magnolia.ui.jdbc.bean.filter.SimpleCriterion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Composite;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.DateRenderer;

/**
 * Analytics module implementation of {@link ContentView}.
 */
@StyleSheet("vaadin://themes/resurface/analytics-ui-styles.css")
public class FormResultsAnalyticsView extends Composite implements ContentView {

    /** Simple translator. */
    private final SimpleTranslator i18n;
    /** Component provider. */
    private final ComponentProvider componentProvider;
    /** Dashboard composer. */
    private final DashboardComposer dashboardComposer;
    /** Chart row. */
    private VerticalLayout chartsContainer;
    /** Response Items service. **/
    private ResponseItemService responseItemService;

    private ChartDefinitionRegistry chartDefinitionRegistry;

    private final String QUESTION_ID = "questionId";

    @Inject
    public FormResultsAnalyticsView(SimpleTranslator i18n,
                                    ComponentProvider componentProvider,
                                    DashboardComposer dashboardComposer,
                                    ChartDefinitionRegistry chartDefinitionRegistry,
                                    ResponseItemService responseItemService) {
        this.i18n = i18n;
        this.componentProvider = componentProvider;
        this.dashboardComposer = dashboardComposer;
        this.chartDefinitionRegistry = chartDefinitionRegistry;
        this.responseItemService = responseItemService;

        VerticalLayout root = new VerticalLayout();
        root.addStyleName("dashboard-app");
        root.setMargin(false);
        root.setSpacing(true);

        // Initialise the component
        this.chartsContainer = new VerticalLayout();
        this.chartsContainer.setMargin(true);
        this.chartsContainer.setSpacing(true);
        this.chartsContainer.setWidth(100.0f, Unit.PERCENTAGE);
        this.chartsContainer.setHeight(100.0f, Unit.PERCENTAGE);
        root.addComponents(this.chartsContainer);

        this.setCompositionRoot(root);
    }


    /**
     * Create question result charts.
     */
    public void initCharts(BaseModel item) {

        this.dashboardComposer.invalidateDataSuppliersCache();

        this.chartsContainer.removeAllComponents();

        if (item instanceof Form) {

            Form form = (Form) item;

            List<Question> questions = form.getSections()
                    .stream()
                    .flatMap(s -> s.getQuestions()
                            .stream()
                            .sorted(Comparator.comparingInt(Question::getOrderIndex)))
                    .collect(Collectors.toList());
            questions.addAll(form.getQuestions()
                    .stream()
                    .sorted(Comparator.comparingInt(Question::getOrderIndex))
                    .collect(Collectors.toList()));

            for (Question question : questions) {

                VerticalLayout chartContainer = new VerticalLayout();
                chartContainer.setSizeFull();
                chartContainer.setDefaultComponentAlignment(Alignment.TOP_CENTER);
                chartContainer.setSpacing(true);
                chartContainer.setResponsive(true);

                Label title = createChartTitle(question);
                chartContainer.addComponent(title);

                if ("single".equals(question.getQuestionType())) {

                    ChartComponent chartComponent = createSingleSelectChartFromDefinition(question);
                    chartContainer.addComponent(chartComponent);

                } else if ("multi".equals(question.getQuestionType())) {

                    ChartComponent chartComponent = createMultiSelectChartFromDefinition(question);
                    chartContainer.addComponent(chartComponent);

                } else if ("range".equals(question.getQuestionType())) {

                    ChartComponent chartComponent = createRangeChartFromDefinition(question);
                    chartContainer.addComponent(chartComponent);
                } /*else if ("text".equals(question.getQuestionType())) {

                    ChartComponent chartComponent = createFreeTextChartFromDefinition(question);
                    chartContainer.addComponent(chartComponent);

                }*/

                this.chartsContainer.addComponent(chartContainer);
                this.chartsContainer.addComponent(createCollapsablePanel(question));
                this.chartsContainer.addComponent(createChartSeparator());
            }
        }
    }


    private Label createChartSeparator() {

        Label separator = new Label();
        separator.setContentMode(ContentMode.HTML);
        separator.setSizeFull();
        separator.setHeight(100, Unit.PIXELS);

        return separator;
    }

    private Label createChartTitle(Question question) {

        Label title = new Label();
        title.setContentMode(ContentMode.HTML);
        title.setValue("<center><h3><b>" + question.getQuestion() + "</b></h3></center>");
        title.setSizeFull();

        return title;
    }

    private ChartDefinition getChartDefinitionForQuestion(Question question, String defaultChartId) {

        if (StringUtils.isNotBlank(question.getCustomChartId())) {
            ChartDefinition customChartDefinition = chartDefinitionRegistry.cloneChartDefinition(question.getCustomChartId());
            if (customChartDefinition != null) {
                return customChartDefinition;
            }
        }

        return chartDefinitionRegistry.cloneChartDefinition(defaultChartId);
    }

    private ChartComponent createSingleSelectChartFromDefinition(Question question) {

        ChartDefinition chartDefinition = getChartDefinitionForQuestion( question, "single-choice");

        if (StringUtils.isNotBlank(question.getCustomChartId())) {
            ChartDefinition customChartDefinition = chartDefinitionRegistry.cloneChartDefinition(question.getCustomChartId());
            if (customChartDefinition != null) {
               chartDefinition = customChartDefinition;
            }
        }

        AnalyticsModule analyticsModule = this.componentProvider.getComponent(AnalyticsModule.class);
        ResponseItemService responseItemService = this.componentProvider.getComponent(ResponseItemService.class);
        QuestionService questionService = this.componentProvider.getComponent(QuestionService.class);
        ResponseCountByAnswerDataSupplier dataSupplier = new ResponseCountByAnswerDataSupplier(analyticsModule, responseItemService, questionService);
        dataSupplier.setParameters(Collections.singletonMap(QUESTION_ID, question.getId() != null ? String.valueOf(question.getId()) : ""));
        dataSupplier.setName(String.valueOf(question.getId()));

        ChartComponent chartComponent = (ChartComponent) this.dashboardComposer.initialiseComponent(chartDefinition, dataSupplier, new HashMap<>());

        chartComponent.setHeight(600, Unit.PIXELS);

        return chartComponent;
    }

    private ChartComponent createMultiSelectChartFromDefinition(Question question) {

        ChartDefinition chartDefinition = getChartDefinitionForQuestion( question, "multi-choice");

        AnalyticsModule analyticsModule = this.componentProvider.getComponent(AnalyticsModule.class);
        ResponseItemService responseItemService = this.componentProvider.getComponent(ResponseItemService.class);
        QuestionService questionService = this.componentProvider.getComponent(QuestionService.class);
        ResponseCountByAnswerDataSupplier dataSupplier = new ResponseCountByAnswerDataSupplier(analyticsModule, responseItemService, questionService);
        dataSupplier.setParameters(Collections.singletonMap(QUESTION_ID, question.getId() != null ? String.valueOf(question.getId()) : ""));
        dataSupplier.setName(String.valueOf(question.getId()));

        ChartComponent chartComponent = (ChartComponent) this.dashboardComposer.initialiseComponent(chartDefinition, dataSupplier, new HashMap<>());

        chartComponent.setWidth(90, Unit.PERCENTAGE);
        chartComponent.setHeight(600, Unit.PIXELS);

        return chartComponent;
    }

    private ChartComponent createFreeTextChartFromDefinition(Question question) {

        ChartDefinition chartDefinition = getChartDefinitionForQuestion( question, "free-text");

        AnalyticsModule analyticsModule = this.componentProvider.getComponent(AnalyticsModule.class);
        ResponseItemService responseItemService = this.componentProvider.getComponent(ResponseItemService.class);
        FreeTextDataSupplier dataSupplier = new FreeTextDataSupplier(analyticsModule, responseItemService);
        dataSupplier.setParameters(Collections.singletonMap(QUESTION_ID, question.getId() != null ? String.valueOf(question.getId()) : ""));
        dataSupplier.setName(String.valueOf(question.getId()));

        ChartComponent chartComponent = (ChartComponent) this.dashboardComposer.initialiseComponent(chartDefinition, dataSupplier, new HashMap<>());

        chartComponent.setHeight(600, Unit.PIXELS);

        return chartComponent;
    }

    private ChartComponent createRangeChartFromDefinition(Question question) {

        ChartDefinition chartDefinition = getChartDefinitionForQuestion( question, "range");

        if (StringUtils.isNotBlank(question.getCustomChartId())) {
            ChartDefinition customChartDefinition = chartDefinitionRegistry.cloneChartDefinition(question.getCustomChartId());
            if (customChartDefinition != null) {
                chartDefinition = customChartDefinition;
            }
        }

        AnalyticsModule analyticsModule = this.componentProvider.getComponent(AnalyticsModule.class);
        ResponseItemService responseItemService = this.componentProvider.getComponent(ResponseItemService.class);
        QuestionService questionService = this.componentProvider.getComponent(QuestionService.class);
        ResponseAverageByAnswerDataSupplier dataSupplier = new ResponseAverageByAnswerDataSupplier(analyticsModule, responseItemService, questionService);
        dataSupplier.setParameters(Collections.singletonMap(QUESTION_ID, question.getId() != null ? String.valueOf(question.getId()) : ""));
        dataSupplier.setName(String.valueOf(question.getId()));

        ChartComponent chartComponent = (ChartComponent) this.dashboardComposer.initialiseComponent(chartDefinition, dataSupplier, new HashMap<>());

        chartComponent.setHeight(600, Unit.PIXELS);

        return chartComponent;
    }

    private Layout createMainGridView(Question question) {

        DataProvider<ResponseItem, String> dataProvider = DataProvider.fromFilteringCallbacks(
                // First callback fetches items based on a query
                query -> {
                    StringBuilder sortStr = new StringBuilder(StringUtils.EMPTY);

                    for (SortOrder<String> queryOrder : query.getSortOrders()) {
                        sortStr
                                .append(sortStr.length() > 0 ? " , " : " ")
                                .append(queryOrder.getSorted())
                                .append(" ")
                                .append(SortDirection.ASCENDING.name().equals(queryOrder.getDirection().name()) ? " asc " : " desc ");
                    }

                    // The index of the first item to load
                    int offset = query.getOffset();
                    // The number of items to load
                    int limit = query.getLimit();
                    // Filter criteria
                    Criterion criterion = buildCriterion(question, query.getFilter());
                    // Get items.
                    return responseItemService.list(offset, limit, criterion, sortStr.length() > 0 ? sortStr.toString() : null).stream();
                },
                // Second callback fetches the number of items for a query
                query -> {
                    // Filter criteria
                    Criterion criterion = buildCriterion(question, query.getFilter());

                    // Get count.
                    return responseItemService.count(criterion);
                }
        );

        ConfigurableFilterDataProvider<ResponseItem, Void, String> dataProviderFilterWrapper = dataProvider.withConfigurableFilter();

        Grid<ResponseItem> grid = new Grid<>();

        grid.addColumn(BaseModel::getCreated, new DateRenderer("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Locale.ENGLISH))
                .setCaption("Date")
                .setSortable(true)
                .setSortProperty("created")
                .setWidth(220);
        grid.addColumn(BaseModel::getCreatedBy)
                .setCaption("User")
                .setSortable(true)
                .setSortProperty("createdBy")
                .setWidth(220);
        Grid.Column valueColumn = grid.addColumn(ResponseItem::getValue)
                .setCaption("Answer")
                .setSortable(true)
                .setSortProperty("value");

        grid.setHeight(300, Unit.PIXELS);
        grid.setWidth(90, Unit.PERCENTAGE);
        grid.setSelectionMode(com.vaadin.ui.Grid.SelectionMode.NONE);
        grid.setResponsive(true);
        grid.setId(String.valueOf(question.getId()));

        grid.setDataProvider(dataProviderFilterWrapper);

        TextField searchField = new TextField();
        searchField.setWidth(90, Unit.PERCENTAGE);
        searchField.setPlaceholder("Start typing to search ...");
        searchField.addValueChangeListener(event -> {
            String filter = event.getValue();
            if (filter.trim().isEmpty()) {
                // null disables filtering
                filter = null;
            }

            dataProviderFilterWrapper.setFilter(filter);
        });

        HorizontalLayout detailsContainer = new HorizontalLayout();
        detailsContainer.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        detailsContainer.setSpacing(true);
        detailsContainer.setSizeFull();
        detailsContainer.addComponent(grid);

        VerticalLayout view = new VerticalLayout();
        view.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        view.setSpacing(true);
        view.setSizeFull();
        view.addComponent(searchField);
        view.addComponent(detailsContainer);

        return view;
    }

    private Criterion buildCriterion(Question question, Optional<String> filter) {

        Collection<Criterion> simpleCriterionList = new ArrayList<>();

        simpleCriterionList.add(SimpleCriterion.builder()
                .fieldName("question.id")
                .parameterKey("question.id")
                .operator(Operator.EQUAL)
                .values(Arrays.asList(question.getId()))
                .build());

        if (filter.isPresent()) {

            Collection<Criterion> orFilter = new ArrayList<>();

            orFilter.add(SimpleCriterion.builder()
                    .fieldName("createdBy")
                    .parameterKey("createdBy")
                    .operator(Operator.CONTAINS)
                    .values(Arrays.asList(filter.get()))
                    .build());

            orFilter.add(SimpleCriterion.builder()
                    .fieldName("created")
                    .parameterKey("created")
                    .operator(Operator.CONTAINS)
                    .values(Arrays.asList(filter.get()))
                    .build());

            orFilter.add(SimpleCriterion.builder()
                    .fieldName("value")
                    .parameterKey("value")
                    .operator(Operator.CONTAINS)
                    .values(Arrays.asList(filter.get()))
                    .build());

            simpleCriterionList.add(ComplexCriterion.builder()
                    .logicalOperator(LogicalOperator.OR)
                    .criteria(orFilter)
                    .build());
        }

        return ComplexCriterion.builder()
                .logicalOperator(LogicalOperator.AND)
                .criteria(simpleCriterionList)
                .build();
    }

    private Layout createFreeTextOptionGridView(Question question, AnswerOption answerOption) {

        DataProvider<ResponseItem, String> dataProvider = DataProvider.fromFilteringCallbacks(
                // First callback fetches items based on a query
                query -> {
                    StringBuilder sortStr = new StringBuilder(StringUtils.EMPTY);

                    for (SortOrder<String> queryOrder : query.getSortOrders()) {
                        sortStr
                                .append(sortStr.length() > 0 ? " , " : " ")
                                .append(queryOrder.getSorted())
                                .append(" ")
                                .append(SortDirection.ASCENDING.name().equals(queryOrder.getDirection().name()) ? " asc " : " desc ");
                    }

                    // The index of the first item to load
                    int offset = query.getOffset();
                    // The number of items to load
                    int limit = query.getLimit();
                    // Filter criteria
                    Criterion criterion = buildFreeTextOptionCriterion(question, query.getFilter(), answerOption.getValue());
                    // Get items.
                    return responseItemService.list(offset, limit, criterion, sortStr.length() > 0 ? sortStr.toString() : null).stream();
                },
                // Second callback fetches the number of items for a query
                query -> {
                    // Filter criteria
                    Criterion criterion = buildFreeTextOptionCriterion(question, query.getFilter(), answerOption.getValue());

                    // Get count.
                    return responseItemService.count(criterion);
                }
        );

        ConfigurableFilterDataProvider<ResponseItem, Void, String> dataProviderFilterWrapper = dataProvider.withConfigurableFilter();

        Grid<ResponseItem> grid = new Grid<>();

        grid.addColumn(BaseModel::getCreated, new DateRenderer("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", Locale.ENGLISH))
                .setCaption("Date")
                .setSortable(true)
                .setSortProperty("created")
                .setWidth(220);
        grid.addColumn(BaseModel::getCreatedBy)
                .setCaption("User")
                .setSortable(true)
                .setSortProperty("createdBy")
                .setWidth(220);
        grid.addColumn(ResponseItem::getFreeTextValue)
                .setCaption("Free text")
                .setSortable(true)
                .setSortProperty("freeTextValue");

        grid.setHeight(300, Unit.PIXELS);
        grid.setWidth(90, Unit.PERCENTAGE);
        grid.setSelectionMode(com.vaadin.ui.Grid.SelectionMode.NONE);
        grid.setResponsive(true);
        grid.setId(question.getId() + "-" + answerOption.getValue());

        grid.setDataProvider(dataProviderFilterWrapper);

        TextField searchField = new TextField();
        searchField.setWidth(90, Unit.PERCENTAGE);
        searchField.setPlaceholder("Start typing to search ...");
        searchField.addValueChangeListener(event -> {
            String filter = event.getValue();
            if (filter.trim().isEmpty()) {
                // null disables filtering
                filter = null;
            }

            dataProviderFilterWrapper.setFilter(filter);
        });

        HorizontalLayout detailsContainer = new HorizontalLayout();
        detailsContainer.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        detailsContainer.setSpacing(true);
        detailsContainer.setSizeFull();
        detailsContainer.addComponent(grid);

        VerticalLayout view = new VerticalLayout();
        view.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        view.setSpacing(true);
        view.setSizeFull();
        view.addComponent(searchField);
        view.addComponent(detailsContainer);

        return view;
    }

    private Criterion buildFreeTextOptionCriterion(Question question, Optional<String> filter, String optionValue) {

        Collection<Criterion> simpleCriterionList = new ArrayList<>();

        simpleCriterionList.add(SimpleCriterion.builder()
                .fieldName("question.id")
                .parameterKey("question.id")
                .operator(Operator.EQUAL)
                .values(Arrays.asList(question.getId()))
                .build());

        simpleCriterionList.add(SimpleCriterion.builder()
                .fieldName("value")
                .parameterKey("value")
                .operator(Operator.EQUAL)
                .values(Arrays.asList(optionValue))
                .build());

        if (filter.isPresent()) {

            Collection<Criterion> orFilter = new ArrayList<>();

            orFilter.add(SimpleCriterion.builder()
                    .fieldName("createdBy")
                    .parameterKey("createdBy")
                    .operator(Operator.CONTAINS)
                    .values(Arrays.asList(filter.get()))
                    .build());

            orFilter.add(SimpleCriterion.builder()
                    .fieldName("created")
                    .parameterKey("created")
                    .operator(Operator.CONTAINS)
                    .values(Arrays.asList(filter.get()))
                    .build());

            orFilter.add(SimpleCriterion.builder()
                    .fieldName("freeTextValue")
                    .parameterKey("freeTextValue")
                    .operator(Operator.CONTAINS)
                    .values(Arrays.asList(filter.get()))
                    .build());

            simpleCriterionList.add(ComplexCriterion.builder()
                    .logicalOperator(LogicalOperator.OR)
                    .criteria(orFilter)
                    .build());
        }

        return ComplexCriterion.builder()
                .logicalOperator(LogicalOperator.AND)
                .criteria(simpleCriterionList)
                .build();
    }

    private List<AnswerOption> getFreeTextAnswerOptions(Question question) {

        if (!Arrays.asList("single", "multi").contains(question.getQuestionType())) {
            return Collections.emptyList();
        }

        return question.getAnswerOptions()
                .stream()
                .filter(ao -> "showFreeText".equals(ao.getFreeText()))
                .collect(Collectors.toList());
    }

    private Layout createCollapsablePanel(Question question) {

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        mainLayout.setSizeFull();

        HorizontalLayout buttonsWrapper = new HorizontalLayout();

        mainLayout.addComponent(buttonsWrapper);

        Layout gridWrapper = createMainGridView(question);

        VerticalLayout hiddenContent = new VerticalLayout();
        hiddenContent.setSizeFull();
        hiddenContent.setVisible(false);
        hiddenContent.addComponent(gridWrapper);
        hiddenContent.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        mainLayout.addComponent(hiddenContent);

        Button button = new Button("Show Details", (Button.ClickListener) event -> {

            hiddenContent.setVisible(!hiddenContent.isVisible());

            if(hiddenContent.isVisible()) {
                event.getButton().setCaption("Hide Details");
            } else {
                event.getButton().setCaption("Show Details");
            }
        });

        buttonsWrapper.addComponent(button);

        List<AnswerOption> freeTextAnswerOptions = getFreeTextAnswerOptions(question);

        for (AnswerOption answerOption:freeTextAnswerOptions) {
            Layout optionGridWrapper = createFreeTextOptionGridView(question, answerOption);

            VerticalLayout optionHiddenContent = new VerticalLayout();
            optionHiddenContent.setSizeFull();
            optionHiddenContent.setVisible(false);
            optionHiddenContent.addComponent(optionGridWrapper);
            optionHiddenContent.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

            mainLayout.addComponent(optionHiddenContent);

            Button optionButton = new Button("Show " + answerOption.getLabel(), (Button.ClickListener) event -> {

                optionHiddenContent.setVisible(!optionHiddenContent.isVisible());

                if(optionHiddenContent.isVisible()) {
                    event.getButton().setCaption("Hide " + answerOption.getLabel());
                } else {
                    event.getButton().setCaption("Show " + answerOption.getLabel());
                }
            });

            buttonsWrapper.addComponent(optionButton);
        }

        return mainLayout;
    }

}
