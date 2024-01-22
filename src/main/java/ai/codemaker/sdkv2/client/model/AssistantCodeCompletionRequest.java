/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

public class AssistantCodeCompletionRequest {

    private final String message;

    private final Language language;

    private final Input input;

    public AssistantCodeCompletionRequest(String message, Language language, Input input) {
        this.message = message;
        this.language = language;
        this.input = input;
    }

    public String getMessage() {
        return message;
    }

    public Language getLanguage() {
        return language;
    }

    public Input getInput() {
        return input;
    }
}
