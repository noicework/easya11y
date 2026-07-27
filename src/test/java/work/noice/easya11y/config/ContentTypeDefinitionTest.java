package work.noice.easya11y.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import info.magnolia.types.ConfiguredContentTypeDefinition;
import info.magnolia.types.datasource.jcr.ConfiguredJcrDataSourceDefinition;
import info.magnolia.types.model.ConfiguredPropertyDefinition;
import info.magnolia.types.model.jcr.ConfiguredJcrModelDefinition;
import org.junit.Test;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ContentTypeDefinitionTest {

    private static final String CONTENT_TYPE_RESOURCE =
        "/easya11y/contentTypes/submissions.yaml";

    @Test
    public void submissionsUsesMagnolia64JcrContentTypeShape() throws Exception {
        Map<String, Object> contentType = readContentType();
        assertBeanPropertiesExist(ConfiguredContentTypeDefinition.class, contentType.keySet());
        assertFalse("nodeType belongs under model", contentType.containsKey("nodeType"));

        Map<String, Object> datasource = mapValue(contentType, "datasource");
        assertBeanPropertiesExist(ConfiguredJcrDataSourceDefinition.class, datasource.keySet());
        assertEquals("easya11y", datasource.get("workspace"));
        assertEquals(false, datasource.get("autoCreate"));

        Map<String, Object> model = mapValue(contentType, "model");
        assertBeanPropertiesExist(ConfiguredJcrModelDefinition.class, model.keySet());
        assertEquals("mgnl:submission", model.get("nodeType"));
        assertFalse("primaryType is not a Magnolia 6.4 model property",
            model.containsKey("primaryType"));
        assertFalse("supertypes is not a Magnolia 6.4 model property",
            model.containsKey("supertypes"));

        List<Map<String, Object>> properties = listValue(model, "properties");
        assertEquals(2, properties.size());
        for (Map<String, Object> property : properties) {
            assertBeanPropertiesExist(ConfiguredPropertyDefinition.class, property.keySet());
        }
        assertEquals(List.of("timestamp", "formPath"), properties.stream()
            .map(property -> (String) property.get("name"))
            .collect(Collectors.toList()));
    }

    private Map<String, Object> readContentType() throws Exception {
        try (InputStream input = getClass().getResourceAsStream(CONTENT_TYPE_RESOURCE)) {
            assertNotNull("Missing content type resource " + CONTENT_TYPE_RESOURCE, input);
            return new ObjectMapper(new YAMLFactory()).readValue(
                input,
                new TypeReference<Map<String, Object>>() {
                }
            );
        }
    }

    private void assertBeanPropertiesExist(Class<?> beanClass, Set<String> configuredProperties)
        throws Exception {
        Set<String> writableProperties = Stream.of(
                Introspector.getBeanInfo(beanClass).getPropertyDescriptors())
            .filter(descriptor -> descriptor.getWriteMethod() != null)
            .map(PropertyDescriptor::getName)
            .collect(Collectors.toSet());

        assertTrue(
            "Unsupported properties for " + beanClass.getName() + ": " + configuredProperties,
            writableProperties.containsAll(configuredProperties)
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
