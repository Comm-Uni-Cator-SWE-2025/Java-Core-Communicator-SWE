package crashhandler;

import datastructures.CloudResponse;
import datastructures.Entity;
import functionlibrary.CloudFunctionLibrary;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CrashHandlerTest {

    CloudFunctionLibrary cloudFunctionLibrary = new CloudFunctionLibrary();

    @Test
    void testSingleton() {
        CrashHandler testCrashHandler = new CrashHandler(cloudFunctionLibrary);

        testCrashHandler.startCrashHandler();
        Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();

        testCrashHandler.startCrashHandler();
        Thread.UncaughtExceptionHandler duplicateHandler = Thread.getDefaultUncaughtExceptionHandler();

        assertSame(originalHandler, duplicateHandler);
    }

    @Test
    void testStartCrashHandler() throws InterruptedException {
        CrashHandler testCrashHandler = new CrashHandler(cloudFunctionLibrary);

        testCrashHandler.startCrashHandler();
        Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread crashingTread = new Thread(() -> {
            throw new RuntimeException("Intentional Crashing...");
        }, "crashingTestThread");
        crashingTread.start();
        crashingTread.join();
    }

    @Test
    void testMultipleCrashes() throws InterruptedException {
        CrashHandler testCrashHandler = new CrashHandler(cloudFunctionLibrary);

        testCrashHandler.startCrashHandler();
        Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread crashingTreadMain = new Thread(() -> {
            throw new RuntimeException("Intentional Crashing...");
        }, "crashingTestThreadMain");

        Thread crashingTreadSecondary = new Thread(() -> {
            throw new ArrayStoreException("Yet another intentional crash...");
        }, "crashingTestThreadDuplicate");

        crashingTreadMain.start();
        crashingTreadSecondary.start();
        crashingTreadMain.join();
        crashingTreadSecondary.join();

    }

//    @Test
//    void testCloudFailure() throws InterruptedException, IOException {
//
//        CloudFunctionLibrary mockCloudFunctionLibrary = Mockito.mock(CloudFunctionLibrary.class);
//        when(mockCloudFunctionLibrary.cloudCreate(any())).thenReturn(CompletableFuture.completedFuture(new CloudResponse(400, "Failure Testing", null)));
//        when(mockCloudFunctionLibrary.cloudGet(any())).thenReturn(CompletableFuture.completedFuture(new CloudResponse(400, "Failure Testing", null)));
//        when(mockCloudFunctionLibrary.cloudPost(any())).thenReturn(CompletableFuture.completedFuture(new CloudResponse(400, "Failure Testing", null)));
//
//        CrashHandler testCrashHandler = new CrashHandler(mockCloudFunctionLibrary);
//
//        testCrashHandler.startCrashHandler();
//        Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();
//    }

}