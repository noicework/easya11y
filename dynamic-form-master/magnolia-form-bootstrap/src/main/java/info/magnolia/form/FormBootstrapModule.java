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

import info.magnolia.form.service.BaseService;
import info.magnolia.form.utils.DbUtil;
import info.magnolia.form.utils.ImportFormBehavior;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.resourceloader.Resource;
import info.magnolia.resourceloader.ResourceOrigin;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * This class is optional and represents the configuration for the commenting-app module.
 * By exposing simple getter/setter/adder methods, this bean can be configured via content2bean
 * using the properties and node from <tt>config:/modules/commenting-app</tt>.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 * See https://documentation.magnolia-cms.com/display/DOCS/Module+configuration for information about module configuration.
 */
public class FormBootstrapModule implements ModuleLifecycle {
    private static final Logger LOG = LoggerFactory.getLogger(FormBootstrapModule.class);

    @Getter
    @Setter
    private String bootstrapFolderLocation;

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        bootstrap(bootstrapFolderLocation, ImportFormBehavior.IMPORT_BEHAVIOR_NO_EXISTING);
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {

    }

    private void bootstrap(String resourcePath, String importBehavior) {

        try {
            ResourceOrigin resourceOrigin = Components.getComponent(ResourceOrigin.class);
            Resource bootstrapResource = resourceOrigin.getByPath(resourcePath);

            if (bootstrapResource.isFile()) {
                bootstrapFile(bootstrapResource, importBehavior);
            } else {
                bootstrapResource.streamChildren().forEach(res -> {
                    if (res.isFile()) {
                        bootstrapFile(res, importBehavior);
                    }
                });
            }
        } catch (Exception e) {
            LOG.error("Can't bootstrap resource at path '{}'", resourcePath, e);
        }
    }

    private void bootstrapFile(Resource res, String importBehavior) {

        try {
            final InputStream stream = res.openStream();
            String yaml = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            BaseService formService = (BaseService) DbUtil.getServiceByItemType("Form");
            String entityType = formService.entityTypeFromYaml(yaml);
            BaseService service = (BaseService) DbUtil.getServiceByItemType(entityType);
            service.importFromYaml(yaml, null, importBehavior);
        } catch (Exception e) {
            LOG.error("Can't bootstrap resource at {}", res.getPath(), e);
        }
    }
}
