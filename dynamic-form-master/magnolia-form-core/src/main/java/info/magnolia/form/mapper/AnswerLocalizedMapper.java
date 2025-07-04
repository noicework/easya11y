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

import info.magnolia.form.domain.AnswerOptionLocalized;
import info.magnolia.form.dto.AnswerOptionLocalizedDto;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Answer Localized mapper.
 */
@Mapper
public abstract class AnswerLocalizedMapper {

    public static AnswerLocalizedMapper INSTANCE = Mappers.getMapper(AnswerLocalizedMapper.class);

    public abstract AnswerOptionLocalizedDto entityToDto(AnswerOptionLocalized entity);

    @InheritInverseConfiguration
    public abstract AnswerOptionLocalized dtoToEntity(AnswerOptionLocalizedDto dto);
}
