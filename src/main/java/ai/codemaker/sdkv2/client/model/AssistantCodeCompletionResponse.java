/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

public class AssistantCodeCompletionResponse {

    private final String message;

    private final Output output;

    public AssistantCodeCompletionResponse(String message, Output output) {
        this.message = message;
        this.output = output;
    }

    public String getMessage() {
        return message;
    }

    public Output getOutput() {
        return output;
    }
}
