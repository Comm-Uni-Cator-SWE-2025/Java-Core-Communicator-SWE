/**
 * Provides asynchronous configuration for background task execution.
 */
/**
 * Author Berelli Gouthami
 */
package configu;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

/**
 * Configures asynchronous task execution for AI operations.
 * This enables background threads for long-running AI tasks
 * so that the main request thread remains responsive.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Creates and configures a thread pool for AI-related tasks.
     *
     * @return a configured thread pool executor
     */
    @Bean(name = "aiExecutor")
    public Executor aiExecutor() {
        // Define pool parameters (method-local variables, not constants)
        final int corePoolSize = 5;
        final int maxPoolSize = 10;
        final int queueCapacity = 50;

        // Create and configure executor
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("AI-Worker-");
        executor.initialize();
        return executor;
    }
}
