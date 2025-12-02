/*
 * -----------------------------------------------------------------------------
 *  File: AsyncConfig.java
 *  Owner: Berelli Gouthami
 *  Roll Number : 112201003
 *  Module : com.swe.aiinsights.configu
 * -----------------------------------------------------------------------------
 */

/**
 * Provides configuration for asynchronous AI execution.
 * This includes creation of a thread pool used for AI-related tasks.
 *
 * @author Berelli Gouthami
 * @editedby Abhirami R Iyer
 */

package com.swe.aiinsights.configu;

import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for async executor setup.
 */
public class AsyncConfig {
    /**
     * Get the log file path.
     */
    private static final SweLogger LOG =
            SweLoggerFactory.getLogger("AI-INSIGHTS");

    /**
     * Creates and configures a thread pool for AI-related tasks.
     *
     * @return a configured executor service
     */
    public static Executor aiExecutor() {
        final int corePoolSize = 5;
        final int maxPoolSize = 10;
        final int queueCapacity = 50;

        LOG.info("AI Executor begins: core=" + corePoolSize + ", max=" + maxPoolSize + ", queueCap=" + queueCapacity);

        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                30L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                r -> {
                    final Thread t = new Thread(r);
                    t.setName("AI-Worker-" + t.getId());
                    LOG.debug("Created new thread " + t.getName());
                    return t;
                }
        );

        LOG.info("AI Executor initialized successfully");

        return executor;  // returning Executor is valid
    }
}
