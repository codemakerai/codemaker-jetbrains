/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.sdk.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GetProcessStatusResponse {

    private final Status status;

    @JsonCreator
    public GetProcessStatusResponse(@JsonProperty("status") Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
