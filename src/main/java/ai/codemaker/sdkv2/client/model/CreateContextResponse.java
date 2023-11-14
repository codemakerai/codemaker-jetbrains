/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

public class CreateContextResponse {

    private final String id;

    public CreateContextResponse(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
