package cosmosoperations;

import interfaces.IdbConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class DbConnectorFactoryTest {
    @BeforeEach
    void resetFactory() {
        DbConnectorFactory.resetInstance();
    }

    @Test
    void getDefaultDbConnector() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var constructor = DbConnectorFactory.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
        assertInstanceOf(MockDbConnector.class, DbConnectorFactory.getDbConnector("mock"));
        assertInstanceOf(MockDbConnector.class, DbConnectorFactory.getDbConnector(""));
    }

    @Test
    void getCosmoDbConnector() {
        assertInstanceOf(CosmosOperations.class, DbConnectorFactory.getDbConnector("cosmo"));
    }
}