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
import info.magnolia.form.domain.Question;
import info.magnolia.form.domain.QuestionLocalized;
import info.magnolia.form.dto.QuestionDto;
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
 * Question mapper.
 */
@Mapper(uses = { AnswerMapper.class, QuestionLocalizedMapper.class })
public abstract class QuestionMapper {

    public static QuestionMapper INSTANCE = Mappers.getMapper(QuestionMapper.class);

    @Mapping(target = "sectionId", source = "section.id")
    @Mapping(target = "formId", source = "form.id")
    public abstract QuestionDto entityToDto(Question entity);

    @InheritInverseConfiguration
    public abstract Question dtoToEntity(QuestionDto dto);

    @InheritConfiguration
    public abstract List<QuestionDto> entityToDtoCollection(Collection<Question> entity);

    @AfterMapping
    void translate(Question entity, @MappingTarget QuestionDto dto) {

        LocaleResolver localeResolver = Components.getComponent(LocaleResolver.class);
        String locale = localeResolver.getCurrentLocaleStr();

        if (StringUtils.isNotBlank(locale)) {
            if(!LocaleResolver.LANG_ALL.equalsIgnoreCase(locale)) {
                if (entity.getLocalisations().size() > 0) {
                    Optional<QuestionLocalized> localized = entity.getLocalisations()
                            .stream()
                            .filter(e -> locale.equalsIgnoreCase(e.getLocale()))
                            .findFirst();
                    localized.ifPresent(l -> {
                        if (StringUtils.isNotBlank(l.getTitle())) {
                            dto.setTitle(l.getTitle());
                        }
                        if (StringUtils.isNotBlank(l.getQuestion())) {
                            dto.setQuestion(l.getQuestion());
                        }
                    });

                    dto.setLocalisations(Collections.emptyList());
                }
            }
        }
    }
}
