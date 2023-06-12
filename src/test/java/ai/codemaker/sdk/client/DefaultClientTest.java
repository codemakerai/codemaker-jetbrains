/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdk.client;

import ai.codemaker.sdk.client.model.CreateProcessRequest;
import ai.codemaker.sdk.client.model.CreateProcessResponse;
import ai.codemaker.sdk.client.model.GetProcessOutputRequest;
import ai.codemaker.sdk.client.model.GetProcessOutputResponse;
import ai.codemaker.sdk.client.model.GetProcessStatusRequest;
import ai.codemaker.sdk.client.model.GetProcessStatusResponse;
import ai.codemaker.sdk.client.model.Input;
import ai.codemaker.sdk.client.model.Language;
import ai.codemaker.sdk.client.model.Mode;
import ai.codemaker.sdk.client.model.Modify;
import ai.codemaker.sdk.client.model.Options;
import ai.codemaker.sdk.client.model.Process;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockServerExtension.class)
public class DefaultClientTest {

    private static final String API_KEY = "ABCDEFGHIJKL";

    private MockServerClient client;

    private DefaultClient instance;

    @BeforeEach
    void setUp(MockServerClient client) {
        this.client = client;
        registerExpectations(this.client);
        instance = new DefaultClient(
                () -> API_KEY,
                Config.builder()
                        .withEndpoint(getEndpoint(this.client))
                        .build()
        );
    }

    @Test
    public void testCreateProcess() {

        // given
        final CreateProcessRequest request = new CreateProcessRequest(
                new Process(
                        Mode.DOCUMENT,
                        Language.JAVA,
                        new Input(""),
                        new Options(Modify.NONE, "Test.method()")
                )
        );

        // when
        final CreateProcessResponse response = instance.createProcess(request);

        // then
        assertNotNull(response);
        assertNotNull(response.getId());
    }

    @Test
    public void testGetProcessStatus() {

        // given
        final GetProcessStatusRequest request = new GetProcessStatusRequest(UUID.randomUUID().toString());

        // when
        final GetProcessStatusResponse response = instance.getProcessStatus(request);

        // then
        assertNotNull(response);
    }

    @Test
    public void testGetProcessOutput() {

        // given
        final GetProcessOutputRequest request = new GetProcessOutputRequest(UUID.randomUUID().toString());

        // when
        final GetProcessOutputResponse response = instance.getProcessOutput(request);

        // then
        assertNotNull(response);
    }

    private static void registerExpectations(MockServerClient client) {
        registerEndpointExpectations(client, "/process", 201, "{\"id\": \"ccc071f2-a773-40b5-b7db-6c2ac56ca169\"}");
        registerEndpointExpectations(client, "/process/status", 200, "{\"status\": \"IN_PROGRESS\"}");
        registerEndpointExpectations(client, "/process/output", 200, "{\"output\": {\"source\": \"\"} }");
    }

    private static void registerEndpointExpectations(MockServerClient client, String path, int statusCode, String body) {
        client.when(
                        request()
                                .withMethod("POST")
                                .withPath(path)
                )
                .respond(
                        response()
                                .withStatusCode(statusCode)
                                .withBody(body)
                );
    }

    private static String getEndpoint(MockServerClient client) {
        return String.format("http://%s:%d",
                client.remoteAddress().getHostName(),
                client.remoteAddress().getPort()
        );
    }
}
