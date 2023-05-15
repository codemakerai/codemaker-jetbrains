/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.sdk.client.model;

public class Options {

    private final Modify modify;

    public Options(Modify modify) {
        this.modify = modify;
    }

    public Modify getModify() {
        return modify;
    }
}
