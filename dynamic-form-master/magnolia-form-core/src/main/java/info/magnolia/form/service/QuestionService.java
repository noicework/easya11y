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

import info.magnolia.form.domain.Question;

/**
 * Question service should be used for Question CRUD operations.
 */
public interface QuestionService extends BaseService<Question>, IdNonExistsException<Question> {

}
