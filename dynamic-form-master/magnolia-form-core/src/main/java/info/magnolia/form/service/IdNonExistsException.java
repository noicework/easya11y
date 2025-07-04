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
import info.magnolia.ui.jdbc.service.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

/**
 * IdNonExistsException interface should be used to throw Exception on import, if uuid exists in database.
 *
 * @param <T>
 */
public interface IdNonExistsException<T extends BaseModel> extends DatabaseService<T, UUID> {

    static final Logger LOG = LoggerFactory.getLogger(IdNonExistsException.class);

    default boolean logExceptionIfIdExists(T entity) {
        Optional<T> res = getById(entity.getId());
        if (res.isPresent()) {
            this.logIdExistsException(entity);
            return true;
        }
        return false;
    }

    default void logIdExistsException(T entity) {
        LOG.error("Entity of type: {} with uuid: {} exists in db.", entity.getClass().getSimpleName(), entity.getId());
    }
}
