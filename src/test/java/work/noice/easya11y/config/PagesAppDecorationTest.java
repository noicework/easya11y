package work.noice.easya11y.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.api.availability.ConfiguredAvailabilityDefinition;
import info.magnolia.ui.availability.JcrAvailabilityChecker;
import info.magnolia.ui.availability.rule.JcrNodeTypeRuleDefinition;
import info.magnolia.jcr.node2bean.TypeDescriptor;
import info.magnolia.transformer.ClassPropertyBasedTypeResolver;
import org.junit.Test;
import work.noice.easya11y.actions.RunAccessibilityCheckAction;

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

            assertFalse("Deprecated action implementationClass must not be configured",
                action.containsKey("implementationClass"));

            TypeDescriptor actionType = new TypeDescriptor();
            actionType.setType(ActionDefinition.class);
            Class<?> definitionClass = new ClassPropertyBasedTypeResolver()
                .resolveType(actionType, action, problem -> {
                    throw new AssertionError(problem);
                })
                .orElseThrow(() -> new AssertionError("Magnolia did not resolve action class"));

            assertTrue(ConfiguredActionDefinition.class.isAssignableFrom(definitionClass));
            assertBeanPropertiesExist(definitionClass, action.keySet());

            ConfiguredActionDefinition definition =
                (ConfiguredActionDefinition) definitionClass.getDeclaredConstructor().newInstance();
            assertEquals(RunAccessibilityCheckAction.class, definition.getImplementationClass());

            Map<String, Object> availability = mapValue(action, "availability");
            assertFalse("JCR node type filtering is a built-in availability rule",
                availability.containsKey("rules"));
            assertBeanPropertiesExist(ConfiguredAvailabilityDefinition.class, availability.keySet());

            Map<String, Object> nodeTypes = mapValue(availability, "nodeTypes");
            assertEquals(List.of("mgnl:page"), List.copyOf(nodeTypes.values()));

            Map<String, Object> actionbar = mapValue(subApp, "actionbar");
            Map<String, Object> sections = mapValue(actionbar, "sections");
            Map<String, Object> pageActions = mapValue(sections, "pageActions");
            Map<String, Object> groups = mapValue(pageActions, "groups");
            Map<String, Object> accessibilityGroup = mapValue(groups, "accessibilityGroup");
            List<Map<String, Object>> items = listValue(accessibilityGroup, "items");
            assertEquals(List.of("runAccessibilityCheck"), items.stream()
                .map(item -> (String) item.get("name"))
                .collect(Collectors.toList()));
        }
    }

    @Test
    public void magnoliaClasspathProvidesDefaultNodeTypeAvailability() throws Exception {
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

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listValue(Map<String, Object> parent, String key) {
        Object value = parent.get(key);
        assertTrue("Expected list at " + key + " but found " + value, value instanceof List);
        return (List<Map<String, Object>>) value;
    }
}
