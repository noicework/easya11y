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
package info.magnolia.form.service.impl;

import info.magnolia.form.FormCoreModule;
import info.magnolia.form.domain.Form;
import info.magnolia.form.domain.Response;
import info.magnolia.form.domain.ResponseItem;
import info.magnolia.form.domain.query.QResponse;
import info.magnolia.form.service.IdNonExistsException;
import info.magnolia.form.service.ResponseItemService;
import info.magnolia.form.service.ResponseService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import io.ebean.Query;

/**
 * Response service implementation class.
 */
public class ResponseServiceImpl extends AbstractBaseServiceImpl<Response> implements ResponseService, IdNonExistsException<Response> {

    private final ResponseItemService responseItemService;

    @Inject
    public ResponseServiceImpl(final Provider<FormCoreModule> formModuleProvider,
                               final ResponseItemService responseItemService) {
        super(formModuleProvider);
        this.responseItemService = responseItemService;
    }

    @Override
    public Class<Response> getModelClass() {
        return Response.class;
    }

    @Override
    public String getEntityName() {
        return Response.class.getSimpleName();
    }

    @Override
    public Response prepareForDbUpdate(Response source, Response destination) {

        if (source == null || destination == null) {
            return null;
        }

        if (source.getForm() != null && source.getForm().getId() != null) {
            // set reference
            destination.setForm(this.getDb().reference(Form.class, source.getForm().getId()));
        } else {
            destination.setForm(null);
        }


        // remap response items
        destination.getItems().forEach(r -> {
            if (!source.getItems().stream().map((i -> i.getId())).collect(Collectors.toList()).contains(r.getId())) {
                this.getDb().delete(r);
            }
        });

        destination.setItems(source.getItems().stream()
                .map(i -> {
                    ResponseItem item;
                    if (i.getId() == null || i.getId() == null) {
                        item = new ResponseItem();
                    } else {
                        item = responseItemService.getById(i.getId()).get();
                    }
                    item = responseItemService.prepareForDbUpdate(i, item);
                    return item;
                })
                .collect(Collectors.toList()));

        if (source.getId() != null) {
            destination.setId(source.getId());
        }

        return destination;
    }

    @Override
    public Collection<String> getChildrenEntityNames() {
        return Arrays.asList(ResponseItem.class.getSimpleName());
    }

    @Override
    public boolean hasChildren(UUID id) {
        Optional<Response> entity = getById(id);
        if (entity.isPresent()) {
            return (entity.get().getItems().size() > 0);
        }
        return false;
    }

    @Override
    public Query<Response> eagerFetchFlatGraph(Query<Response> query) {
        query = query.fetch(QResponse.alias().items.toString());
        return query;
    }

    @Override
    public Query<Response> eagerFetchFullGraph(Query<Response> query) {
        query = query.fetch(QResponse.alias().form.toString());
        query = query.fetch(QResponse.alias().items.toString());
        query = query.fetch(QResponse.alias().items.question.toString());
        query = query.fetch(QResponse.alias().items.question.answerOptions.toString());
        return query;
    }
}
