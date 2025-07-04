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

import info.magnolia.form.FormCoreModule;
import info.magnolia.form.domain.AnswerOption;
import info.magnolia.form.domain.AnswerOptionLocalized;
import info.magnolia.form.domain.Form;
import info.magnolia.form.domain.FormLocalized;
import info.magnolia.form.domain.Question;
import info.magnolia.form.domain.QuestionLocalized;
import info.magnolia.form.domain.Response;
import info.magnolia.form.domain.ResponseItem;
import info.magnolia.form.domain.Section;
import info.magnolia.form.domain.SectionLocalized;
import info.magnolia.form.service.FormService;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.jdbc.service.DatabaseService;
import info.magnolia.ui.jdbc.service.DatabaseServiceFactory;

import java.util.Properties;
import java.util.UUID;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import io.ebean.PersistenceContextScope;

/**
 * DB service utility class.
 */
public class DbUtil {

    private static final Logger log = LoggerFactory.getLogger(DbUtil.class);
    public static final String PROPERTY_PREFIX = "magnolia.form.";
    public static final String DATASOURCE_DB_USERNAME = "datasource.db.username";
    public static final String DATASOURCE_DB_PASSWORD = "datasource.db.password";
    public static final String DATASOURCE_DB_URL = "datasource.db.url";
    public static final String DATASOURCE_DB_DRIVER = "datasource.db.driver";
    public static final String EBEAN_MIGRATION_PATH = "ebean.migration.path";
    public static final String EBEAN_MIGRATION_RUN = "ebean.migration.run";
    public static final String EBEAN_CURRENT_USER_PROVIDER = "ebean.currentUserProvider";

    public static final String CURRENT_USER_PROVIDER = "info.magnolia.form.provider.MgnlCurrentUserProvider";

    public static Database database;

    static {
        database = loadEbeanDatabase();
    }

    private DbUtil() {
        //Private constructor only
    }

    public static DatabaseService getServiceByItemTypeId(String id) {

        if (!id.contains("|")) {
            log.warn("Non valid id format '{}'", id);
            return null;
        }

        String[] idParts = id.split("[|]");
        if (idParts.length != 2) {
            log.warn("Non valid id format '{}'", id);
            return null;
        }

        try {
            UUID.fromString(idParts[1]);
        } catch (IllegalArgumentException exception) {
            log.warn("Non uuid id part '{}'", id);
            return null;
        }

        DatabaseServiceFactory serviceFactory = Components.getComponent(DatabaseServiceFactory.class);
        DatabaseService service = serviceFactory.getService(idParts[0]);

        if (service == null) {
            log.warn("No service instantiated for related type '{}'", idParts[0]);
            return null;
        }

        return service;
    }

    public static DatabaseService getServiceByItemType(String type) {
        DatabaseServiceFactory serviceFactory = Components.getComponent(DatabaseServiceFactory.class);
        DatabaseService service = serviceFactory.getService(type);

        if (service == null) {
            log.warn("No service instantiated for related type '{}'", type);
            return null;
        }

        return service;
    }

    public static DatabaseConfig loadServerConfig(Properties properties) {

        DatabaseConfig cfg = new DatabaseConfig();

        // load ebean magnolia properties
        cfg.loadFromProperties(properties);

        cfg.setName(FormService.DB_NAME);

        // add domain classes
        cfg.addClass(Form.class);
        cfg.addClass(FormLocalized.class);
        cfg.addClass(Section.class);
        cfg.addClass(SectionLocalized.class);
        cfg.addClass(Question.class);
        cfg.addClass(QuestionLocalized.class);
        cfg.addClass(AnswerOption.class);
        cfg.addClass(AnswerOptionLocalized.class);

        cfg.addClass(Response.class);
        cfg.addClass(ResponseItem.class);

        // don't register server and don't set as default
        // if db connection can not be established,
        // ebean throws error from which ebean can not recover
        // in that case magnolia instance needs to be restarted
        cfg.setDefaultServer(false);
        cfg.setRegister(false);

        // set jackson object mapper and register Java 8 Date Time mapping
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
        cfg.setObjectMapper(mapper);

        cfg.setPersistenceContextScope(PersistenceContextScope.QUERY);

        return cfg;
    }

    @SneakyThrows
    public static Properties loadEbeanProperties() {
        Properties properties = new Properties();

        // we'll run migrations manually before setting up ebean server
        // so that we don't have conflicts with other modules
        // keep ebean.migration.run property false
        properties.put(EBEAN_MIGRATION_RUN, String.valueOf(false));

        properties.put(EBEAN_CURRENT_USER_PROVIDER, CURRENT_USER_PROVIDER);


        FormCoreModule formCoreModule = null;
        try {
            formCoreModule = Components.getComponent(FormCoreModule.class);
        } catch (Exception e) {
            log.info("mgnl components are not loaded");
        }

        if (formCoreModule != null) {

            if (formCoreModule.getDatasource() != null) {
                if (formCoreModule.getDatasource().getUsername() != null) {
                    properties.put(DATASOURCE_DB_USERNAME, formCoreModule.getDatasource().getUsername());
                }
                if (formCoreModule.getDatasource().getPassword() != null) {
                    properties.put(DATASOURCE_DB_PASSWORD, formCoreModule.getDatasource().getPassword());
                }
                if (formCoreModule.getDatasource().getUrl() != null) {
                    properties.put(DATASOURCE_DB_URL, formCoreModule.getDatasource().getUrl());
                }
                if (formCoreModule.getDatasource().getDriver() != null) {
                    properties.put(DATASOURCE_DB_DRIVER, formCoreModule.getDatasource().getDriver());
                    Class.forName(formCoreModule.getDatasource().getDriver());
                }

                if (formCoreModule.getDatasource().getMigration() != null) {
                    properties.put(PROPERTY_PREFIX + EBEAN_MIGRATION_RUN, String.valueOf(formCoreModule.getDatasource().getMigration().isRun()));

                    if (formCoreModule.getDatasource().getMigration().getPath() != null) {
                        properties.put(PROPERTY_PREFIX + EBEAN_MIGRATION_PATH, formCoreModule.getDatasource().getMigration().getPath());
                    }
                }
            }
            if (formCoreModule.getCurrentUserProvider() != null) {
                properties.put(EBEAN_CURRENT_USER_PROVIDER, formCoreModule.getCurrentUserProvider());
            }
        }

        return properties;
    }

    public static Database loadEbeanDatabase() {

        unloadEbeanServer();

        Properties properties = loadEbeanProperties();
        DatabaseConfig cfg = loadServerConfig(properties);

        try {
            database = DatabaseFactory.create(cfg);
        } catch (Exception | NoClassDefFoundError e) {
            log.error("error connecting to forms database", e);
            database = null;
            return null;
        }

        log.info("Forms Ebean Server created.");
        return database;
    }

    public static void unloadEbeanServer() {

        try {
            if(database != null) {
                database.shutdown(true, false);
                database = null;
            }
        } catch (Exception | NoClassDefFoundError e) {
            log.error("ebean server can not be stopped.", e);
        }
    }
}
