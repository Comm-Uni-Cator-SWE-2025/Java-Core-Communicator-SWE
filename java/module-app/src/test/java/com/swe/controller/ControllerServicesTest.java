package com.swe.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.swe.core.Context;
import java.lang.reflect.Field;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControllerServicesTest {

    @BeforeEach
    @AfterEach
    void resetSingleton() throws Exception {
        final Field instanceField = ControllerServices.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
        final Context context = Context.getInstance();
        context.rpc = null;
        context.self = null;
        context.selfIP = null;
        context.mainServerIP = null;
        context.meetingSession = null;
    }

    @Test
    void getInstanceReturnsSameReference() {
        final ControllerServices first = ControllerServices.getInstance();
        final ControllerServices second = ControllerServices.getInstance();
        assertSame(first, second, "ControllerServices should be a singleton");
    }

    @Test
    void constructorInitializesContext() {
        final ControllerServices services = ControllerServices.getInstance();
        assertNotNull(services.context, "Context should be initialized");
        assertSame(Context.getInstance(), services.context, "Context should reuse singleton instance");
    }
}
