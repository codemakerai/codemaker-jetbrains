/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

public class AssistantCompletionRequest {

    private final String message;

    public AssistantCompletionRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
