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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.io.IOException;

public class DefaultClient implements Client {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String apiKey;

    private final Config config;

    public DefaultClient(String apiKey) {
        this(apiKey, Config.create());
    }

    public DefaultClient(String apiKey, Config config) {
        checkNotNull(apiKey, "apiKey");
        checkNotNull(config, "config");

        this.apiKey = apiKey;
        this.config = config;
    }

    @Override
    public CreateProcessResponse createProcess(CreateProcessRequest request) {
        return doRequest(request, "/process", CreateProcessResponse.class);
    }

    @Override
    public GetProcessStatusResponse getProcessStatus(GetProcessStatusRequest request) {
        return doRequest(request, "/process/status", GetProcessStatusResponse.class);
    }

    @Override
    public GetProcessOutputResponse getProcessOutput(GetProcessOutputRequest request) {
        return doRequest(request, "/process/output", GetProcessOutputResponse.class);
    }

    private <TReq, TResp> TResp doRequest(TReq request, String path, Class<TResp> responseType) {
        try (final CloseableHttpClient client = HttpClients.createDefault()) {
            final byte[] body = serialize(request);
            final ClassicHttpRequest httpRequest = ClassicRequestBuilder.post(endpoint(path))
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "CodeMakerSdkJava/1.4.0")
                    .addHeader("Authorization", String.format("Bearer %s", apiKey))
                    .setEntity(body, ContentType.APPLICATION_JSON)
                    .build();
            return client.execute(httpRequest, response -> {
                if (!isSuccess(response.getCode())) {
                    throw new ClientException(String.format("Request failed with status code %d", response.getCode()));
                }

                final byte[] respBody = EntityUtils.toByteArray(response.getEntity());
                return deserialize(respBody, responseType);
            });
        } catch (IOException e) {
            throw new ClientException("CodeMaker client request has failed", e);
        }
    }

    private byte[] serialize(Object value) throws JsonProcessingException {
        return MAPPER.writeValueAsBytes(value);
    }

    private <T> T deserialize(byte[] src, Class<T> responseType) throws IOException {
        return MAPPER.readValue(src, responseType);
    }

    private String endpoint(String path) {
        return String.format("%s%s", config.getEndpoint(), path);
    }

    private static boolean isSuccess(int code) {
        return code >= 200 && code < 300;
    }

    private static void checkNotNull(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(String.format("Parameter %s can not be null.", name));
        }
    }
}
