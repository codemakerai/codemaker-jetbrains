/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */
package ai.codemaker.sdk.client;

import ai.codemaker.sdk.client.model.CreateProcessRequest;
import ai.codemaker.sdk.client.model.CreateProcessResponse;
import ai.codemaker.sdk.client.model.GetProcessOutputRequest;
import ai.codemaker.sdk.client.model.GetProcessOutputResponse;
import ai.codemaker.sdk.client.model.GetProcessStatusRequest;
import ai.codemaker.sdk.client.model.GetProcessStatusResponse;

public interface Client {

    CreateProcessResponse CreateProcess(CreateProcessRequest request);

    GetProcessStatusResponse GetProcessStatus(GetProcessStatusRequest request);

    GetProcessOutputResponse GetProcessOutput(GetProcessOutputRequest request);
}
