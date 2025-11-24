/*
 * -----------------------------------------------------------------------------
 *  File: RateLimitExceptionTest.java
 *  Owner: Nandhana Sunil
 *  Roll Number : 112201008
 *  Module : com.swe.aiinsights
 * -----------------------------------------------------------------------------
 */

package com.swe.aiinsights;

import com.swe.aiinsights.customexceptions.RateLimitException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class RateLimitExceptionTest {

    @Test
    void testConstructorWithMessage() {
        final String errorMessage = "Rate limit exceeded";

        final RateLimitException exception = new RateLimitException(errorMessage);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testConstructorWithNull() {
        final RateLimitException exception = new RateLimitException(null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

}