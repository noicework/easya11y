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
package info.magnolia.form.service.impl;

import info.magnolia.form.FormCoreModule;
import info.magnolia.form.domain.AnswerOption;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.domain.Form;
import info.magnolia.form.domain.Question;
import info.magnolia.form.domain.Section;
import info.magnolia.form.service.BaseService;
import info.magnolia.form.service.DropLocation;
import info.magnolia.form.service.IdNonExistsException;
import info.magnolia.form.utils.ImportFormBehavior;
import info.magnolia.form.utils.DbUtil;
import info.magnolia.ui.jdbc.bean.filter.ComplexCriterion;
import info.magnolia.ui.jdbc.bean.filter.Criterion;
import info.magnolia.ui.jdbc.bean.filter.LogicalOperator;
import info.magnolia.ui.jdbc.bean.filter.Operator;
import info.magnolia.ui.jdbc.bean.filter.OrderedColumn;
import info.magnolia.ui.jdbc.bean.filter.SimpleCriterion;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Provider;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Query;
import io.ebean.Transaction;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.Streams;

import lombok.SneakyThrows;

/**
 * Default service implementation for CRUD operations.
 * @param <T> BaseModel entity.
 */
public abstract class AbstractBaseServiceImpl<T extends BaseModel> implements BaseService<T> {

    private final Provider<FormCoreModule> formModuleProvider;

    protected AbstractBaseServiceImpl(Provider<FormCoreModule> formModuleProvider) {
        this.formModuleProvider = formModuleProvider;
    }

    @Override
    public List<T> list(Criterion mainCriteria) {
        return list(null, null, mainCriteria, (String) null);
    }

    @Override
    public List<T> list(Integer offset, Integer limit, Criterion mainCriteria) {
        return list(offset, limit, mainCriteria, (String) null);
    }

    @Override
    public List<T> list(Integer offset, Integer limit, Criterion mainCriteria, String sort) {
        PagedList<T> pagedForms = getPagedList(offset, limit, mainCriteria, sort);
        return pagedForms.getList();
    }

    private String convertOrderListToSortString(List<OrderedColumn> list) {
        StringBuilder sort = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sort.append(" ");
            sort.append(list.get(i).getFieldName());
            sort.append(" ");
            sort.append(list.get(i).getOrder().toString());
            if (i < list.size() - 1) {
                sort.append(", ");
            }
        }

