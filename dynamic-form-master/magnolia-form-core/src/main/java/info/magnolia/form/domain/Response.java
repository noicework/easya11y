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


import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response entity bean.
 */
@Entity
@Table(name="responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Response extends BaseModel {

    public static final String RELATION_NAME = "response_id";

    @ManyToOne(optional=false)
    Form form;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "response")
    @JoinColumn(name = RELATION_NAME)
    List<ResponseItem> items;

    @Override
    public String getEditorCaption() {
        return String.valueOf(this.form.getEditorCaption());
    }

    @Override
    public String getStatusBarCaption() {
        return String.valueOf(this.form.getStatusBarCaption());
    }

    @Override
    public String getParentName() {
        return RELATION_NAME;
    }
}
