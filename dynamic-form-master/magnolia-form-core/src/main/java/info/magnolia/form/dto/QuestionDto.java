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
package info.magnolia.form.dto;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/**
 * Question dto.
 */
@Data
public class QuestionDto extends BaseDto {

    String title;
    String question;
    String questionType;
    @JsonSerialize(using=AssetSerializer.class)
    String image;
    Long rangeFrom;
    String rangeFromLabel;
    @JsonSerialize(using=AssetSerializer.class)
    String rangeFromImage;
    Long rangeTo;
    String rangeToLabel;
    @JsonSerialize(using=AssetSerializer.class)
    String rangeToImage;
    Set<AnswerOptionDto> answerOptions;
    UUID sectionId;
    UUID formId;
    int orderIndex;
    Map<String,Object> content;

    List<QuestionLocalizedDto> localisations;
}
