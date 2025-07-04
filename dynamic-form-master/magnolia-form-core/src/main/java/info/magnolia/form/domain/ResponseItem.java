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


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response Item entity bean.
 */
@Entity
@Table(name = "response_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseItem extends BaseModel {

    public static final String RELATION_NAME = "response_item_id";

    @ManyToOne(optional=false)
    Question question;

    @Column(length = 2000)
    String value;

    @Column(length = 2000)
    String freeTextValue;

    @ManyToOne(optional = false)
    Response response;

    @Override
    public String getEditorCaption() {
        return String.valueOf(this.question.getEditorCaption());
    }

    @Override
    public String getStatusBarCaption() {
        return String.valueOf(this.question.getStatusBarCaption());
    }

    @Override
    public String getParentName() {
        return RELATION_NAME;
    }
}
