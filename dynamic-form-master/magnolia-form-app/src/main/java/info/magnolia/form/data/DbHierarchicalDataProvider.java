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
package info.magnolia.form.data;

import info.magnolia.form.app.FormDetailResultsBrowserSubApp;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.service.BaseService;
import info.magnolia.form.service.impl.AnswerOptionServiceImpl;
import info.magnolia.form.service.impl.FormServiceImpl;
import info.magnolia.form.service.impl.QuestionServiceImpl;
import info.magnolia.form.service.impl.SectionServiceImpl;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.filter.DataFilter;
import info.magnolia.ui.jdbc.bean.filter.ComplexCriterion;
import info.magnolia.ui.jdbc.bean.filter.Criterion;
import info.magnolia.ui.jdbc.bean.filter.LogicalOperator;
import info.magnolia.ui.jdbc.bean.filter.Operator;
import info.magnolia.ui.jdbc.bean.filter.SimpleCriterion;
import info.magnolia.ui.jdbc.data.JdbcHierarchicalDataProvider;
import info.magnolia.ui.jdbc.service.DatabaseServiceFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.vaadin.data.provider.HierarchicalQuery;

/**
 * Hierarchical data provider working with sql db services.
 */
public class DbHierarchicalDataProvider extends JdbcHierarchicalDataProvider<BaseModel, UUID> {

    private final BaseService service;
    private final DatabaseServiceFactory serviceFactory;
    private SubAppContext subAppContext;

    @Inject
    public DbHierarchicalDataProvider(DbDatasourceDefinition sourceDefinition, DatabaseServiceFactory serviceFactory, SubAppContext subAppContext) {
        super(sourceDefinition, serviceFactory);
        this.serviceFactory = serviceFactory;
        this.service = (BaseService) serviceFactory.getService(sourceDefinition.getEntityName());
        this.subAppContext = subAppContext;
    }

    @Override
    protected Stream<BaseModel> fetchChildrenFromBackEnd(HierarchicalQuery<BaseModel, DataFilter> query) {

        if (query.getParent() == null) {
            Collection results = this.service.list(null, null, this.buildFilters(query), this.getOrderColumnFromService(this.service));
            if (results != null) {
                return results.stream();
            }
        } else {

            Collection<BaseModel> result = new ArrayList<>();
            ((BaseService) this.serviceFactory.getService(query.getParent().getClass().getSimpleName())).getChildrenEntityNames().forEach(c -> {
                BaseService childrenService =  (BaseService) this.serviceFactory.getService((String) c);
                result.addAll(childrenService.list(null, null, this.buildChildrenFilter(query), this.getOrderColumnFromService(childrenService)));
            });

            return result.stream();
        }

        return Stream.empty();
    }

    private String getOrderColumnFromService(BaseService service) {

        String orderColumn = "id";

        if ( service instanceof FormServiceImpl ||
                service instanceof SectionServiceImpl ||
                service instanceof QuestionServiceImpl ||
                service instanceof AnswerOptionServiceImpl ) {

            orderColumn = "order_index";
        }

        return orderColumn;
    }

    @Override
    public boolean hasChildren(BaseModel item) {
        return this.serviceFactory.getService(item.getClass().getSimpleName()).hasChildren(item.getId());
    }

    @Override
    public int getChildCount(HierarchicalQuery<BaseModel, DataFilter> query) {

        if (query.getParent() == null) {
            return this.service.count(this.buildChildrenFilter(query));
        } else {

            return  ((BaseService) this.serviceFactory.getService(query.getParent().getClass().getSimpleName())).getChildrenEntityNames().stream()
                    .map(c -> this.serviceFactory.getService((String) c))
                    .mapToInt(childrenService -> ((BaseService) childrenService).count(this.buildChildrenFilter(query)))
                    .sum();
        }
    }

    @Override
    public BaseModel getParent(UUID id) {
        throw new UnsupportedOperationException();
    }

    public BaseModel getParent(BaseModel item) {
        if (item.getId() != null) {
            Optional<BaseModel> parent = this.serviceFactory.getService(item.getClass().getSimpleName()).getParent(item.getId());
            if (parent.isPresent()) {
                return parent.get();
            }
        }

        return null;
    }

    /**
     * Build the filters complex criterion.
     *
     * @param query The query
     * @return The filters complex criterion.
     */
    private ComplexCriterion buildFilters(HierarchicalQuery<BaseModel, DataFilter> query) {

        List<Criterion> filters = new ArrayList<>();

        if (query.getParent() != null) {

            filters.add(SimpleCriterion.builder()
                    .fieldName(query.getParent().getParentName())
                    .parameterKey(query.getParent().getParentName())
                    .values(Collections.singleton(query.getParent().getId()))
                    .operator(Operator.EQUAL)
                    .build());
        } else {

            // transferred from value context when subapp starts/data provider get initialized
            Optional<BaseModel> rootItem = getRootItem();
            if (rootItem.isPresent()) {
                filters.add(SimpleCriterion.builder()
                        .fieldName(rootItem.get().getParentName())
                        .parameterKey(rootItem.get().getParentName())
                        .values(Collections.singleton(rootItem.get().getId()))
                        .operator(Operator.EQUAL)
                        .build());
            }
        }

        if (query.getFilter().isPresent()) {
            DataFilter dataFilter = query.getFilter().get();

            dataFilter.getPropertyFilters().entrySet().forEach(entry -> {
                filters.add(SimpleCriterion.builder()
                        .fieldName(entry.getKey())
                        .parameterKey(entry.getKey())
                        .values(Collections.singleton(entry.getValue()))
                        .operator(Operator.STARTS_WITH)
                        .build());
            });
        }

        if (filters.size() == 0) {
            return null;
        }

        return ComplexCriterion.builder()
                .logicalOperator(LogicalOperator.AND)
                .criteria(filters)
                .build();
    }

    private ComplexCriterion buildChildrenFilter(HierarchicalQuery<BaseModel, DataFilter> query) {

        List<Criterion> filters = new ArrayList<>();

        if (query.getParent() != null) {

            filters.add(SimpleCriterion.builder()
                    .fieldName(query.getParent().getParentName())
                    .parameterKey(query.getParent().getParentName())
                    .values(Collections.singleton(query.getParent().getId()))
                    .operator(Operator.EQUAL)
                    .build());
        } else {
            return null;
        }

        return ComplexCriterion.builder()
                .logicalOperator(LogicalOperator.AND)
                .criteria(filters)
                .build();
    }

    private Optional<BaseModel> getRootItem() {

        if (subAppContext.getSubApp() != null && subAppContext.getSubApp() instanceof FormDetailResultsBrowserSubApp) {
            return ((FormDetailResultsBrowserSubApp) subAppContext.getSubApp()).getRootItem();
        }

        return Optional.empty();
    }
}
