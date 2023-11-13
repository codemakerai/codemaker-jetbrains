/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

import java.util.Collection;

public class DiscoverContextResponse {

    private final Collection<RequiredContext> requiredContexts;

    public DiscoverContextResponse(Collection<RequiredContext> requiredContexts) {
        this.requiredContexts = requiredContexts;
    }

    public Collection<RequiredContext> getRequiredContexts() {
        return requiredContexts;
    }
}
