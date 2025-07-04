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

import info.magnolia.ui.datasource.DatasourceType;
import info.magnolia.ui.jdbc.data.JdbcDatasourceDefinition;

import java.util.Map;

import lombok.Data;

/**
 * Definition holds information about sql db datasource.
 *
 */
@Data
@DatasourceType("dbFormDatasource")
public class DbDatasourceDefinition extends JdbcDatasourceDefinition {

    private Map<String, String> filters;

    public DbDatasourceDefinition() {
        setName("forms");
    }
}
