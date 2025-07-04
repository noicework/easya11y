package work.noice.easya11y.actions;

import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.location.DefaultLocation;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.action.OpenDetailSubappActionDefinition;

import javax.inject.Inject;
import javax.jcr.Node;

/**
 * Action to view submissions for a specific form in the easya11y app.
 */
public class ViewFormSubmissionsAction extends AbstractAction<OpenDetailSubappActionDefinition> {

    private final LocationController locationController;
    private final Node node;

    @Inject
    public ViewFormSubmissionsAction(OpenDetailSubappActionDefinition definition, 
                                    LocationController locationController,
                                    Node node) {
        super(definition);
        this.locationController = locationController;
        this.node = node;
    }

    @Override
    public void execute() throws ActionExecutionException {
        try {
            // Get the form node name
            String formName = node.getName();
            
            // Create location with form parameter
            Location location = new DefaultLocation("easya11y", "submissionsViewer", formName);
            
            // Navigate to the location
            locationController.goTo(location);
            
        } catch (Exception e) {
            throw new ActionExecutionException("Failed to open submissions viewer: " + e.getMessage(), e);
        }
    }
}