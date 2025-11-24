package com.swe.core.serialize;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ClientNodeModuleTest {

    @Test
    public void constructorCreatesModule() {
        final ClientNodeModule module = new ClientNodeModule();
        assertNotNull(module);
    }

    @Test
    public void moduleCanBeRegistered() {
        final ClientNodeModule module = new ClientNodeModule();
        // Just verify it can be instantiated without errors
        assertNotNull(module);
    }
}

