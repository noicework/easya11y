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

import info.magnolia.objectfactory.Components;
import info.magnolia.form.domain.Section;
import info.magnolia.form.domain.SectionLocalized;
import info.magnolia.form.dto.SectionDto;
import info.magnolia.form.i18n.LocaleResolver;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * Section mapper.
 */
@Mapper(uses = { QuestionMapper.class, SectionLocalizedMapper.class })
public abstract class SectionMapper {

    public static SectionMapper INSTANCE = Mappers.getMapper(SectionMapper.class);

    @Mapping(target = "formId", source = "form.id")
    public abstract SectionDto entityToDto(Section entity);

    @InheritInverseConfiguration
    public abstract Section dtoToEntity(SectionDto dto);

    @InheritConfiguration
    public abstract List<SectionDto> entityToDtoCollection(Collection<Section> entity);

    @AfterMapping
    void translate(Section entity, @MappingTarget SectionDto dto) {

        LocaleResolver localeResolver = Components.getComponent(LocaleResolver.class);
        String locale = localeResolver.getCurrentLocaleStr();

        if (StringUtils.isNotBlank(locale)) {
            if(!LocaleResolver.LANG_ALL.equalsIgnoreCase(locale)) {
                if (entity.getLocalisations().size() > 0) {
                    Optional<SectionLocalized> localized = entity.getLocalisations()
                            .stream()
                            .filter(e -> locale.equalsIgnoreCase(e.getLocale()))
                            .findFirst();
                    localized.ifPresent(l -> {
                        if (StringUtils.isNotBlank(l.getTitle())) {
                            dto.setTitle(l.getTitle());
                        }
                        if (StringUtils.isNotBlank(l.getDescription())) {
                            dto.setDescription(l.getDescription());
                        }
                    });

                    dto.setLocalisations(Collections.emptyList());
                }
            }
        }
    }
}
