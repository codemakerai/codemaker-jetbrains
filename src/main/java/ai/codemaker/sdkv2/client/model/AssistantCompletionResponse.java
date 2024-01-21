/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

public class AssistantCompletionResponse {

    private final String output;

    public AssistantCompletionResponse(String output) {
        this.output = output;
    }

    public String getOutput() {
        return output;
    }
}
