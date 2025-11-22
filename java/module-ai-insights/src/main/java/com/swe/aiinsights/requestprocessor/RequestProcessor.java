/**
 * Author : Abhirami R Iyer.
 */

package com.swe.aiinsights.requestprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swe.aiinsights.request.AiRequestable;
import java.io.IOException;

/**
 * IRequestProcessor class is used to build the json request body.
 *
 */
public interface RequestProcessor {
    /**
     * Builds the json string for making the AI request.
     * @param objectMapper holds the structure for serializing
     *                     java objects to JSON it is used for
     *                     handling request bodies and response bodies.
     * @param request holds the details of request.
     * @return the json string request.
     * @throws IOException in case of any exceptions.
     */
    String processRequest(ObjectMapper objectMapper,
                           AiRequestable request) throws IOException;
}
