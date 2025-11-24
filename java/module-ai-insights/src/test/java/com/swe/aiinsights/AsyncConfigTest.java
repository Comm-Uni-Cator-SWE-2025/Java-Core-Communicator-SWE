/*
 * -----------------------------------------------------------------------------
 *  File: AsyncConfigTest.java
 *  Owner: Berelli Gouthami
 *  Roll Number : 112201003
 *  Module : com.swe.aiinsights
 * -----------------------------------------------------------------------------
 */

package com.swe.aiinsights;

import com.swe.aiinsights.configu.AsyncConfig;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test class for AsyncConfig with 100% code coverage.
 */
class AsyncConfigTest {


    @Test
    void testAiExecutorReturnsExecutor() {

        final Executor executor = AsyncConfig.aiExecutor();


        assertTrue(executor instanceof ThreadPoolExecutor);
    }



    @Test
    void testAiExecutorCanExecuteTasks() throws InterruptedException {

        final Executor executor = AsyncConfig.aiExecutor();
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger counter = new AtomicInteger(0);


        executor.execute(() -> {
            counter.incrementAndGet();
            latch.countDown();
        });

        final boolean completed = latch.await(5, TimeUnit.SECONDS);

        assertTrue(completed);
        assertEquals(1, counter.get());
    }

    @Test
    void testAiExecutorMultipleTasks() throws InterruptedException {
        final Executor executor = AsyncConfig.aiExecutor();
        final int taskCount = 10;
        final CountDownLatch latch = new CountDownLatch(taskCount);
        final AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < taskCount; i++) {
            executor.execute(() -> {
                counter.incrementAndGet();
                latch.countDown();
            });
        }

        final boolean completed = latch.await(10, TimeUnit.SECONDS);

        assertTrue(completed);
        assertEquals(taskCount, counter.get());
    }
}