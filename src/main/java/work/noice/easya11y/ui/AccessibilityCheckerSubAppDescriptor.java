package work.noice.easya11y.ui;

import info.magnolia.ui.api.app.SubApp;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.action.ActionDefinition;
import java.util.Map;
import java.util.HashMap;

/**
 * Descriptor for the Accessibility Checker SubApp.
 * This loads the accessibility checker HTML interface in an iframe.
 */
public class AccessibilityCheckerSubAppDescriptor implements SubAppDescriptor {
    
    private String name = "accessibilityChecker";
    private String label = "Accessibility Checker";
    private boolean closable = false;
    private String icon = "icon-app";
    private String htmlPath = ".resources/easya11y/webresources/accessibility-checker.html";
    private Map<String, ActionDefinition> actions = new HashMap<>();
    
    public AccessibilityCheckerSubAppDescriptor() {
    }
    
    public String getHtmlPath() {
        return htmlPath;
    }
    
    public void setHtmlPath(String htmlPath) {
        this.htmlPath = htmlPath;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    @Override
    public boolean isClosable() {
        return closable;
    }
    
    public void setClosable(boolean closable) {
        this.closable = closable;
    }
    
    @Override
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    @Override
    public Map<String, ActionDefinition> getActions() {
        return actions;
    }
    
    public void setActions(Map<String, ActionDefinition> actions) {
        this.actions = actions;
    }
    
    @Override
    public Class<? extends SubApp> getSubAppClass() {
        return AccessibilityCheckerSubApp.class;
    }
}