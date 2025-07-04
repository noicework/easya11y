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

import info.magnolia.form.domain.ResponseItem;
import info.magnolia.form.dto.ResponseItemDto;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Form response item mapper.
 */
@Mapper(uses = { QuestionMapper.class })
public abstract class ResponseItemMapper {

    public static ResponseItemMapper INSTANCE = Mappers.getMapper(ResponseItemMapper.class);

    @Mapping(target = "questionId", source = "question.id")
    public abstract ResponseItemDto entityToDto(ResponseItem entity);

    @InheritInverseConfiguration
    public abstract ResponseItem dtoToEntity(ResponseItemDto dto);
}
