/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

public class RequiredContext {

    private final String path;

    public RequiredContext(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
