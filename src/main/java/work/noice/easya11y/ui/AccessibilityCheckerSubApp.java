package work.noice.easya11y.ui;

import info.magnolia.ui.api.app.SubApp;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.view.View;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

import javax.inject.Inject;

/**
 * SubApp for the Accessibility Checker.
 * Displays the accessibility checker interface in an embedded iframe.
 */
public class AccessibilityCheckerSubApp implements SubApp, View {
    
    private final SubAppContext subAppContext;
    private final VerticalLayout layout;
    private final BrowserFrame frame;
    
    @Inject
    public AccessibilityCheckerSubApp(SubAppContext subAppContext) {
        this.subAppContext = subAppContext;
        
        this.layout = new VerticalLayout();
        this.layout.setSizeFull();
        this.layout.setMargin(false);
        this.layout.setSpacing(false);
        
        this.frame = new BrowserFrame();
        this.frame.setSizeFull();
        this.layout.addComponent(frame);
    }
    
    @Override
    public View start(Location location) {
        AccessibilityCheckerSubAppDescriptor descriptor = 
            (AccessibilityCheckerSubAppDescriptor) subAppContext.getSubAppDescriptor();
        String htmlPath = descriptor.getHtmlPath();
        
        // Get the context path dynamically
        String contextPath = VaadinService.getCurrentRequest().getContextPath();
        
        // Add context path if not already present
        if (!htmlPath.startsWith(contextPath + "/") && !htmlPath.startsWith("http")) {
            htmlPath = contextPath + "/" + (htmlPath.startsWith("/") ? htmlPath.substring(1) : htmlPath);
        }
        
        frame.setSource(new ExternalResource(htmlPath));
        return this;
    }
    
    @Override
    public String getCaption() {
        return subAppContext.getSubAppDescriptor().getLabel();
    }
    
    @Override
    public void locationChanged(Location location) {
        // Handle location changes if needed
    }
    
    @Override
    public View getView() {
        return this;
    }
    
    @Override
    public Component asVaadinComponent() {
        return layout;
    }
    
    @Override
    public String getSubAppId() {
        return subAppContext.getSubAppId();
    }
    
    @Override
    public boolean supportsLocation(Location location) {
        return true;
    }
    
    @Override
    public boolean isCloseable() {
        return subAppContext.getSubAppDescriptor().isClosable();
    }
    
    @Override
    public void stop() {
        // Clean up resources if needed
    }
}