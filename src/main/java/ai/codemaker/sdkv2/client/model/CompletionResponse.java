/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

import ai.codemaker.sdk.client.model.Output;

public class CompletionResponse {

    private final Output output;

    public CompletionResponse(Output output) {
        this.output = output;
    }

    public Output getOutput() {
        return output;
    }
}
