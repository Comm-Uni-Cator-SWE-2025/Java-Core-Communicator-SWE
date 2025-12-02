/*
 * -----------------------------------------------------------------------------
 *  File: AiInstance.java
 *  Owner: Nandhana Sunil
 *  Roll Number : 112201008
 *  Module : com.swe.aiinsights.aiinstance
 * -----------------------------------------------------------------------------
 */

/**
 * Creates an Instance of AI Service.
 * <p>
 * The instance will be the singleton instance of AI Service
 * for running in a client machine.
 * </p>
 * <p>
 *    References
 *     1. https://www.geeksforgeeks.org/java/singleton-class-java/
 *          (for synchronised singleton)
 *     2. https://www.geeksforgeeks.org/
 *      java/java-program-to-demonstrate-the-double-check-locking-for-singleton-class/
 *          (for double checking locking for singleton classes)
 *     3. https://www.cs.cornell.edu/courses/cs6120/2019fa/blog/double-checked-locking/
 *          #:~:text=However%2C%20since%20all%20read%20and,of%20times%20accessing%20volatile%20variable.
 *          (for increasing performance with local variable)
 * </p>
 * <p>
 * Creation of singleton AI Service should be synchronised because 
 * multiple threads may try to create the instance.
 * Using double-checked lock is better here because, if we use simple
 * synchronised singleton creation, every thread has to wait for lock 
 * even if the object is created already.
 * </p>
 *
 * @author Nandhana Sunil
 * @version 1.0.0
 * @since 1.0.0
 */

package com.swe.aiinsights.aiinstance;

import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import com.swe.aiinsights.apiendpoints.AiClientService;

/**
 * THis class is used ot create a singleton instance of AI.
 * Across requests, it will remain same in a client.
 */

public class AiInstance {
    /**
     * Get the log file path.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("AI-INSIGHTS");
    /**
     * Creates a singleton instance of AI Service using getInstance method.
     */
    private static volatile AiClientService aiClientService = null;

    /**
     * Volatile Keyword ensures that the aiClientService variable
     * is not cached and changes are visible across threads,
     * preventing partially initialized objects.
     */

    /**
     * The constructor is made private.
     * We don't want to get AiInstance object,
     * but AiClientService using getInstance
     */
    private AiInstance() {

    }

    /**
     * Used to get the singleton ClientService.
     * 
     * @return AiClientService common to all requests
     */
    public static AiClientService getInstance() {
        LOG.debug("Fetching the AI instance");
        AiClientService localReference = aiClientService;
        if (localReference == null) { // 1st check: there is no locking here
            synchronized (AiInstance.class) { // AiClientInstance is static,
                // therefore we should lock on sth common, which is the class object here.
                if (localReference == null) { // 2nd check: with locking
                    try {
                        LOG.debug("No AI instance found! Creating a new instance ..");
                         localReference = new AiClientService();
                         aiClientService = localReference;
                    } catch (Exception e) {
                        LOG.error("Failure in initialising AI service");
                        LOG.error("Error is " + e.getMessage());
                        throw new RuntimeException("AI Service Initialization Failed", e);
                    }
                }
            }
        }
        return localReference;
    }
}
