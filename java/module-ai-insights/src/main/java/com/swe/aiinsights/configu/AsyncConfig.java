/**
 * Author :
 */
package com.swe.aiinsights.configu;

import java.util.concurrent.*;

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

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                30L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("AI-Worker-" + t.getId());
                    return t;
                }
        );

        return executor;  // returning Executor is valid
    }
}
