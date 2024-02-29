/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.sdkv2.client.model;

public class Options {

    private final Modify modify;

    private final String codePath;

    private final String prompt;

    private final boolean detectSyntaxErrors;

    private final boolean allowMultiLineAutocomplete;

    private final String contextId;

    private final String model;

    public Options(Modify modify, String codePath, String prompt, boolean detectSyntaxErrors, boolean allowMultiLineAutocomplete, String contextId, String model) {
        this.modify = modify;
        this.codePath = codePath;
        this.prompt = prompt;
        this.detectSyntaxErrors = detectSyntaxErrors;
        this.allowMultiLineAutocomplete = allowMultiLineAutocomplete;
        this.contextId = contextId;
        this.model = model;
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

    public boolean isAllowMultiLineAutocomplete() {
        return allowMultiLineAutocomplete;
    }

    public String getContextId() {
        return contextId;
    }

    public String getModel() {
        return model;
    }
}
