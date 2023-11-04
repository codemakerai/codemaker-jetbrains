/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client;

public class Config {

    private static final String DEFAULT_ENDPOINT = "process.codemaker.ai";

    private static final int DEFAULT_PORT = 443;

    private static final boolean DEFAULT_ENABLE_COMPRESSION = true;

    private static final int DEFAULT_MINIMUM_COMPRESSION_PAYLOAD_SIZE = 2048;

    private final String endpoint;

    private final int port;

    private final boolean enableCompression;

    private final int minimumCompressionPayloadSize;

    private Config(Builder builder) {
        this.endpoint = builder.endpoint;
        this.port = builder.port;
        this.enableCompression = builder.enableCompression;
        this.minimumCompressionPayloadSize = builder.minimumCompressionPayloadSize;
    }

    public static Config create() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public int getPort() {
        return port;
    }

    public boolean isEnableCompression() {
        return enableCompression;
    }

    public int getMinimumCompressionPayloadSize() {
        return minimumCompressionPayloadSize;
    }

    public static class Builder {

        private String endpoint = DEFAULT_ENDPOINT;

        private int port = DEFAULT_PORT;

        private boolean enableCompression = DEFAULT_ENABLE_COMPRESSION;

        private int minimumCompressionPayloadSize = DEFAULT_MINIMUM_COMPRESSION_PAYLOAD_SIZE;

        public Builder withEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder withPort(int port) {
            this.port = port;
            return this;
        }

        public Builder withEnableCompression(boolean enableCompression) {
            this.enableCompression = enableCompression;
            return this;
        }

        public Builder withMinimumCompressionPayloadSize(int minimumCompressionPayloadSize) {
            this.minimumCompressionPayloadSize = minimumCompressionPayloadSize;
            return this;
        }

        public Config build() {
            return new Config(this);
        }
    }
}
