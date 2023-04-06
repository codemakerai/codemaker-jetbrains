/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.sdk.client.model;

public class Process {
    private final Mode mode;

    private final Language language;

    private final Input input;

    public Process(Mode mode, Language language, Input input) {
        this.mode = mode;
        this.language = language;
        this.input = input;
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
}
