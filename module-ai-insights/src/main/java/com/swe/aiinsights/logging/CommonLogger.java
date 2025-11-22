package com.swe.aiinsights.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonLogger {

    private CommonLogger() {}

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}
