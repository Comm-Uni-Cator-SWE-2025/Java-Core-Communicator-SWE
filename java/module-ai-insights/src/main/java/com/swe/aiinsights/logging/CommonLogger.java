/*
 * -----------------------------------------------------------------------------
 *  File: CommonLogger.java
 *  Owner: Berelli Gouthami
 *  Roll Number : 112201001
 *  Module : com.swe.aiinsights.logging
 * -----------------------------------------------------------------------------
 */

package com.swe.aiinsights.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log files for AI Insights module.
 */
public class CommonLogger {

    private CommonLogger() { }

    public static Logger getLogger(final Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}
