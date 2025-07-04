package work.noice.easya11y.ui;

import info.magnolia.ui.api.app.SubApp;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.action.ActionDefinition;
import java.util.Collections;
import java.util.Map;

/**
 * Descriptor for the configuration viewer sub-app.
 */
public class ConfigurationSubAppDescriptor implements SubAppDescriptor {

    private String name;
    private String label;
    private String icon;
    private boolean closable = true;
    private String htmlPath = ".resources/easya11y/webresources/configuration.html";

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
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public boolean isClosable() {
        return closable;
    }

    public void setClosable(boolean closable) {
        this.closable = closable;
    }

    @Override
    public Class<? extends SubApp> getSubAppClass() {
        return ConfigurationSubApp.class;
    }
    
    @Override
    public Map<String, ActionDefinition> getActions() {
        return Collections.emptyMap();
    }

    public String getHtmlPath() {
        return htmlPath;
    }

    public void setHtmlPath(String htmlPath) {
        this.htmlPath = htmlPath;
    }
}