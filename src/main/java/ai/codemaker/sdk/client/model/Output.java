/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.sdk.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Output {

    private final String source;

    @JsonCreator
    public Output(@JsonProperty("source") String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }
}
