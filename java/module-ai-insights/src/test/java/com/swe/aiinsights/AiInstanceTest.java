/*
 * -----------------------------------------------------------------------------
 *  File: AiInstanceTest.java
 *  Owner: Nandhana Sunil
 *  Roll Number : 112201008
 *  Module : com.swe.aiinsights
 *  References:
 *          1. Mocked construction : https://www.baeldung.com
 *              /java-mockito-constructors-unit-testing
 *          2. https://www.baeldung.com/java-mockito-mockedconstruction
 * -----------------------------------------------------------------------------
 */

package com.swe.aiinsights;

import com.swe.aiinsights.aiinstance.AiInstance;
import com.swe.aiinsights.apiendpoints.AiClientService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;

/**
 * Test class for AiInstance.
 */
@ExtendWith(MockitoExtension.class)
class AiInstanceTest {

    @AfterEach
    void tearDown() throws Exception {
        resetSingleton();
    }

    /**
     * Helper method to reset the singleton instance using reflection.
     */
    private void resetSingleton() throws Exception {
        final Field instance = AiInstance.class.getDeclaredField("aiClientService");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    void testGetInstance() throws Exception {
        resetSingleton();

        final AiClientService instance1 = AiInstance.getInstance();
        final AiClientService instance2 = AiInstance.getInstance();

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2, "getInstance should return the same instance");
    }

    @Test
    void testInitializationFailure() {
        // Mock constructor of AiClientService to simulate failure
        try (MockedConstruction<AiClientService> mocked =
                     mockConstruction(AiClientService.class,
                             (mock, context) -> {
                                 throw new RuntimeException("Simulating failure");
                             })) {

            final RuntimeException ex = assertThrows(RuntimeException.class, AiInstance::getInstance);

            assertEquals("AI Service Initialization Failed", ex.getMessage());
        }
    }
}