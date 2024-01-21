/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client;

import ai.codemaker.sdkv2.client.model.AssistantCompletionRequest;
import ai.codemaker.sdkv2.client.model.AssistantCompletionResponse;
import ai.codemaker.sdkv2.client.model.CompletionRequest;
import ai.codemaker.sdkv2.client.model.CompletionResponse;
import ai.codemaker.sdkv2.client.model.CreateContextRequest;
import ai.codemaker.sdkv2.client.model.CreateContextResponse;
import ai.codemaker.sdkv2.client.model.DiscoverContextRequest;
import ai.codemaker.sdkv2.client.model.DiscoverContextResponse;
import ai.codemaker.sdkv2.client.model.PredictRequest;
import ai.codemaker.sdkv2.client.model.PredictResponse;
import ai.codemaker.sdkv2.client.model.ProcessRequest;
import ai.codemaker.sdkv2.client.model.ProcessResponse;
import ai.codemaker.sdkv2.client.model.RegisterContextRequest;
import ai.codemaker.sdkv2.client.model.RegisterContextResponse;

public interface Client {

    AssistantCompletionResponse assistantCompletion(AssistantCompletionRequest request);

    CompletionResponse completion(CompletionRequest request);

    ProcessResponse process(ProcessRequest request);

    PredictResponse predict(PredictRequest request);

    DiscoverContextResponse discoverContext(DiscoverContextRequest request);

    CreateContextResponse createContext(CreateContextRequest request);

    RegisterContextResponse registerContext(RegisterContextRequest request);
}
