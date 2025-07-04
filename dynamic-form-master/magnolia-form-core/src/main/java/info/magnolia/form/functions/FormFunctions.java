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
package info.magnolia.form.functions;

import info.magnolia.context.MgnlContext;
import info.magnolia.form.domain.Form;
import info.magnolia.form.dto.FormDto;
import info.magnolia.form.dto.ResponseDto;
import info.magnolia.form.dto.ResponseItemDto;
import info.magnolia.form.mapper.FormMapper;
import info.magnolia.form.mapper.ResponseMapper;
import info.magnolia.form.provider.MgnlCurrentUserProvider;
import info.magnolia.form.service.FormService;
import info.magnolia.form.service.ResponseService;
import info.magnolia.ui.jdbc.bean.filter.Criterion;
import info.magnolia.ui.jdbc.bean.filter.Operator;
import info.magnolia.ui.jdbc.bean.filter.SimpleCriterion;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An object exposing several methods useful for templates. It is exposed in templates as <code>formfn</code>.
 */
public class FormFunctions {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final FormService formService;
    private final ResponseService responseService;

    @Inject
    public FormFunctions(final FormService formService,
                         final ResponseService responseService) {
        this.formService = formService;
        this.responseService = responseService;
    }

    public List<FormDto> getForms() {
        Collection<Form> forms = formService.list(null, null, null, "order_index");
        return FormMapper.INSTANCE.entityToDtoCollection(forms);
    }

    public FormDto getForm(UUID id) {

        Optional<Form> form = formService.getById(id);
        if (!form.isPresent()) {
            return null;
        }
        return FormMapper.INSTANCE.entityToDto(form.get());
    }

    public ResponseDto submitResponse(final UUID id, final List<ResponseItemDto> formResponseItemDtoList) {
        Optional<Form> form = formService.getById(id);

        if (!form.isPresent()) {
            log.debug("Form with id [{}] not found", id);
            return null;
        }

        if (form.get().isOnlyAuthenticated()) {
            boolean isAnonymousUser = MgnlCurrentUserProvider.ANONYMOUS.equals(MgnlContext.getUser().getName());
            if (isAnonymousUser) {
                log.debug("Only authorized users allowed for form with id [{}]", id);
                return null;
            }
        }

        if (form.get().isAnonymize()) {
            // anonymize data in DB for this request
            MgnlContext.setAttribute(MgnlCurrentUserProvider.ANONYMIZE, Boolean.TRUE.toString());
        }

        ResponseDto formResponseDto = new ResponseDto();
        formResponseDto.setFormId(id);
        formResponseDto.setItems(formResponseItemDtoList);

        info.magnolia.form.domain.Response formResponse = ResponseMapper.INSTANCE.dtoToEntity(formResponseDto);
        info.magnolia.form.domain.Response response = responseService.save(formResponse);

        return ResponseMapper.INSTANCE.entityToDto(response);
    }

    public Collection<ResponseDto> getResults(UUID id) {

        Optional<Form> form = formService.getById(id);

        if (!form.isPresent()) {
            log.debug("Form with id [{}] not found", id);
            return Collections.EMPTY_LIST;
        }

        if (!form.get().isPublicResults()) {
            log.debug("Results are not public for form with id [{}]", id);
            return Collections.EMPTY_LIST;
        }

        Criterion criterion = SimpleCriterion.builder()
                .parameterKey("form_id")
                .fieldName("form_id")
                .operator(Operator.EQUAL)
                .values(Arrays.asList(id))
                .build();
        Collection<info.magnolia.form.domain.Response> responses = responseService.list(criterion);


        return ResponseMapper.INSTANCE.entityToDtoList(responses);
    }

    public UUID parseStringToUuid(String id) {

        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            // do nothing
        }
        return null;
    }
}

