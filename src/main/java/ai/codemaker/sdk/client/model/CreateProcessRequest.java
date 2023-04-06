/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.sdk.client.model;

public class CreateProcessRequest {

    private final Process process;

    public CreateProcessRequest(Process process) {
        this.process = process;
    }

    public Process getProcess() {
        return process;
    }
}
