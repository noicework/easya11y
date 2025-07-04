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


import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.ebean.annotation.DbJson;
import io.ebean.annotation.Index;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form entity bean.
 */
@Entity
@Table(name="forms_localized")
@Index(unique = true, columnNames = {"locale","parent_id"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FormLocalized extends BaseModel {

    public static final String RELATION_NAME = "parent_id";

    @Column
    String title;

    @Column(length = 2000)
    String description;

    @Column(nullable = false)
    private String locale;

    @ManyToOne(optional=false)
    Form parent;

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
