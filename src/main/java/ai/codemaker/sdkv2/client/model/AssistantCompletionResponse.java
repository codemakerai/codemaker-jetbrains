/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

public class AssistantCompletionResponse {

    private final String message;

    public AssistantCompletionResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
