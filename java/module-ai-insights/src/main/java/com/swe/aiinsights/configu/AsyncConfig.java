/**
 * Provides configuration for asynchronous AI execution.
 * This includes creation of a thread pool used for AI-related tasks.
 *
 * @author Berelli Gouthami
 * @editedby Abhirami R Iyer
 */

package com.swe.aiinsights.configu;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for async executor setup.
 */
public class AsyncConfig {

    /**
     * Creates and configures a thread pool for AI-related tasks.
     *
     * @return a configured executor service
     */
    public static Executor aiExecutor() {
        final int corePoolSize = 5;
        final int maxPoolSize = 10;
        final int queueCapacity = 50;

        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                30L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                r -> {
                    final Thread t = new Thread(r);
                    t.setName("AI-Worker-" + t.getId());
                    return t;
                }
        );

        return executor;  // returning Executor is valid
    }
}
