package crashhandler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrashHandlerTest {

    @Test
    void testSingleton() {
        CrashHandler testCrashHandler = new CrashHandler();

        testCrashHandler.startCrashHandler();
        Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();

        testCrashHandler.startCrashHandler();
        Thread.UncaughtExceptionHandler duplicateHandler = Thread.getDefaultUncaughtExceptionHandler();

        assertSame(originalHandler, duplicateHandler);
    }

//    @Test
//    void testStartCrashHandler() {
//        CrashHandler testCrashHandler = new CrashHandler();
//
//        testCrashHandler.startCrashHandler();
//        Thread.UncaughtExceptionHandler originalHandler = Thread.getDefaultUncaughtExceptionHandler();
//
//
//    }
}