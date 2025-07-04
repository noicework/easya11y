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
import javax.persistence.JoinColumn;
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
 * Form entity bean.
 */
@Entity
@Table(name="forms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Form extends BaseModel {

    public static final String RELATION_NAME = "form_id";

    @Column
    String title;

    @Column(length = 2000)
    String description;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "form")
    @JoinColumn(name = RELATION_NAME)
    List<Section> sections;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "form")
    @JoinColumn(name = RELATION_NAME)
    List<Question> questions;

    @Column
    boolean anonymize;

    @Column
    boolean onlyAuthenticated;

    @Column
    boolean publicResults;

    /** Localization */
    @MapKey(name = "locale")
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "parent", targetEntity = FormLocalized.class)
    Map<String, FormLocalized> localizations = new BeanMap<>();

    /** Localization */
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "parent", targetEntity = FormLocalized.class)
    List<FormLocalized> localisations = new ArrayList<>();

    @Column(name="order_index")
    int orderIndex;

    @Column
    String image;

    @Column(length = 2000)
    String rules;

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
