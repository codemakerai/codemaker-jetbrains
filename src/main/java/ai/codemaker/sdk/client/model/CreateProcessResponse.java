/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.sdk.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateProcessResponse {

    private final String id;

    @JsonCreator
    public CreateProcessResponse(@JsonProperty("id") String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
