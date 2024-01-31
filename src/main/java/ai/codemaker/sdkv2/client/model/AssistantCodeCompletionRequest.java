/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

public class AssistantCodeCompletionRequest {

    private final String message;

    private final Language language;

    private final Input input;

    private final Options options;

    public AssistantCodeCompletionRequest(String message, Language language, Input input, Options options) {
        this.message = message;
        this.language = language;
        this.input = input;
        this.options = options;
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

    public Options getOptions() {
        return options;
    }
}
