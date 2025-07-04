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
package info.magnolia.form.endpoints.v1;

import info.magnolia.form.dto.FormDto;
import info.magnolia.form.dto.ResponseDto;
import info.magnolia.form.dto.ResponseItemDto;
import info.magnolia.form.functions.FormFunctions;
import info.magnolia.rest.AbstractEndpoint;
import info.magnolia.rest.EndpointDefinition;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Endpoint for accessing and manipulating comments.
 *
 * @param <D> The endpoint definition
 */
@Tag(name = "/forms/v1", description = "The forms API")
@Path("/forms/v1")
public class FormEndpoint<D extends EndpointDefinition> extends AbstractEndpoint<D> {

    private static final String STATUS_MESSAGE_OK = "OK";
    private static final String STATUS_MESSAGE_ERROR_OCCURRED = "Error occurred";
    private static final String STATUS_MESSAGE_NOT_FOUND = "Not found";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final FormFunctions formFunctions;

    @Inject
    public FormEndpoint(final D endpointDefinition,
                        final FormFunctions formFunctions) {
        super(endpointDefinition);
        this.formFunctions = formFunctions;
    }

    /**
     * Test endpoint.
     */
    @GET
    @Path("/forms/test")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "timestamp", description = "Returns timestamp for test")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = STATUS_MESSAGE_OK),
            @ApiResponse(responseCode = "500", description = STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response test() {
        return Response.ok("endpoint is working").build();
    }

    /**
     * Returns forms list.
     */
    @GET
    @Path("/forms/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "list", description = "Returns forms list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = STATUS_MESSAGE_OK),
            @ApiResponse(responseCode = "500", description = STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response list() {
        log.debug("Get forms endpoint call");
        Collection<FormDto> formDtos = formFunctions.getForms();
        log.debug("Returned [{}] forms", formDtos.size());
        return Response.ok(formDtos).build();
    }

    /**
     * Returns specific form.
     */
    @GET
    @Path("/forms/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "list", description = "Returns forms list.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = STATUS_MESSAGE_OK),
            @ApiResponse(responseCode = "404", description = STATUS_MESSAGE_NOT_FOUND),
            @ApiResponse(responseCode = "500", description = STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response get(final @PathParam("id") UUID id) {
        log.debug("Get form endpoint call");

        FormDto formDto = formFunctions.getForm(id);
        if (formDto == null) {
            log.debug("Form with id [{}] not found", id);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        log.debug("Returned form with id [{}]", id);
        return Response.ok(formDto).build();
    }

    /**
     * Post form results.
     */
    @POST
    @Path("/forms/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_FORM_URLENCODED})
    @Operation(summary = "list", description = "Saves form response")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = STATUS_MESSAGE_OK),
            @ApiResponse(responseCode = "404", description = STATUS_MESSAGE_NOT_FOUND),
            @ApiResponse(responseCode = "500", description = STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response response(final @PathParam("id") UUID id, final List<ResponseItemDto> formResponseItemDtoList) {
        log.debug("Post form response endpoint call");

        ResponseDto responseDto =  formFunctions.submitResponse(id, formResponseItemDtoList);

        log.debug("Response saved with id [{}]", responseDto.getId());

        responseDto.setItems(responseDto.getItems().stream().sorted(Comparator.comparing(ResponseItemDto::getValue)).collect(Collectors.toList()));
        return Response.ok(responseDto).build();
    }

    /**
     * Returns specific form results.
     */
    @GET
    @Path("/forms/{id}/results")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "list", description = "Returns form results.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = STATUS_MESSAGE_OK),
            @ApiResponse(responseCode = "404", description = STATUS_MESSAGE_NOT_FOUND),
            @ApiResponse(responseCode = "500", description = STATUS_MESSAGE_ERROR_OCCURRED)
    })
    public Response results(final @PathParam("id") UUID id) {
        log.debug("Get form results endpoint call");
        Collection<ResponseDto> responseDtos = formFunctions.getResults(id);
        log.debug("Returned form results for form with id [{}]", id);
        return Response.ok(responseDtos).build();
    }
}

