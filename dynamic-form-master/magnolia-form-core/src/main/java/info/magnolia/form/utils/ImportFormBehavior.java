/**
 * This file Copyright (c) 2022 Magnolia International
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
package info.magnolia.form.utils;

/**
 * The possible actions specified by the <code>formImportBehavior</code> parameter.
 */

public interface ImportFormBehavior {
    public static final String IMPORT_BEHAVIOR_NO_EXISTING = "no-existing";
    public static final String IMPORT_BEHAVIOR_GENERATE_NEW = "generate-new";
    public static final String IMPORT_BEHAVIOR_REPLACE_EXISTING = "replace-existing";
}
