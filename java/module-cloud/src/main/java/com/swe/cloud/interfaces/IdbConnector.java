/******************************************************************************
 * Filename    = IdbConnector.java
 * Author      = Nikhil S Thomas
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = Defines an interface for database operations in the cloud module.
 *****************************************************************************/

package com.swe.interfaces;

import com.swe.cloud.datastructures.CloudResponse;
import com.swe.cloud.datastructures.Entity;

/**
 * Interface for performing CRUD operations on cloud database entities.
 */
public interface IdbConnector {

    void init();

    CloudResponse getData(Entity request);

    CloudResponse postData(Entity request);

    CloudResponse createData(Entity request);

    CloudResponse deleteData(Entity request);

    CloudResponse updateData(Entity request);
}
