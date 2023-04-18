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

    private static final String API_ENDPOINT = "https://api.codemaker.ai";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String apiKey;

    public DefaultClient(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public CreateProcessResponse CreateProcess(CreateProcessRequest request) {
        return doRequest(request, "/process", CreateProcessResponse.class);
    }

    @Override
    public GetProcessStatusResponse GetProcessStatus(GetProcessStatusRequest request) {
        return doRequest(request, "/process/status", GetProcessStatusResponse.class);
    }

    @Override
    public GetProcessOutputResponse GetProcessOutput(GetProcessOutputRequest request) {
        return doRequest(request, "/process/output", GetProcessOutputResponse.class);
    }

    private <TReq, TResp> TResp doRequest(TReq request, String path, Class<TResp> responseType) {
        try (final CloseableHttpClient client = HttpClients.createDefault()) {
            final byte[] body = serialize(request);
            final ClassicHttpRequest httpRequest = ClassicRequestBuilder.post(endpoint(path))
                    .addHeader("Accept", "application/json")
                    .addHeader("User-Agent", "CodeMakerSdkJava/1.1.0")
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

    private static String endpoint(String path) {
        return String.format("%s%s", API_ENDPOINT, path);
    }

    private static boolean isSuccess(int code) {
        return code >= 200 && code < 300;
    }
}
