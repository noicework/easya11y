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
package info.magnolia.form;

import info.magnolia.form.configuration.Datasource;
import info.magnolia.form.utils.DbUtil;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.ebean.migration.MigrationConfig;
import io.ebean.migration.MigrationRunner;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * This class is optional and represents the configuration for the forms-core module.
 * By exposing simple getter/setter/adder methods, this bean can be configured via content2bean
 * using the properties and node from <tt>config:/modules/forms-core</tt>.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 * See https://documentation.magnolia-cms.com/display/DOCS/Module+configuration for information about module configuration.
 */
public class FormCoreModule implements ModuleLifecycle {

    private static final Logger LOG = LoggerFactory.getLogger(FormCoreModule.class);
    private static final String DB_CONNECTION_ERROR_MESSAGE = "forms database connection can not be configured.";
    private static final String DB_CONNECTION_MISSING_PARAM_MESSAGE = "missing forms db configuration: {}";

    @Getter
    @Setter
    private Datasource datasource;
    @Getter
    @Setter
    private String currentUserProvider;

    @SneakyThrows
    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        LOG.info("start module {}", FormCoreModule.class.getSimpleName());

        Properties properties = DbUtil.loadEbeanProperties();

        if (StringUtils.isBlank(properties.getProperty(DbUtil.DATASOURCE_DB_USERNAME))) {
            LOG.error(DB_CONNECTION_MISSING_PARAM_MESSAGE, DbUtil.DATASOURCE_DB_USERNAME);
            LOG.error(DB_CONNECTION_ERROR_MESSAGE);
            return;
        }

        if (StringUtils.isBlank(properties.getProperty(DbUtil.DATASOURCE_DB_PASSWORD))) {
            LOG.error(DB_CONNECTION_MISSING_PARAM_MESSAGE, DbUtil.DATASOURCE_DB_PASSWORD);
            LOG.error(DB_CONNECTION_ERROR_MESSAGE);
            return;
        }

        if (StringUtils.isBlank(properties.getProperty(DbUtil.DATASOURCE_DB_URL))) {
            LOG.error(DB_CONNECTION_MISSING_PARAM_MESSAGE, DbUtil.DATASOURCE_DB_URL);
            LOG.error(DB_CONNECTION_ERROR_MESSAGE);
            return;
        }

        if (StringUtils.isBlank(properties.getProperty(DbUtil.DATASOURCE_DB_DRIVER))) {
            LOG.error(DB_CONNECTION_MISSING_PARAM_MESSAGE, DbUtil.DATASOURCE_DB_DRIVER);
            LOG.error(DB_CONNECTION_ERROR_MESSAGE);
            return;
        }

        if (!StringUtils.isBlank(properties.getProperty(DbUtil.PROPERTY_PREFIX + DbUtil.EBEAN_MIGRATION_RUN)) ||
                !StringUtils.isBlank(properties.getProperty(DbUtil.PROPERTY_PREFIX + DbUtil.EBEAN_MIGRATION_PATH))) {
            if (Boolean.parseBoolean(properties.getProperty(DbUtil.PROPERTY_PREFIX + DbUtil.EBEAN_MIGRATION_RUN))) {
                runMigrations(
                        properties.getProperty(DbUtil.DATASOURCE_DB_USERNAME),
                        properties.getProperty(DbUtil.DATASOURCE_DB_PASSWORD),
                        properties.getProperty(DbUtil.DATASOURCE_DB_URL),
                        properties.getProperty(DbUtil.DATASOURCE_DB_DRIVER),
                        properties.getProperty(DbUtil.PROPERTY_PREFIX + DbUtil.EBEAN_MIGRATION_PATH));
            }
        }

        try {
            DbUtil.loadEbeanDatabase();
        } catch (Exception e) {
            LOG.error("error loading ebean server on startup");
        }
    }

    private void runMigrations(final String username,
                               final String password,
                               final String url,
                               final String driver,
                               final String migrationScriptsPath) throws ClassNotFoundException {

        MigrationConfig config = new MigrationConfig();
        config.setDbUsername(username);
        config.setDbPassword(password);
        config.setDbUrl(url);
        config.setDbDriver(driver);

        Class.forName(driver);

        config.setMigrationPath(migrationScriptsPath);

        MigrationRunner runner = new MigrationRunner(config);

        runner.run();
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {

        LOG.info("stop module {}", FormCoreModule.class.getSimpleName());
        DbUtil.unloadEbeanServer();
    }
}
