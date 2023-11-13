/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

public class Context {

    private final Language language;

    private final Input input;

    private final String path;

    public Context(Language language, Input input, String path) {
        this.language = language;
        this.input = input;
        this.path = path;
    }

    public Language getLanguage() {
        return language;
    }

    public Input getInput() {
        return input;
    }

    public String getPath() {
        return path;
    }
}
