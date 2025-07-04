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
package info.magnolia.form.action;

import info.magnolia.form.data.DbItemResolver;
import info.magnolia.form.domain.BaseModel;
import info.magnolia.form.service.BaseService;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.framework.util.TempFileStreamResource;

import java.nio.charset.Charset;
import java.util.Optional;

import javax.inject.Inject;

import com.vaadin.server.Page;

import lombok.SneakyThrows;

/**
 * Action for exporting form related entity from database;
 */
public class ExportDatabaseAction extends AbstractAction<ExportDatabaseActionDefinition> {

    private final static String EXTENSION = "yaml";

    private TempFileStreamResource tempFileStreamResource;
    private final ValueContext<BaseModel> valueContext;
    private final DbItemResolver itemResolver;

    @Inject
    public ExportDatabaseAction(ExportDatabaseActionDefinition definition,
                                ValueContext<BaseModel> valueContext,
                                DbItemResolver itemResolver) {
        super(definition);
        this.valueContext = valueContext;
        this.itemResolver = itemResolver;
    }

    @SneakyThrows
    public void execute() {

        if (valueContext.getSingle().isPresent()) {
            BaseModel item = valueContext.getSingle().get();
            BaseService service = (BaseService) itemResolver.getServiceByItemType(item.getClass().getSimpleName());
            if (service != null) {
                Optional<BaseModel> detailedItem = service.getById(item.getId());
                if (detailedItem.isPresent()) {
                    BaseModel entity = detailedItem.get();
                    String yaml = service.entityToYaml(entity);
                    this.tempFileStreamResource = new TempFileStreamResource();
                    this.tempFileStreamResource.getTempFileOutputStream().write(yaml.getBytes(Charset.forName("UTF-8")));
                    this.tempFileStreamResource.setFilename(entity.getId().toString() + "." + EXTENSION);
                    this.tempFileStreamResource.setMIMEType("application/" + EXTENSION);
                    Page.getCurrent().open(this.tempFileStreamResource, "", true);
                }
            }
        }

    }
}
