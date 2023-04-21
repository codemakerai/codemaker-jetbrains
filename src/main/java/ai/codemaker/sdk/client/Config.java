/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdk.client;

public class Config {

    private static final String DEFAULT_ENDPOINT = "https://api.codemaker.ai";

    private final String endpoint;

    private Config(Builder builder) {
        this.endpoint = builder.endpoint;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public static Config create() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String endpoint = DEFAULT_ENDPOINT;

        public Builder withEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Config build() {
            return new Config(this);
        }
    }
}
