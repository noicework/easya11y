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
package info.magnolia.form.domain;

import info.magnolia.commands.CommandsManager;
import info.magnolia.commands.chain.Command;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SimpleContext;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.objectfactory.Components;
import info.magnolia.ui.jdbc.bean.DatabaseBean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import io.ebean.annotation.WhoCreated;
import io.ebean.annotation.WhoModified;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Base domain object with Id, version, whenCreated and whenModified.
 *
 * <p>
 * Extending Model to enable the 'active record' style.
 *
 * <p>
 * whenCreated and whenModified are generally useful for maintaining external search services (like
 * elasticsearch) and audit.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseModel extends Model implements DatabaseBean<UUID> {

    @Transient
    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private static final Logger LOG = LoggerFactory.getLogger(BaseModel.class);

    @Id
    UUID id;

    @Version
    Long version;

    @WhenCreated
    Date created;

    @WhoCreated
    String createdBy;

    @WhenModified
    Date modified;

    @WhoModified
    String modifiedBy;

    @PrePersist
    public void prePersist1() {
        triggerCommand();
    }

    @PostPersist
    public void postPersist1() {
        triggerCommand();
    }

    @PreUpdate
    public void preUpdate1() {
        triggerCommand();
    }

    @PostUpdate
    public void postUpdate1() {
        triggerCommand();
    }


    @PreRemove
    public void preRemove1() {
        triggerCommand();
    }

    @PostRemove
    public void postRemove1() {
        triggerCommand();
    }

    private void triggerCommand() {
        try {

            if (MgnlContext.hasInstance()) {
                MagnoliaConfigurationProperties systemProperties = Components.getComponent(MagnoliaConfigurationProperties.class);
                String listenersEnabled = systemProperties.getProperty("ebean.listeners.enabled");

                if ("true".equals(listenersEnabled)) {

                    CommandsManager commandsManager = Components.getComponent(CommandsManager.class);

                    String catalogName = StringUtils.uncapitalize(
                            this.getClass().getSimpleName());
                    String commandName = StringUtils.uncapitalize(
                            this.getClass().getMethod(
                                    Thread.currentThread().getStackTrace()[2].getMethodName()
                            ).getAnnotations()[0].annotationType().getSimpleName());
                    Command command = commandsManager.getCommand(catalogName, commandName);
                    if (command != null) {
                        SimpleContext context = new SimpleContext();
                        Map<String, Object> map = new HashMap<>();
                        map.put("data", this);
                        context.setMap(map);
                        context.setLocale(MgnlContext.getLocale());
                        command.execute(context);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("error triggering db event", e);
        }
    }

    public String getEditorCaption() {
        return StringUtils.EMPTY;
    }

    public String getStatusBarCaption() {
        return StringUtils.EMPTY;
    }

    public abstract String getParentName();
}
