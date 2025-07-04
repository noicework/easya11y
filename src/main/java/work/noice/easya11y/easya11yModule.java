package work.noice.easya11y;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main module class for the easya11y module.
 */
public class easya11yModule implements ModuleLifecycle {
    
    private static final Logger log = LoggerFactory.getLogger(easya11yModule.class);
    
    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        log.info("Starting easya11y module");
    }
    
    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        log.info("Stopping easya11y module");
    }
}
