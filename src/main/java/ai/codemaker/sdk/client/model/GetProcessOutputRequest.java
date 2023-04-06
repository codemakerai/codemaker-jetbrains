/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.sdk.client.model;

public class GetProcessOutputRequest {

    private final String id;

    public GetProcessOutputRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
