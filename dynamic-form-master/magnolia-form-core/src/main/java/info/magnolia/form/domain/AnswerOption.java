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
package info.magnolia.form.domain;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.ebean.annotation.DbJson;
import io.ebean.common.BeanMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Answer entity bean.
 */
@Entity
@Table(name="answer_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerOption extends BaseModel {

    public static final String RELATION_NAME = "answer_option_id";

    @Column
    String title;

    @Column(length = 2000)
    String label;

    @Column(length = 2000)
    String value;

    @Column
    String image;

    @Column
    String freeText;

    @Column
    String freeTextLabel;

    @ManyToOne(optional=false)
    Question question;

    /** Localization */
    @MapKey(name = "locale")
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "parent", targetEntity = AnswerOptionLocalized.class)
    private Map<String, AnswerOptionLocalized> localizations = new BeanMap<>();

    /** Localization */
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "parent", targetEntity = AnswerOptionLocalized.class)
    List<AnswerOptionLocalized> localisations = new ArrayList<>();

    @Column(name="order_index")
    int orderIndex;


    @Override
    public String getEditorCaption() {
        return this.title;
    }

    @Override
    public String getStatusBarCaption() {
        return this.title;
    }

    @Override
    public String getParentName() {
        return RELATION_NAME;
    }

    @DbJson
    Map<String,Object> content;
}
