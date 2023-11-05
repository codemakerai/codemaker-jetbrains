/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdkv2.client;

import ai.codemaker.sdkv2.client.model.Input;
import ai.codemaker.sdkv2.client.model.Language;
import ai.codemaker.sdkv2.client.model.Mode;
import ai.codemaker.sdkv2.client.model.ProcessRequest;
import ai.codemaker.sdkv2.client.model.ProcessResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class DefaultClientTest {

    @Disabled
    @Test
    public void testNewClientProcess() {

        final DefaultClient instance = new DefaultClient(() -> "CODEMAKER_TEST_PRODUCT_KEY", Config.create());

        final long start = System.currentTimeMillis();
        final ProcessResponse response = instance.process(new ProcessRequest(
                        Mode.DOCUMENT,
                        Language.JAVA,
                        new Input("public class Math {\n public int addTwo(int a, int b) {\n return a + b; }\n}"),
                        null
                )
        );
        System.out.println(System.currentTimeMillis() - start);

        System.out.println(response.getOutput());
    }
}