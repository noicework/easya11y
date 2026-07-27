package work.noice.easya11y.packaging;

import org.junit.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PackagedAxeCoreResourceIT {

    private static final String PACKAGED_RESOURCE =
        "easya11y/webresources/vendor/axe.min.js";

    @Test
    public void packagedJarContainsInstalledAxeCoreAtScannerResourcePath() throws Exception {
        Path artifact = Path.of(requiredProperty("packagedArtifact"));
        Path source = Path.of(requiredProperty("axeCoreSource"));

        assertTrue("Missing packaged artifact " + artifact, Files.isRegularFile(artifact));
        assertTrue("Missing installed axe-core source " + source, Files.isRegularFile(source));

        try (ZipFile jar = new ZipFile(artifact.toFile())) {
            ZipEntry entry = jar.getEntry(PACKAGED_RESOURCE);
            assertNotNull("Missing " + PACKAGED_RESOURCE + " in " + artifact, entry);

            try (InputStream packagedAxe = jar.getInputStream(entry)) {
                assertArrayEquals(
                    "The packaged resource must be the installed axe-core distribution",
                    Files.readAllBytes(source),
                    packagedAxe.readAllBytes()
                );
            }
        }
    }

    private String requiredProperty(String name) {
        String value = System.getProperty(name);
        assertNotNull("Missing system property " + name, value);
        return value;
    }
}