        return sort.toString();
    }
    @Override
    public Collection<T> list(Criterion mainCriteria, List<OrderedColumn> list) {
        return list(null, null, mainCriteria, convertOrderListToSortString(list));
    }

    @Override
    public Collection<T> list(Integer offset, Integer limit, Criterion mainCriteria, List<OrderedColumn> list) {
        return list(offset, limit, mainCriteria, convertOrderListToSortString(list));
    }

    @Override
    public int count(Criterion mainCriteria) {

        PagedList<T> pagedForms = getPagedList(null, null, mainCriteria, null);

        return pagedForms.getTotalCount();
    }

    private PagedList<T> getPagedList(Integer offset, Integer limit, Criterion mainCriteria, String sort) {

        Query<T> query = getDb().find(getModelClass()).setDisableLazyLoading(true);

        query = eagerFetchFlatGraph(query);

        if (limit != null) {
            query = query.setMaxRows(limit);
        } else {
            query = query.setMaxRows(Integer.MAX_VALUE);
        }

        if (offset != null) {
            query = query.setFirstRow(offset);
        }

        query = query.orderById(true);

        if (StringUtils.isNotBlank(sort)) {
            query = query.orderBy(sort);
        }

        ExpressionList expressionList = query.where();

        expressionList = applyFilters(expressionList, mainCriteria);

        // with paged list we can get total number of rows, in case we need it.
        return expressionList.query().findPagedList();

        // we can call row count in a background thread
        // pagedForms.loadRowCount();
    }

    protected ExpressionList applyFilters(ExpressionList expression, Criterion criteria) {

        if (criteria != null) {
            if (!criteria.isEmpty()) {
                if (criteria.isSimple()) {
                    expression = applySimpleFilter(expression, criteria);
                } else {
                    expression = applyComplexFilter(expression, criteria);
                }
            }
        }

        return expression;
    }

    protected ExpressionList applyComplexFilter(ExpressionList expression, Criterion criteria) {

        ComplexCriterion complexCriterion = ((ComplexCriterion) criteria);
        LogicalOperator logicalOperator = complexCriterion.getLogicalOperator();

        if (!complexCriterion.isEmpty()) {
            if (LogicalOperator.OR.equals(logicalOperator)) {
                expression = expression.or();
            }

            for (Criterion criterion : complexCriterion.getCriteria()) {
                if (criterion.isSimple()) {
                    expression = applySimpleFilter(expression, criterion);
                } else {
                    expression = applyComplexFilter(expression, criterion);
                }
            }

            if (LogicalOperator.OR.equals(logicalOperator)) {
                expression = expression.endOr();
            }
        }

        return expression;
    }

    protected ExpressionList applySimpleFilter(ExpressionList expression, Criterion criteria) {

        SimpleCriterion simpleCriterion = ((SimpleCriterion) criteria);
        Operator operator = simpleCriterion.getOperator();

        if (!simpleCriterion.isEmpty()) {

            if ((simpleCriterion.getValues().toArray()[0] instanceof String) && (StringUtils.isEmpty((String)simpleCriterion.getValues().toArray()[0]))) {
                // do nothing
                return expression;
            }

            if (Operator.EQUAL.equals(operator)) {
                return expression.eq(simpleCriterion.getFieldName(), simpleCriterion.getValues().toArray()[0]);
            } else if (Operator.NOT_EQUAL.equals(operator)) {
                return expression.ne(simpleCriterion.getFieldName(), simpleCriterion.getValues().toArray()[0]);
            } else if (Operator.GREATER.equals(operator)) {
                return expression.gt(simpleCriterion.getFieldName(), simpleCriterion.getValues().toArray()[0]);
            } else if (Operator.GREATER_OR_EQUAL.equals(operator)) {
                return expression.ge(simpleCriterion.getFieldName(), simpleCriterion.getValues().toArray()[0]);
            } else if (Operator.LOWER.equals(operator)) {
                return expression.lt(simpleCriterion.getFieldName(), simpleCriterion.getValues().toArray()[0]);
            } else if (Operator.LOWER_OR_EQUAL.equals(operator)) {
                return expression.le(simpleCriterion.getFieldName(), simpleCriterion.getValues().toArray()[0]);
            } else if (Operator.IS_NULL.equals(operator)) {
                return expression.isNull(simpleCriterion.getFieldName());
            } else if (Operator.IS_NOT_NULL.equals(operator)) {
                return expression.isNotNull(simpleCriterion.getFieldName());
            } else if (Operator.STARTS_WITH.equals(operator)) {
                return expression.startsWith(simpleCriterion.getFieldName(), (String) simpleCriterion.getValues().toArray()[0]);
            } else if (Operator.ENDS_WITH.equals(operator)) {
                return expression.endsWith(simpleCriterion.getFieldName(), (String) simpleCriterion.getValues().toArray()[0]);
            } else if (Operator.CONTAINS.equals(operator)) {
                return expression.contains(simpleCriterion.getFieldName(), (String) simpleCriterion.getValues().toArray()[0]);
            } else if (Operator.IN.equals(operator)) {
                return expression.in(simpleCriterion.getFieldName(), simpleCriterion.getValues());
            } else if (Operator.NOT_IN.equals(operator)) {
                return expression.notIn(simpleCriterion.getFieldName(), simpleCriterion.getValues());
            }
        }

        return expression;
    }

    public Query<T> eagerFetchFlatGraph(Query<T> query) {
        // by default don't eager fetch related entities.
        return query;
    }

    public Query<T> eagerFetchFullGraph(Query<T> query) {
        // by default don't eager fetch related entities.
        return query;
    }

    @Override
    public Optional<T> getById(UUID id) {

        Query<T> query = getDb().find(getModelClass()).setDisableLazyLoading(true);
        // define related entities that should be eager loaded
        query = eagerFetchFullGraph(query);
        ExpressionList<T> expression = query.where();
        expression = expression.idEq(id);

        Optional<T> res = expression
                .query()
                .findOneOrEmpty();

        return res;
    }

    @SneakyThrows
    @Override
    public T newItem() {
        Constructor<?> ctor = getModelClass().getConstructor();
        return (T) ctor.newInstance(new Object[] {});
    }

    @Override
    public Optional getParent(UUID id) {
        return Optional.empty();
    }

    @Override
    public boolean hasChildren(UUID id) {
        return false;
    }

    @Override
    public T save(T entityToInsert) {

        try (Transaction transaction = getDb().beginTransaction()) {

            T entity = prepareForDbUpdate(entityToInsert, newItem());
            getDb().save(entity);

            transaction.commit();

            return entity;
        }
    }

    @Override
    public T update(T entityToUpdate) {

        try (Transaction transaction = getDb().beginTransaction()) {
            Optional<T> dbEntity = getById(entityToUpdate.getId());

            if (dbEntity.isPresent()) {

                T entity = prepareForDbUpdate(entityToUpdate, dbEntity.get());
                getDb().update(entity);

                transaction.commit();

                return entity;
            }
        }

        return null;
    }

    @Override
    public T upsert(T entityToSaveOrUpdate) {

        if (entityToSaveOrUpdate.getId() == null) {
            return save(entityToSaveOrUpdate);
        } else {
            return update(entityToSaveOrUpdate);
        }
    }

    @Override
    public void delete(UUID id) {

        try (Transaction transaction = getDb().beginTransaction()) {
            Optional<T> entityToDelete = getById(id);
            if (entityToDelete.isPresent()) {
                // delete form and all child forms
                getDb().delete(entityToDelete.get());
                transaction.commit();
            }
        }
    }

    /**
     * Provides DB object for forming database.
     */
    public Database getDb() {

        Database ebeanServer = DbUtil.database;
        if (ebeanServer != null) {
            return ebeanServer;
        }
        // return default server for tests
        return DB.getDefault();
    }

    @Override
    public Collection<String> getChildrenEntityNames() {
        return Collections.emptyList();
    }

    @Override
    public T entityFromJson(String json) {
        return getDb().json().toBean(getModelClass(), json);
    }

    @Override
    public String entityToJson(T entity) {
        return getDb().json().toJson(entity);
    }

    @SneakyThrows
    @Override
    public T entityFromYaml(String yaml) {
        // parse json from yaml
        JsonNode jsonNodeTree = new YAMLMapper().readTree(yaml);
        String json = new ObjectMapper().writeValueAsString(jsonNodeTree);
        return entityFromJson(json);
    }

    @SneakyThrows
    @Override
    public String entityTypeFromYaml(String yaml) {
        // parse json from yaml
        JsonNode jsonNodeTree = new YAMLMapper().readTree(yaml);
        return jsonNodeTree.get("entityType").asText();
    }

    @SneakyThrows
    @Override
    public String entityToYaml(T entity) {
        // convert json to yaml
        String json = entityToJson(entity);
        JsonNode jsonNodeTree = new ObjectMapper().readTree(json);
        ((ObjectNode) jsonNodeTree).put("entityType", getEntityName());
        return new YAMLMapper().writeValueAsString(jsonNodeTree);
    }

    @Override
    public void prepareForDbImport(T destination) {
        // do nothing
    }

    @SneakyThrows
    @Override
    public T importFromYaml(String yaml, BaseModel parent, String behaviour) {

        T entity = entityFromYaml(yaml);

        if (ImportFormBehavior.IMPORT_BEHAVIOR_GENERATE_NEW.equals(behaviour)) {
            // in this case we should init all id's to null or 0
            JsonNode jsonNodeTree = new YAMLMapper().readTree(yaml);
            generateNewIdsForJsonNode(jsonNodeTree);
            String json = new ObjectMapper().writeValueAsString(jsonNodeTree);
            entity = entityFromJson(json);
        }

        if (ImportFormBehavior.IMPORT_BEHAVIOR_NO_EXISTING.equals(behaviour)) {
            if (this instanceof IdNonExistsException) {
                if (((IdNonExistsException) this).logExceptionIfIdExists(entity)) {
                    return null;
                }
            }
        }

        if (parent == null) {
            if (!(entity instanceof Form)) {
                throw new UnsupportedOperationException(String.format("not allowed import of %s on ROOT level",
                        entity.getClass().getSimpleName()));
            }
        } else {
            if (parent instanceof Form) {
                if (!(entity instanceof Section) && !(entity instanceof Question)) {
                    throw new UnsupportedOperationException(String.format("not allowed import of %s on Form level",
                            entity.getClass().getSimpleName()));
                } else {
                    if (entity instanceof Section) {
                        ((Section) entity).setForm((Form) parent);
                    } else if (entity instanceof Question) {
                        ((Question) entity).setForm((Form) parent);
                        ((Question) entity).setSection(null);
                    }
                }
            } else if (parent instanceof Section) {
                if (!(entity instanceof Question)) {
                    throw new UnsupportedOperationException(String.format("not allowed import of %s on Section level",
                            entity.getClass().getSimpleName()));
                } else {
                    ((Question) entity).setForm(null);
                    ((Question) entity).setSection((Section) parent);
                }
            } else if (parent instanceof Question) {
                if (!(entity instanceof AnswerOption)) {
                    throw new UnsupportedOperationException(String.format("not allowed import of %s on Section level",
                            entity.getClass().getSimpleName()));
                } else {
                    ((AnswerOption) entity).setQuestion((Question) parent);
                }
            } else if (parent instanceof AnswerOption) {
                throw new UnsupportedOperationException(String.format("not allowed import of %s on Answer level",
                        entity.getClass().getSimpleName()));
            }
        }

        prepareForDbImport(entity);

        if (ImportFormBehavior.IMPORT_BEHAVIOR_REPLACE_EXISTING.equals(behaviour)) {
            return update(entity);
        }

        return save(entity);
    }

    private boolean isParent(BaseModel parent, BaseModel entity) {

        if (parent == null) {
            if (entity instanceof Form) {
                return true;
            }
        }

        if (parent instanceof Form) {
            if (entity instanceof Section) {
                return ((Form) parent).getSections().stream().anyMatch(s -> s.getId().equals(entity.getId()));
            } else if (entity instanceof Question) {
                return ((Form) parent).getQuestions().stream().anyMatch(s -> s.getId().equals(entity.getId()));
            }

        } else if (parent instanceof Section) {
            if ((entity instanceof Question)) {
                return ((Section) parent).getQuestions().stream().anyMatch(s -> s.getId().equals(entity.getId()));
            }
        } else if (parent instanceof Question) {
            if ((entity instanceof AnswerOption)) {
                ((AnswerOption) entity).setQuestion((Question) parent);
                return ((Question) parent).getAnswerOptions().stream().anyMatch(s -> s.getId().equals(entity.getId()));
            }
        }

        return false;
    }

    private void generateNewIdsForJsonNode(JsonNode root) {

        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();
            if (Streams.stream(fieldNames).anyMatch("id"::equalsIgnoreCase)) {
                ((ObjectNode) root).set("id", new TextNode(UUID.randomUUID().toString()));
            }
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);
                generateNewIdsForJsonNode(fieldValue);
            }
        } else if (root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode arrayElement = arrayNode.get(i);
                generateNewIdsForJsonNode(arrayElement);
            }
        }
    }

    @Override
    public void move(List<T> entities, BaseModel target, DropLocation dropLocation) {

        try (Transaction transaction = getDb().beginTransaction()) {
            for (T entity: entities) {
                Optional<T> entityToMove = getById(entity.getId());
                Optional<BaseModel> entityTarget = (Optional<BaseModel>) getById(target.getId());
                if (entityToMove.isPresent() && entityTarget.isPresent()) {
                    move(entityToMove.get(), entityTarget.get(), dropLocation);
                }
            }
            transaction.commit();
        }
    }

    @Override
    public void move(T entity, BaseModel target, DropLocation dropLocation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UUID parseStringToId(String sId) {
        return UUID.fromString(sId);
    }
}
