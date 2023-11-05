/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

public class ProcessRequest {

    private final Mode mode;

    private final Language language;

    private final Input input;

    private final Options options;

    public ProcessRequest(Mode mode, Language language, Input input, Options options) {
        this.mode = mode;
        this.language = language;
        this.input = input;
        this.options = options;
    }

    public Mode getMode() {
        return mode;
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
