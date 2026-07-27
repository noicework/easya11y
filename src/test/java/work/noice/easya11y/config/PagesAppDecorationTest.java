package work.noice.easya11y.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import info.magnolia.ui.api.action.Action;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.availability.JcrAvailabilityChecker;
import info.magnolia.ui.availability.rule.JcrNodeTypeRuleDefinition;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PagesAppDecorationTest {

    private static final String DECORATION_RESOURCE =
        "/easya11y/decorations/pages-app/apps/pages-app.yaml";

    @Test
    public void decorationUsesMagnoliasDefaultActionAndNodeTypeAvailabilityStructure() throws Exception {
        Map<String, Object> decoration = readDecoration();

        assertNoTypeDiscriminators(decoration);

        Map<String, Object> subApps = mapValue(decoration, "subApps");
        for (String subAppName : List.of("browser", "detail")) {
            Map<String, Object> subApp = mapValue(subApps, subAppName);
            Map<String, Object> actions = mapValue(subApp, "actions");
            Map<String, Object> action = mapValue(actions, "runAccessibilityCheck");

            assertFalse("The default action definition does not need an explicit class",
                action.containsKey("class"));
            assertBeanPropertiesExist(ConfiguredActionDefinition.class, action.keySet());

            Class<?> implementationClass =
                Class.forName((String) action.get("implementationClass"));
            assertTrue(Action.class.isAssignableFrom(implementationClass));

            Map<String, Object> availability = mapValue(action, "availability");
            assertFalse("JCR node type filtering is a built-in availability rule",
                availability.containsKey("rules"));
            assertBeanPropertiesExist(ConfiguredAvailabilityDefinition.class, availability.keySet());

            Map<String, Object> nodeTypes = mapValue(availability, "nodeTypes");
            assertEquals(List.of("mgnl:page"), List.copyOf(nodeTypes.values()));
        }
    }

    @Test
    public void magnoliaClasspathProvidesTheDefaultsReliedOnByTheDecoration() throws Exception {
        assertEquals(
            ConfiguredActionDefinition.class.getName(),
            configuredImplementationFor(ActionDefinition.class.getName())
        );

        Field defaultRulesField = JcrAvailabilityChecker.class.getDeclaredField("RULE_DEFINITIONS");
        defaultRulesField.setAccessible(true);
        Collection<?> defaultRules = (Collection<?>) defaultRulesField.get(null);

        assertTrue(
            "JcrAvailabilityChecker must apply JcrNodeTypeRuleDefinition by default",
            defaultRules.stream().anyMatch(JcrNodeTypeRuleDefinition.class::isInstance)
        );
    }

    private Map<String, Object> readDecoration() throws Exception {
        try (InputStream input = getClass().getResourceAsStream(DECORATION_RESOURCE)) {
            assertNotNull("Missing decoration resource " + DECORATION_RESOURCE, input);
            return new ObjectMapper(new YAMLFactory()).readValue(
                input,
                new TypeReference<Map<String, Object>>() {
                }
            );
        }
    }

    private String configuredImplementationFor(String typeName) throws Exception {
        try (InputStream input = getClass().getResourceAsStream(
            "/META-INF/magnolia/ui-framework-core.xml")) {
            assertNotNull("Magnolia UI Framework type mappings are not on the test classpath", input);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature(
                "http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(input);
            NodeList mappings = document.getElementsByTagName("type-mapping");

            for (int i = 0; i < mappings.getLength(); i++) {
                org.w3c.dom.Node mapping = mappings.item(i);
                String type = childText(mapping, "type");
                if (typeName.equals(type)) {
                    return childText(mapping, "implementation");
                }
            }
        }

        throw new AssertionError("No Magnolia type mapping found for " + typeName);
    }

    private String childText(org.w3c.dom.Node parent, String childName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            org.w3c.dom.Node child = children.item(i);
            if (childName.equals(child.getNodeName())) {
                return child.getTextContent().trim();
            }
        }
        return null;
    }

    private void assertNoTypeDiscriminators(Object value) {
        if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) value;
            assertFalse("Magnolia decoration must not contain unresolved $type values",
                map.containsKey("$type"));
            map.values().forEach(this::assertNoTypeDiscriminators);
        } else if (value instanceof Collection<?>) {
            ((Collection<?>) value).forEach(this::assertNoTypeDiscriminators);
        }
    }

    private void assertBeanPropertiesExist(Class<?> beanClass, Set<String> configuredProperties)
        throws Exception {
        Set<String> writableProperties = Stream.of(
                Introspector.getBeanInfo(beanClass).getPropertyDescriptors())
            .filter(descriptor -> descriptor.getWriteMethod() != null)
            .map(PropertyDescriptor::getName)
            .collect(Collectors.toSet());

        Set<String> unsupportedProperties = configuredProperties.stream()
            .filter(name -> !name.startsWith("$"))
            .filter(name -> !"class".equals(name))
            .filter(name -> !writableProperties.contains(name))
            .collect(Collectors.toSet());

        assertTrue(
            "Unsupported properties for " + beanClass.getName() + ": " + unsupportedProperties,
            unsupportedProperties.isEmpty()
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Map<String, Object> parent, String key) {
        Object value = parent.get(key);
        assertTrue("Expected mapping at " + key + " but found " + value, value instanceof Map);
        return (Map<String, Object>) value;
    }
}
