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
package info.magnolia.form.main;

import io.ebean.annotation.Platform;
import io.ebean.dbmigration.DbMigration;

/**
 * Generate the DB Migration.
 */
public class GenerateDbMigration {

    /**
     * Run this method manually when you have completed some work and you consider it ready to be released.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        DbMigration dbMigration = DbMigration.create();
        dbMigration.setPlatform(Platform.H2);
        dbMigration.setPathToResources("../form/magnolia-form-core/src/main/resources/form-core");

        // dbMigration.setAddForeignKeySkipCheck(true);
        // dbMigration.setLockTimeout(10);

        dbMigration.addPlatform(Platform.POSTGRES, "pg");
        dbMigration.addPlatform(Platform.MYSQL, "mysql");
        dbMigration.addPlatform(Platform.SQLSERVER17, "ms");
        dbMigration.addPlatform(Platform.ORACLE, "orcl");

        // generate the migration ddl and xml
        dbMigration.generateMigration();
    }
}
