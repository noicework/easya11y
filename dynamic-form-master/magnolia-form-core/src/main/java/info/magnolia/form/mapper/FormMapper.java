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
import info.magnolia.form.domain.Form;
import info.magnolia.form.domain.FormLocalized;
import info.magnolia.form.dto.FormDto;
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
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * Form mapper.
 */
@Mapper(uses = { QuestionMapper.class, SectionMapper.class, FormLocalizedMapper.class })
public abstract class FormMapper {

    public static FormMapper INSTANCE = Mappers.getMapper(FormMapper.class);

    public abstract FormDto entityToDto(Form entity);

    @InheritInverseConfiguration
    public abstract Form dtoToEntity(FormDto dto);

    @InheritConfiguration
    public abstract List<FormDto> entityToDtoCollection(Collection<Form> entity);

    @AfterMapping
    void translate(Form entity, @MappingTarget FormDto dto) {

        LocaleResolver localeResolver = Components.getComponent(LocaleResolver.class);
        String locale = localeResolver.getCurrentLocaleStr();

        if (StringUtils.isNotBlank(locale)) {
            if(!LocaleResolver.LANG_ALL.equalsIgnoreCase(locale)) {
                if (entity.getLocalisations().size() > 0) {
                    Optional<FormLocalized> localized = entity.getLocalisations()
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
