/******************************************************************************
 * Filename    = DbConnectorFactory.java
 * Author      = Nikhil S Thomas
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = Defines a factory class  for providing an instance of the
 *               database connector used for cloud database operations.
 *****************************************************************************/

package cosmosoperations;

import interfaces.IdbConnector;
import java.util.Objects;

/**
 * Factory class for creating and managing instances of {@link IdbConnector}.
 */
public class DbConnectorFactory {

    /** Singleton instance of the database connector. */
    private static IdbConnector dbConnector;

    public DbConnectorFactory() { }

    /**
     * Returns a shared instance of the database connector.
     * If the connector is not yet initialized, it creates a new one,
     * initializes it, and returns it.
     *
     * @param provider Name of the database provider/platform.
     * @return The initialized {@link IdbConnector} instance.
     */
    public static IdbConnector getDbConnector(final String provider) {
        if (dbConnector == null) {
            if (Objects.equals(provider, "cosmo")) {
                dbConnector = new CosmosOperations();  // Instantiate CosmosDB Connector
            } else {
                dbConnector = new MockDbConnector();  // Default to mock dbConnector
            }
            dbConnector.init();
        }
        return dbConnector;
    }

    /**
     * Resets the singleton instance.
     * FOR TESTING PURPOSES ONLY.
     */
    public static void resetInstance() {
        dbConnector = null;
    }
}
