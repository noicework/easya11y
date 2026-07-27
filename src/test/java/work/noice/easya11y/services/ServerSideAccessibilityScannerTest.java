package work.noice.easya11y.services;

import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ServerSideAccessibilityScannerTest {

    @Test
    public void constructionDoesNotInitializeBrowserDependencies() throws Exception {
        CountingScanner scanner = new CountingScanner();

        assertEquals(0, scanner.axeLoadCount.get());
        assertEquals(0, scanner.driverSetupCount.get());

        try {
            scanner.scanUrl("https://example.test", "AA");
            fail("Expected test driver creation to stop the scan");
        } catch (ExpectedDriverCreationException expected) {
            // Initialization happens immediately before the first driver is created.
        }

        assertEquals(1, scanner.axeLoadCount.get());
        assertEquals(1, scanner.driverSetupCount.get());
    }

    @Test
    public void initializationIsThreadSafeAndIdempotent() throws Exception {
        CountingScanner scanner = new CountingScanner();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < 32; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    scanner.ensureInitialized();
                    return null;
                }));
            }

            start.countDown();
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }

        scanner.ensureInitialized();
        assertEquals(1, scanner.axeLoadCount.get());
        assertEquals(1, scanner.driverSetupCount.get());
    }

    @Test
    public void moduleKeepsOneLazyScannerInstance() throws Exception {
        try (InputStream input = getClass().getResourceAsStream(
            "/META-INF/magnolia/easya11y.xml")) {
            assertNotNull("Missing Magnolia module descriptor", input);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature(
                "http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(input);

            assertEquals("singleton", firstElementText(document, "scope"));
            assertEquals("true", firstElementText(document, "lazy"));
        }
    }

    private String firstElementText(Document document, String elementName) {
        NodeList elements = document.getElementsByTagName(elementName);
        assertEquals("Expected exactly one " + elementName + " element", 1, elements.getLength());
        return elements.item(0).getTextContent().trim();
    }

    private static final class CountingScanner extends ServerSideAccessibilityScanner {
        private final AtomicInteger axeLoadCount = new AtomicInteger();
        private final AtomicInteger driverSetupCount = new AtomicInteger();

        @Override
        String loadAxeCoreScript() {
            axeLoadCount.incrementAndGet();
            return "window.axe = {};";
        }

        @Override
        void setupChromeDriver() {
            driverSetupCount.incrementAndGet();
        }

        @Override
        ChromeDriver createWebDriver() {
            throw new ExpectedDriverCreationException();
        }
    }

    private static final class ExpectedDriverCreationException extends RuntimeException {
    }
}
