/******************************************************************************
 * Filename    = Response.java
 * Author      = Kallepally Sai Kiran, Nikhil S Thomas
 * Product     = cloud-function-app
 * Project     = Comm-Uni-Cator
 * Description = Defines a common data structure for the cloud API responses.
 *****************************************************************************/

package datastructures;

import com.fasterxml.jackson.databind.JsonNode;

public record Response(int status_code, String message, JsonNode data) { }
