package work.noice.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import org.junit.Test;
import work.noice.easya11y.setup.MigrateScheduledScanJobPropertiesTask;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class easya11yVersionHandlerTest {

    @Test
    public void updateFrom151RunsTheScheduledJobMigrationIn152() {
        Version releaseVersion = Version.parseVersion("1.5.2");
        ModuleDefinition module = new ModuleDefinition(
            "easya11y",
            releaseVersion,
            null,
            easya11yVersionHandler.class
        );
        InstallContext context = (InstallContext) Proxy.newProxyInstance(
            InstallContext.class.getClassLoader(),
            new Class<?>[]{InstallContext.class},
            (proxy, method, args) -> "getCurrentModuleDefinition".equals(method.getName())
                ? module
                : null
        );

        List<Delta> deltas = new easya11yVersionHandler().getDeltas(
            context,
            Version.parseVersion("1.5.1")
        );

        assertEquals(1, deltas.size());
        assertEquals(releaseVersion, deltas.get(0).getVersion());
        assertTrue(deltas.get(0).getTasks().stream()
            .anyMatch(MigrateScheduledScanJobPropertiesTask.class::isInstance));
    }
}
