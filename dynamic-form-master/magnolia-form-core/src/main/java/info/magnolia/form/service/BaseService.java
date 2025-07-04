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
package info.magnolia.form.service;

import info.magnolia.form.domain.BaseModel;
import info.magnolia.ui.jdbc.bean.filter.Criterion;
import info.magnolia.ui.jdbc.service.DatabaseService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import io.ebean.Database;

/**
 * Base service interface to implement for providing CRUD operations.
 * @param <T> BaseModel entity.
 */
public interface BaseService<T extends BaseModel> extends DatabaseService<T, UUID> {

    String DB_NAME = "form";

    Collection<T> list(Integer start, Integer limit, Criterion mainCriteria, String sort);

    /**
     * Provides DB object for form database.
     */
    Database getDb();

    T upsert(T entityToSaveOrUpdate);

    Class<T> getModelClass();

    T prepareForDbUpdate(T source, T destination);

    Collection<String> getChildrenEntityNames();

    T entityFromJson(String json);

    String entityToJson(T entity);

    T entityFromYaml(String yaml);

    String entityTypeFromYaml(String yaml);

    String entityToYaml(T entity);

    T importFromYaml(String yaml, BaseModel parent, String behaviour);

    void prepareForDbImport(T destination);

    void move(List<T> entities, BaseModel target, DropLocation dropLocation);

    void move(T entity, BaseModel target, DropLocation dropLocation);

/*    void throwExceptionIfIdExists(T entity) throws Exception;

    void throwIdExistsException(T entity) throws Exception;*/

}
