package work.noice.easya11y.setup;

import info.magnolia.module.InstallContext;
import info.magnolia.module.scheduler.JobDefinition;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RegisterScheduledScanJobTaskTest {

    @Test
    public void installWritesOnlyRealJobDefinitionFields() throws Exception {
        JcrFixture fixture = new JcrFixture();

        new RegisterScheduledScanJobTask().doExecute(fixture.installContext());

        NodeState job = fixture.node(RegisterScheduledScanJobTask.JOB_PATH);
        assertNotNull(job);
        assertEquals(
            Map.of(
                "name", "Accessibility Scan",
                "description", "Automated accessibility scan for all pages",
                "catalog", "default",
                "command", "easya11y-serverSideScan",
                "cron", "0 0 9 ? * MON",
                "enabled", false
            ),
            job.properties
        );

        Set<String> jobDefinitionFields = Arrays.stream(JobDefinition.class.getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .map(java.lang.reflect.Field::getName)
            .collect(Collectors.toSet());
        assertTrue(
            "Task wrote properties that are not JobDefinition fields: " + job.properties.keySet(),
            jobDefinitionFields.containsAll(job.properties.keySet())
        );

        Set<String> writableProperties = Arrays.stream(
                Introspector.getBeanInfo(JobDefinition.class).getPropertyDescriptors())
            .filter(descriptor -> descriptor.getWriteMethod() != null)
            .map(PropertyDescriptor::getName)
            .collect(Collectors.toSet());
        assertTrue(writableProperties.containsAll(job.properties.keySet()));
        assertFalse(job.properties.containsKey("catalogName"));
        assertFalse(job.properties.containsKey("jobName"));
        assertFalse(job.properties.containsKey("active"));

        NodeState params = job.children.get("params");
        assertNotNull(params);
        assertTrue(jobDefinitionFields.contains("params"));
        assertEquals(
            Map.of(
                "pagePattern", "/",
                "wcagLevel", "AA",
                "maxPages", "50",
                "sendEmail", "true",
                "sendDigest", "true"
            ),
            params.properties
        );
    }

    @Test
    public void migrationReplacesLegacyPropertiesWithoutResettingCustomSettings() throws Exception {
        JcrFixture fixture = new JcrFixture();
        NodeState job = fixture.createPath(RegisterScheduledScanJobTask.JOB_PATH);
        job.properties.put("catalogName", "customCatalog");
        job.properties.put("jobName", "legacyName");
        job.properties.put("active", true);
        job.properties.put("cron", "0 30 6 ? * FRI");
        job.properties.put("command", "customCommand");
        NodeState params = job.addChild("params");
        params.properties.put("maxPages", "125");

        new MigrateScheduledScanJobPropertiesTask().doExecute(fixture.installContext());

        assertEquals("customCatalog", job.properties.get("catalog"));
        assertEquals(true, job.properties.get("enabled"));
        assertEquals("0 30 6 ? * FRI", job.properties.get("cron"));
        assertEquals("customCommand", job.properties.get("command"));
        assertEquals("125", params.properties.get("maxPages"));
        assertFalse(job.properties.containsKey("catalogName"));
        assertFalse(job.properties.containsKey("jobName"));
        assertFalse(job.properties.containsKey("active"));
    }

    @Test
    public void migrationPrefersExistingSupportedProperties() throws Exception {
        JcrFixture fixture = new JcrFixture();
        NodeState job = fixture.createPath(RegisterScheduledScanJobTask.JOB_PATH);
        job.properties.put("catalog", "supportedCatalog");
        job.properties.put("catalogName", "legacyCatalog");
        job.properties.put("enabled", false);
        job.properties.put("active", true);

        new MigrateScheduledScanJobPropertiesTask().doExecute(fixture.installContext());

        assertEquals("supportedCatalog", job.properties.get("catalog"));
        assertEquals(false, job.properties.get("enabled"));
        assertFalse(job.properties.containsKey("catalogName"));
        assertFalse(job.properties.containsKey("active"));
    }

    private static final class JcrFixture {
        private final NodeState root = new NodeState("", null);
        private final Session session = proxy(Session.class, this::handleSession);

        private JcrFixture() {
            createPath("/modules/scheduler/config");
        }

        private InstallContext installContext() {
            return proxy(InstallContext.class, (proxy, method, args) -> {
                if ("getConfigJCRSession".equals(method.getName())) {
                    return session;
                }
                return defaultValue(method.getReturnType());
            });
        }

        private Object handleSession(Object proxy, Method method, Object[] args)
            throws RepositoryException {
            if ("getNode".equals(method.getName())) {
                NodeState state = node((String) args[0]);
                if (state == null) {
                    throw new RepositoryException("Missing node " + args[0]);
                }
                return state.proxy;
            }
            if ("nodeExists".equals(method.getName())) {
                return node((String) args[0]) != null;
            }
            if ("getRootNode".equals(method.getName())) {
                return root.proxy;
            }
            return defaultValue(method.getReturnType());
        }

        private NodeState createPath(String absolutePath) {
            NodeState current = root;
            for (String segment : absolutePath.split("/")) {
                if (!segment.isEmpty()) {
                    NodeState child = current.children.get(segment);
                    if (child == null) {
                        child = new NodeState(segment, current);
                        current.children.put(segment, child);
                    }
                    current = child;
                }
            }
            return current;
        }

        private NodeState node(String absolutePath) {
            NodeState current = root;
            for (String segment : absolutePath.split("/")) {
                if (!segment.isEmpty()) {
                    current = current.children.get(segment);
                    if (current == null) {
                        return null;
                    }
                }
            }
            return current;
        }
    }

    private static final class NodeState implements InvocationHandler {
        private final String name;
        private final NodeState parent;
        private final Map<String, NodeState> children = new LinkedHashMap<>();
        private final Map<String, Object> properties = new LinkedHashMap<>();
        private final Node proxy;

        private NodeState(String name, NodeState parent) {
            this.name = name;
            this.parent = parent;
            this.proxy = proxy(Node.class, this);
        }

        private NodeState addChild(String childName) {
            return children.computeIfAbsent(childName, name -> new NodeState(name, this));
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws RepositoryException {
            String methodName = method.getName();
            if ("hasNode".equals(methodName)) {
                return children.containsKey(args[0]);
            }
            if ("getNode".equals(methodName)) {
                NodeState child = children.get(args[0]);
                if (child == null) {
                    throw new RepositoryException("Missing child node " + args[0]);
                }
                return child.proxy;
            }
            if ("addNode".equals(methodName)) {
                return addChild((String) args[0]).proxy;
            }
            if ("hasProperty".equals(methodName)) {
                return properties.containsKey(args[0]);
            }
            if ("getProperty".equals(methodName)) {
                String propertyName = (String) args[0];
                if (!properties.containsKey(propertyName)) {
                    throw new RepositoryException("Missing property " + propertyName);
                }
                return property(propertyName);
            }
            if ("setProperty".equals(methodName)) {
                String propertyName = (String) args[0];
                Object value = args[1];
                if (value == null) {
                    properties.remove(propertyName);
                    return null;
                }
                properties.put(propertyName, value);
                return property(propertyName);
            }
            if ("getName".equals(methodName)) {
                return name;
            }
            if ("getPath".equals(methodName)) {
                return path();
            }
            if ("getParent".equals(methodName)) {
                return parent == null ? null : parent.proxy;
            }
            return defaultValue(method.getReturnType());
        }

        private Property property(String propertyName) {
            return proxy(Property.class, (proxy, method, args) -> {
                if ("getString".equals(method.getName())) {
                    return String.valueOf(properties.get(propertyName));
                }
                if ("getBoolean".equals(method.getName())) {
                    Object value = properties.get(propertyName);
                    return value instanceof Boolean
                        ? value
                        : Boolean.parseBoolean(String.valueOf(value));
                }
                if ("isMultiple".equals(method.getName())) {
                    return false;
                }
                if ("remove".equals(method.getName())) {
                    properties.remove(propertyName);
                    return null;
                }
                if ("getName".equals(method.getName())) {
                    return propertyName;
                }
                return defaultValue(method.getReturnType());
            });
        }

        private String path() {
            if (parent == null) {
                return "/";
            }
            String parentPath = parent.path();
            return "/".equals(parentPath) ? parentPath + name : parentPath + "/" + name;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
            type.getClassLoader(),
            new Class<?>[]{type},
            handler
        );
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        return 0;
    }
}
