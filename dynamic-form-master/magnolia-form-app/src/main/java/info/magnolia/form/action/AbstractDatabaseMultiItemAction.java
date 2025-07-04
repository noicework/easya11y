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
package info.magnolia.form.action;

import info.magnolia.form.domain.BaseModel;
import info.magnolia.ui.AlertBuilder;
import info.magnolia.ui.ValueContext;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ActionExecutionException;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Ordering;
import com.vaadin.ui.Notification;

/**
 * Abstract multi-item Action that defines the default behavior.
 *
 * @param <D> the action definition type
 */
public abstract class AbstractDatabaseMultiItemAction<D extends ActionDefinition> extends AbstractAction<D> {

    private final Logger log = LoggerFactory.getLogger(AbstractDatabaseMultiItemAction.class);

    private final List<BaseModel> items;
    private Map<BaseModel, Exception> failedItems;
    // the item that is currently BEING processed
    private BaseModel currentItem;


    protected AbstractDatabaseMultiItemAction(D definition, ValueContext<BaseModel> valueContext) {
        super(definition);
        this.items = valueContext.get().collect(Collectors.toList());
    }

    /**
     * Executes the action on ONE item.
     */
    protected abstract void executeOnItem(BaseModel item) throws Exception;

    /**
     * Returns the message to display, if the execution succeeds on ALL items. May return <code>null</code>,
     * if the implementing action handles the user notification on its own.
     */
    protected abstract String getSuccessMessage();

    /**
     * Returns the message to display, if the execution fails on at least ONE item. May return <code>null</code>,
     * if the implementing action handles the user notification on its own.
     */
    protected abstract String getFailureMessage();

    @Override
    public void execute() throws ActionExecutionException {
        failedItems = new LinkedHashMap<BaseModel, Exception>();

        for (BaseModel item : getSortedItems(getItemComparator())) {
            this.currentItem = item;
            try {
                executeOnItem(item);
            } catch (Exception ex) {
                failedItems.put(item, ex);
            }
        }
        this.currentItem = null;

        if (failedItems.isEmpty()) {
            String message = getSuccessMessage();
            if (StringUtils.isNotBlank(message)) {

                AlertBuilder.alert(message)
                        .withLevel(Notification.Type.TRAY_NOTIFICATION)
                        .withOkButtonCaption("OK")
                        .buildAndOpen();
            }
        } else {
            String message = getErrorNotification();
            if (StringUtils.isNotBlank(message)) {

                AlertBuilder.alert(message)
                        .withLevel(Notification.Type.ERROR_MESSAGE)
                        .withOkButtonCaption("OK")
                        .withBody(message)
                        .buildAndOpen();
            }
        }
    }

    protected String getErrorNotification() {
        String failureMessage = getFailureMessage();
        if (failureMessage != null) {
            StringBuilder notification = new StringBuilder(failureMessage);
            notification.append("<ul>");
            for (BaseModel item : failedItems.keySet()) {
                Exception ex = failedItems.get(item);
                notification.append("<li>").append("<b>");
                notification.append(String.valueOf(item.getId())).append("</b>: ").append(ex.getMessage());
                notification.append("</li>");
            }
            notification.append("</ul>");
            return notification.toString();
        }
        return null;
    }

    protected List<BaseModel> getItems() {
        return this.items;
    }

    /**
     * @return the sorted Items list based on the desired {@link Comparator}.
     */
    protected List<BaseModel> getSortedItems(Comparator<BaseModel> comparator) {
        return Ordering.from(comparator).sortedCopy(this.items);
    }

    protected Map<BaseModel, Exception> getFailedItems() {
        return this.failedItems;
    }

    /**
     * Returns the item that is currently <b>being</b> processed - i.e. <code>null</code> if the {@link #execute()} method is not running.
     */
    protected BaseModel getCurrentItem() {
        return this.currentItem;
    }

    /**
     * This method should be used <b>only in tests</b>.
     */
    protected void setCurrentItem(BaseModel item) {
        this.currentItem = item;
    }

    protected Comparator<BaseModel> getItemComparator() {
        return new Comparator<BaseModel>() {
            @Override
            public int compare(BaseModel o1, BaseModel o2) {
                return (o2.getClass().getSimpleName() + o2.getId()).compareTo(o1.getClass().getSimpleName() + o1.getId());
            }
        };
    }
}
