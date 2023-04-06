/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.sdk.client.model;

public class GetProcessStatusRequest {

    private final String id;

    public GetProcessStatusRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
