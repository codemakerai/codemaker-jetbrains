/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

public class ProcessResponse {

    private final Output output;

    public ProcessResponse(Output output) {
        this.output = output;
    }

    public Output getOutput() {
        return output;
    }
}
