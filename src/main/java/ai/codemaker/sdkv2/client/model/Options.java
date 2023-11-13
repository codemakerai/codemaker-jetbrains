/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.sdkv2.client.model;

public class Options {

    private final Modify modify;

    private final String codePath;

    private final String prompt;

    private final boolean detectSyntaxErrors;

    private final String contextId;

    public Options(Modify modify, String codePath, String prompt, boolean detectSyntaxErrors, String contextId) {
        this.modify = modify;
        this.codePath = codePath;
        this.prompt = prompt;
        this.detectSyntaxErrors = detectSyntaxErrors;
        this.contextId = contextId;
    }

    public Modify getModify() {
        return modify;
    }

    public String getCodePath() {
        return codePath;
    }

    public String getPrompt() {
        return prompt;
    }

    public boolean isDetectSyntaxErrors() {
        return detectSyntaxErrors;
    }

    public String getContextId() {
        return contextId;
    }
}
