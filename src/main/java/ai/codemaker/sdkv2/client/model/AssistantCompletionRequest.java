/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

public class AssistantCompletionRequest {

    private final String input;

    public AssistantCompletionRequest(String input) {
        this.input = input;
    }

    public String getInput() {
        return input;
    }
}
