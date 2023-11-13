/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client.model;

public class DiscoverContextRequest {

    private final Context context;

    public DiscoverContextRequest(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
