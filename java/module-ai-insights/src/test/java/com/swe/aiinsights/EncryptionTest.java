/*
 * -----------------------------------------------------------------------------
 *  File: EncryptionTest.java
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

import com.swe.aiinsights.getkeys.EncryptKeys;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertSame;

/**
 * Test class for EncryptKeys.
 */
class EncryptionTest{

    @Test
    void testGetInstance() throws Exception {
        final String testString = "testString";
        final String encrytpedString = EncryptKeys.encrypt(testString);
        final String decryptedString = EncryptKeys.decrypt(encrytpedString);
        assertSame(encrytpedString, decryptedString);
    }

}