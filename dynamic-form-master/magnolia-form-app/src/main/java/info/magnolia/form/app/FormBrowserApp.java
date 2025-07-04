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
package info.magnolia.form.app;

import info.magnolia.context.MgnlContext;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.AppView;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.message.Message;
import info.magnolia.ui.contentapp.ContentBrowserSubApp;
import info.magnolia.ui.framework.app.BaseApp;
import info.magnolia.util.OptionalConsumer;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * App class for the forms management. Open several application
 * simultaneously.
 *
 * @author Magnolia International Ltd.
 */
public class FormBrowserApp extends BaseApp {

    private SimpleTranslator i18n;

    @Inject
    protected FormBrowserApp(AppContext appContext, AppView view, SimpleTranslator i18n) {
        super(appContext, view, i18n);
        this.i18n = i18n;
    }

    @Override
    public void start(Location location) {
        getView().setAppName(location.getAppName());

        List<SubAppDescriptor> subAppDescriptors = getSubAppDescriptorStream().collect(toList());

        subAppDescriptors.stream()
                .filter(subAppDescriptor -> !subAppDescriptor.isClosable())
                .map(SubAppDescriptor::getName)
                .map(subAppName -> getDefaultLocation(location, subAppName))
                .forEach(appContext::openSubApp);

        openSubApp(location, () -> subAppDescriptors.stream()
                .findFirst()
                .map(SubAppDescriptor::getName)
                .map(subAppName -> getDefaultLocation(location, subAppName))
                .ifPresent(l -> {
                    Message message = new Message();
                    message.setSubject(i18n.translate("ui-framework.openSubApp.url.subject", location.getSubAppId()));
                    message.setMessage(i18n.translate("ui-framework.openSubApp.url.message",
                            getRequestURL(location), getRequestURL(l), location.getAppName(), location.getSubAppId()));

                    appContext.sendLocalMessage(message);
                    appContext.openSubApp(l);
                }));
    }

    private ContentBrowserSubApp.BrowserLocation getDefaultLocation(Location location, String subAppName) {
        return new ContentBrowserSubApp.BrowserLocation(location.getAppName(), subAppName, location.getParameter());
    }

    private String getRequestURL(Location location) {
        return StringUtils.join(MgnlContext.getWebContext().getRequest().getRequestURL(), "#", location.toString());
    }

    private OptionalConsumer<SubAppDescriptor> openSubApp(Location location, Runnable unknownSubAppHandler) {
        String subAppId = ofNullable(location.getSubAppId()).orElse("null");
        return OptionalConsumer.of(getSubAppDescriptorStream().filter(s -> s.getName().equals(subAppId)).findFirst())
                .ifPresent(s -> appContext.openSubApp(location)).ifNotPresent(unknownSubAppHandler);
    }

    private Stream<SubAppDescriptor> getSubAppDescriptorStream() {
        return appContext.getAppDescriptor().getSubApps().values().stream();
    }
}
