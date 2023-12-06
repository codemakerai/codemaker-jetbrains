/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

import java.util.Collection;

public class DiscoverContextResponse {

    private final Collection<RequiredContext> requiredContexts;

    private final boolean requiresProcessing;

    public DiscoverContextResponse(Collection<RequiredContext> requiredContexts, boolean requiresProcessing) {
        this.requiredContexts = requiredContexts;
        this.requiresProcessing = requiresProcessing;
    }

    public Collection<RequiredContext> getRequiredContexts() {
        return requiredContexts;
    }

    public boolean isRequiresProcessing() {
        return requiresProcessing;
    }
}
