/*
 * Copyright 2023 CodeMaker AI Inc. All rights reserved.
 */

package ai.codemaker.sdk.client;


import org.junit.jupiter.api.Test;

public class ClientExceptionTest {

    @Test
    public void testCreation() {

        // when
        new ClientException("Test exception");
    }

    @Test
    public void testCreationWithCause() {

        // when
        new ClientException("Test exception", new Exception());
    }
}
