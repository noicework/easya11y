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
package info.magnolia.form.endpoints.v1;

import info.magnolia.form.endpoints.AbstractFormEndpointTest;
import info.magnolia.form.endpoints.Snapshot;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.jsonSnapshot.SnapshotMatcher;

public class FormEndpointTest extends AbstractFormEndpointTest {

    private final String FORM_UUID = "2ccd98a1-9081-4beb-a954-c2aa2b3b4248";

    @BeforeClass
    public static void setUp() {
        setUpEnvironment();
        SnapshotMatcher.start(Snapshot::asJsonString);
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    public void getForms() throws URISyntaxException, IOException {

        MockHttpRequest request = MockHttpRequest.get("/forms/v1/forms");
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(200, response.getStatus());
        JsonNode jsonNode = parseJsonResponse(response.getContentAsString());
        SnapshotMatcher.expect(jsonNode).toMatchSnapshot();
    }

    @Test
    public void getForm() throws URISyntaxException, IOException {

        MockHttpRequest request = MockHttpRequest.get("/forms/v1/forms/" + FORM_UUID);
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(200, response.getStatus());

        JsonNode jsonNode = parseJsonResponse(response.getContentAsString());
        System.out.println(jsonNode);
        SnapshotMatcher.expect(jsonNode).toMatchSnapshot();
    }

    @Test
    public void postResponse() throws URISyntaxException, IOException {

        MockHttpRequest request = MockHttpRequest.post("/forms/v1/forms/" + FORM_UUID);
        request.accept(MediaType.APPLICATION_JSON);
        request.contentType(MediaType.APPLICATION_JSON_TYPE);
        String requestBody = "[\n" +
                "  {\n" +
                "    \"questionId\": \"c75790a6-6f74-4a1c-8508-5f0bdca4c1dc\",\n" +
                "    \"value\":\"powerful\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionId\": \"d712ca28-b50a-4302-b692-483e67617365\",\n" +
                "    \"value\":\"life\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionId\": \"ae27d6ef-d0c9-48b8-bc4f-a621930f36b5\",\n" +
                "    \"value\":\"Nikola Tesla\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionId\": \"d8b56533-2391-423a-950a-a19b40a8b1bc\",\n" +
                "    \"value\":\"father\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionId\": \"d712ca28-b50a-4302-b692-483e67617365\",\n" +
                "    \"value\":\"bitcoin\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionId\": \"dfc8a09b-1112-4f4f-a618-2a9255496d31\",\n" +
                "    \"value\":\"night-owl\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionId\": \"b5ad27d4-9570-4226-aa4f-f7976ed27f3b\",\n" +
                "    \"value\":\"flying\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionId\": \"380e4c1c-6858-4842-b76e-8d7279b6f318\",\n" +
                "    \"value\":\"future\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionId\": \"c23458d4-80bb-4e74-bb05-4dc1c2ccd105\",\n" +
                "    \"value\":\"no\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionId\": \"ea13f315-3988-4511-981f-981aedb0229e\",\n" +
                "    \"value\":\"8>\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionId\": \"00687337-f69a-4b0b-a2b4-60b69eae01cf\",\n" +
                "    \"value\":\"WTF\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionId\": \"e77aed2e-494c-4e6f-8bd6-9a647bbf42e1\",\n" +
                "    \"value\":\"individual\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionId\": \"04fd750f-b958-4f08-bf6b-622b6c87ef52\",\n" +
                "    \"value\":\"dev\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionId\": \"e2440662-89d8-4eaa-adcd-3d65df31b757\",\n" +
                "    \"value\":\"pajamas\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionId\": \"502a3270-3e81-45a2-a4d4-b81571c20150\",\n" +
                "    \"value\":\"grandparent\"\n" +
                "  }\n" +
                "]";
        request.content(requestBody.getBytes());
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(200, response.getStatus());

        JsonNode jsonNode = parseJsonResponse(response.getContentAsString(), Arrays.asList("created", "modified", "id"));
        SnapshotMatcher.expect(jsonNode).toMatchSnapshot();
    }
}
