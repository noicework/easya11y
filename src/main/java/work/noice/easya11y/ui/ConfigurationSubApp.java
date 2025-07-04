package work.noice.easya11y.ui;

import info.magnolia.ui.api.app.SubApp;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.view.View;

import javax.inject.Inject;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.server.VaadinService;

/**
 * SubApp implementation for displaying the configuration HTML page.
 */
public class ConfigurationSubApp implements SubApp {

    private final SubAppContext subAppContext;
    private final ConfigurationSubAppDescriptor descriptor;
    private ConfigurationView view;

    @Inject
    public ConfigurationSubApp(SubAppContext subAppContext, ConfigurationSubAppDescriptor descriptor) {
        this.subAppContext = subAppContext;
        this.descriptor = descriptor;
    }

    @Override
    public View start(Location location) {
        view = new ConfigurationView(descriptor.getHtmlPath());
        return view;
    }

    @Override
    public void locationChanged(Location location) {
        // Handle location changes if needed
    }

    @Override
    public String getCaption() {
        return descriptor.getLabel();
    }

    public SubAppContext getSubAppContext() {
        return subAppContext;
    }

    @Override
    public boolean isCloseable() {
        return descriptor.isClosable();
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public boolean supportsLocation(Location location) {
        return true; // This subapp supports all locations
    }

    @Override
    public String getSubAppId() {
        return descriptor.getName();
    }

    @Override
    public void stop() {
        // Clean up resources if needed
    }

    /**
     * View implementation that displays an HTML page in an iframe.
     */
    public static class ConfigurationView implements View {

        private final CssLayout layout;

        public ConfigurationView(String htmlPath) {
            layout = new CssLayout();
            layout.setSizeFull();
            layout.addStyleName("configuration-viewer");

            // Get the context path
            String contextPath = VaadinService.getCurrentRequest().getContextPath();
            String fullPath = contextPath + "/" + htmlPath;

            // Create an iframe to display the HTML content
            com.vaadin.ui.BrowserFrame frame = new com.vaadin.ui.BrowserFrame(null,
                    new com.vaadin.server.ExternalResource(fullPath));
            frame.setSizeFull();
            frame.addStyleName("configuration-viewer-frame");

            layout.addComponent(frame);
        }

        @Override
        public Component asVaadinComponent() {
            return layout;
        }
    }
}