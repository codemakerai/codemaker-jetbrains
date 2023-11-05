/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

public class PredictRequest {

    private final Language language;

    private final Input input;

    public PredictRequest(Language language, Input input) {
        this.language = language;
        this.input = input;
    }

    public Language getLanguage() {
        return language;
    }

    public Input getInput() {
        return input;
    }
}
