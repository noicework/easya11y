package work.noice.easya11y;

import info.magnolia.cms.core.Path;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.registry.RegistrationException;
import info.magnolia.templating.functions.TemplatingFunctions;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Functions for working with forms from the easya11y workspace.
 */
@Singleton
public class easya11yFunctions {

    private final TemplatingFunctions templatingFunctions;
    private final Provider<TemplateDefinitionRegistry> templateDefinitionRegistryProvider;

    /**
     * Constructor.
     */
    @Inject
    public easya11yFunctions(TemplatingFunctions templatingFunctions,
            Provider<TemplateDefinitionRegistry> templateDefinitionRegistryProvider) {
        this.templatingFunctions = templatingFunctions;
        this.templateDefinitionRegistryProvider = templateDefinitionRegistryProvider;
    }

    /**
     * Get the managed area of a form.
     *
     * @param formNode the form node
     * @return the area node or null if not found
     */
    public Node getFormManagedArea(Node formNode) {
        try {
            if (formNode != null && formNode.hasNode("main")) {
                return formNode.getNode("main");
            }
        } catch (RepositoryException e) {
            // Log error
        }
        return null;
    }

    /**
     * Get the managed area definition of a form.
     *
     * @param templateId the template ID
     * @return the area definition or null if not found
     */
    public AreaDefinition getFormManagedAreaDefinition(String templateId) {
        if (templateId == null) {
            return null;
        }

        TemplateDefinition templateDefinition;
        try {
            templateDefinition = templateDefinitionRegistryProvider.get()
                    .getTemplateDefinition(templateId);
        } catch (RegistrationException e) {
            // Log error
            return null;
        }
        if (templateDefinition != null && templateDefinition.getAreas() != null
                && templateDefinition.getAreas().containsKey("main")) {
            return templateDefinition.getAreas().get("main");
        }
        return null;
    }
}