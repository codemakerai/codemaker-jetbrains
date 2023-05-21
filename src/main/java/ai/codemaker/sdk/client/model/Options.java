/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.sdk.client.model;

public class Options {

    private final Modify modify;

    private final String codePath;

    public Options(Modify modify, String codePath) {
        this.modify = modify;
        this.codePath = codePath;
    }

    public Modify getModify() {
        return modify;
    }

    public String getCodePath() {
        return codePath;
    }
}
