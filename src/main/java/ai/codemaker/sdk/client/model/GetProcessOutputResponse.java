/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.sdk.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GetProcessOutputResponse {

    private final Output output;

    @JsonCreator
    public GetProcessOutputResponse(@JsonProperty("output") Output output) {
        this.output = output;
    }

    public Output getOutput() {
        return output;
    }
}
