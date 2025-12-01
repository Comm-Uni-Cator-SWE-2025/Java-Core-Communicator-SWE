/******************************************************************************
 * Filename    = DbConnectorFactoryTest.java
 * Author      = Nikhil S Thomas
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = Unit tests for DbConnectorFactory
 *****************************************************************************/

package cosmosoperations;

import interfaces.IdbConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link DbConnectorFactory}. Ensures the correct database
 * connector implementations are returned based on the input type.
 */
class DbConnectorFactoryTest {
    /**
     * Resets the factory instance before each test to ensure no state is carried across tests.
     */
    @BeforeEach
    void resetFactory() {
        DbConnectorFactory.resetInstance();
    }

    /**
     * Verifies that the default constructor is invoked and requesting a "mock" or
     * empty string connector returns an instance of {@link MockDbConnector}.
     */
    @Test
    void getDefaultDbConnector() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var constructor = DbConnectorFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
        assertInstanceOf(MockDbConnector.class, DbConnectorFactory.getDbConnector("mock"));
        assertInstanceOf(MockDbConnector.class, DbConnectorFactory.getDbConnector(""));
    }

    /**
     * Ensures that passing "cosmo" returns the Cosmos DB connector implementation.
     */
    @Test
    void getCosmoDbConnector() {
        assertInstanceOf(CosmosOperations.class, DbConnectorFactory.getDbConnector("cosmo"));
    }
}