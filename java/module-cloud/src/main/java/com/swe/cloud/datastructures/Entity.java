/******************************************************************************
 * Filename    = Entity.java
 * Author      = Kallepally Sai Kiran, Nikhil S Thomas
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = Defines a common data structure for the cloud API requests.
 *****************************************************************************/

package datastructures;

import com.fasterxml.jackson.databind.JsonNode;

public record Entity(String module, String table, String id, String type,
                     int lastN, TimeRange timeRange, JsonNode data) { }