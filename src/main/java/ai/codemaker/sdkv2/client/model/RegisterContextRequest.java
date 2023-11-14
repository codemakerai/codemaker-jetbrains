/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

import java.util.Collection;

public class RegisterContextRequest {

    private final String id;

    private final Collection<Context> contexts;

    public RegisterContextRequest(String id, Collection<Context> contexts) {
        this.id = id;
        this.contexts = contexts;
    }

    public String getId() {
        return id;
    }

    public Collection<Context> getContexts() {
        return contexts;
    }
}
