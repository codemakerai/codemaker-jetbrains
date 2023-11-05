/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client;

import ai.codemaker.sdkv2.client.model.CompletionRequest;
import ai.codemaker.sdkv2.client.model.CompletionResponse;
import ai.codemaker.sdkv2.client.model.PredictRequest;
import ai.codemaker.sdkv2.client.model.PredictResponse;
import ai.codemaker.sdkv2.client.model.ProcessRequest;
import ai.codemaker.sdkv2.client.model.ProcessResponse;

public interface Client {

    CompletionResponse completion(CompletionRequest request);

    ProcessResponse process(ProcessRequest request);

    PredictResponse predict(PredictRequest request);
}
