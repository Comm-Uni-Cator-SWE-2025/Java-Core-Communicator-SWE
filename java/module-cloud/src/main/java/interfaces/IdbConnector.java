/******************************************************************************
 * Filename    = IdbConnector.java
 * Author      = Nikhil S Thomas
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = Defines an interface for database operations in the cloud module.
 *****************************************************************************/

package interfaces;

import datastructures.Entity;
import datastructures.Response;

/**
 * Interface for performing CRUD operations on cloud database entities.
 */
public interface IdbConnector {

    void init();

    Response getData(Entity request);

    Response postData(Entity request);

    Response createData(Entity request);

    Response deleteData(Entity request);

    Response updateData(Entity request);
}
