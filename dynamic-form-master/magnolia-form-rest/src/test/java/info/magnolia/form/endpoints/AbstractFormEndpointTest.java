/**
 * This file Copyright (c) 2021 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.form.endpoints;

import static org.mockito.Mockito.*;

import info.magnolia.context.MgnlContext;
import info.magnolia.dam.api.Asset;
import info.magnolia.dam.api.AssetProvider;
import info.magnolia.dam.api.AssetProviderRegistry;
import info.magnolia.dam.api.ItemKey;
import info.magnolia.dam.api.metadata.AssetMetadata;
import info.magnolia.dam.api.metadata.MagnoliaAssetMetadata;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.form.endpoints.v1.FormEndpoint;
import info.magnolia.form.functions.FormFunctions;
import info.magnolia.form.i18n.DefaultLocaleResolver;
import info.magnolia.form.i18n.LocaleResolver;
import info.magnolia.form.service.AnswerOptionLocalizedService;
import info.magnolia.form.service.AnswerOptionService;
import info.magnolia.form.service.FormLocalizedService;
import info.magnolia.form.service.FormService;
import info.magnolia.form.service.QuestionLocalizedService;
import info.magnolia.form.service.QuestionService;
import info.magnolia.form.service.ResponseItemService;
import info.magnolia.form.service.ResponseService;
import info.magnolia.form.service.SectionLocalizedService;
import info.magnolia.form.service.SectionService;
import info.magnolia.form.service.impl.AnswerOptionLocalizedServiceImpl;
import info.magnolia.form.service.impl.AnswerOptionServiceImpl;
import info.magnolia.form.service.impl.FormLocalizedServiceImpl;
import info.magnolia.form.service.impl.FormServiceImpl;
import info.magnolia.form.service.impl.QuestionLocalizedServiceImpl;
import info.magnolia.form.service.impl.QuestionServiceImpl;
import info.magnolia.form.service.impl.ResponseItemServiceImpl;
import info.magnolia.form.service.impl.ResponseServiceImpl;
import info.magnolia.form.service.impl.SectionLocalizedServiceImpl;
import info.magnolia.form.service.impl.SectionServiceImpl;
import info.magnolia.rest.registry.ConfiguredEndpointDefinition;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.TestMagnoliaConfigurationProperties;
import info.magnolia.test.mock.MockContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.Dispatcher;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import lombok.SneakyThrows;


public abstract class AbstractFormEndpointTest {

    private static final String MOCK_ASSET_PROVIDER_ID = "form-tests";
    public static Dispatcher dispatcher;

    protected static Injector injector;

    @SneakyThrows
    public static void setUpEnvironment() {

        injector = Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
                bind(AnswerOptionService.class).to(AnswerOptionServiceImpl.class);
                bind(AnswerOptionLocalizedService.class).to(AnswerOptionLocalizedServiceImpl.class);
                bind(QuestionService.class).to(QuestionServiceImpl.class);
                bind(QuestionLocalizedService.class).to(QuestionLocalizedServiceImpl.class);
                bind(SectionService.class).to(SectionServiceImpl.class);
                bind(SectionLocalizedService.class).to(SectionLocalizedServiceImpl.class);
                bind(FormService.class).to(FormServiceImpl.class);
                bind(FormLocalizedService.class).to(FormLocalizedServiceImpl.class);
                bind(ResponseService.class).to(ResponseServiceImpl.class);
                bind(ResponseItemService.class).to(ResponseItemServiceImpl.class);

            }
        });

        MockContext ctx = new MockContext();
        MgnlContext.setInstance(ctx);
        ComponentsTestUtil.setInstance(LocaleResolver.class, new DefaultLocaleResolver());

        AssetProviderRegistry assetProviderRegistry = mock(AssetProviderRegistry.class);
        ComponentsTestUtil.setInstance(AssetProviderRegistry.class, assetProviderRegistry);
        AssetProvider assetProvider = mock(AssetProvider.class);
        when(assetProviderRegistry.getProviderFor(any())).thenReturn(assetProvider);

        Asset asset = mockAsset("Talking to yourself to others");
        MagnoliaAssetMetadata metadata = addMockMetadata(asset, MagnoliaAssetMetadata.class);
        when(assetProvider.getAsset(any())).thenReturn(asset);
        ComponentsTestUtil.setInstance(MagnoliaConfigurationProperties.class, new TestMagnoliaConfigurationProperties());

        ConfiguredEndpointDefinition configuredEndpointDefinition = new ConfiguredEndpointDefinition();
        FormService formService = injector.getInstance(FormService.class);
        ResponseService responseService = injector.getInstance(ResponseService.class);

        FormFunctions formFunctions = new FormFunctions(formService, responseService);

        FormEndpoint formEndpoint = new FormEndpoint(configuredEndpointDefinition, formFunctions);

        dispatcher = MockDispatcherFactory.createDispatcher();
        dispatcher.getRegistry().addSingletonResource(formEndpoint);
    }

    @BeforeClass
    public static void setUp() {
        setUpEnvironment();
    }

    @AfterClass
    public static void tearDown(){
        injector = null;
    }

    @Test
    public void test() throws URISyntaxException, UnsupportedEncodingException {
        MockHttpRequest request = MockHttpRequest.get("/forms/v1/forms/test");
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("endpoint is working", response.getContentAsString());
    }


    public static void traverseAndRemoveProperties(JsonNode root) {
        traverseAndRemoveProperties(root, Arrays.asList("created", "modified"));
    }

    public static void traverseAndRemoveProperties(JsonNode root, List<String> propertyNames) {

        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();
            List<String> fieldsToRemove = new ArrayList<>();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);

                if (fieldValue.isObject() || fieldValue.isArray()) {
                    traverseAndRemoveProperties(fieldValue, propertyNames);
                }

                if (propertyNames.contains(fieldName)) {
                    fieldsToRemove.add(fieldName);
                }
            }

            fieldsToRemove.forEach(f -> ((ObjectNode) root).remove(f));

        } else if (root.isArray()) {
            ArrayNode arrayNode = (ArrayNode) root;
            for (int i = 0; i < arrayNode.size(); i++) {
                JsonNode arrayElement = arrayNode.get(i);
                traverseAndRemoveProperties(arrayElement, propertyNames);
            }
        }
    }

    public static JsonNode parseJsonResponse(String jsonString) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonNode = mapper.readTree(jsonString);

        AbstractFormEndpointTest.traverseAndRemoveProperties(jsonNode);

        return jsonNode;
    }

    public static JsonNode parseJsonResponse(String jsonString, List<String> propertyNames) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonNode = mapper.readTree(jsonString);

        AbstractFormEndpointTest.traverseAndRemoveProperties(jsonNode, propertyNames);

        return jsonNode;
    }

    private static Asset mockAsset(String name) {
        return mockAsset(name, "/" + name, "4dce0442-5add-4854-91d7-afdbcfa316f4");
    }

    private static Asset mockAsset(String name, String path, String id) {
        Asset asset = mock(Asset.class);
        when(asset.getName()).thenReturn(name);
        when(asset.getPath()).thenReturn(path);
        when(asset.getItemKey()).thenReturn(new ItemKey(MOCK_ASSET_PROVIDER_ID, id));
        when(asset.getAssetProvider()).thenReturn(mock(AssetProvider.class));
        return asset;
    }

    private static <M extends AssetMetadata> M addMockMetadata(Asset asset, Class<M> metadataType) {
        M mgnlMetadata = mock(metadataType);
        when(asset.supports(metadataType)).thenReturn(true);
        when(asset.getMetadata(metadataType)).thenReturn(mgnlMetadata);
        return mgnlMetadata;
    }
}
