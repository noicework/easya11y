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
package info.magnolia.form.mapper;

import info.magnolia.form.domain.Response;
import info.magnolia.form.dto.ResponseDto;

import java.util.Collection;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Form response mapper.
 */
@Mapper(uses = { ResponseItemMapper.class, FormMapper.class })
public abstract class ResponseMapper {

    public static ResponseMapper INSTANCE = Mappers.getMapper(ResponseMapper.class);

    @Mapping(target = "formId", source = "form.id")
    public abstract ResponseDto entityToDto(Response entity);

    @InheritInverseConfiguration
    public abstract Response dtoToEntity(ResponseDto dto);

    public abstract Collection<ResponseDto> entityToDtoList(Collection<Response> entity);
}
