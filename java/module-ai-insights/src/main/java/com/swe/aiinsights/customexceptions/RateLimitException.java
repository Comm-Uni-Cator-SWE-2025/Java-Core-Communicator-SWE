/**
 * Class that creates a custom Exception when rate limit of cloud LLM is reached.
 * <p>
 *     References :
 *     1. https://www.baeldung.com/java-new-custom-exception
 *     2. https://www.geeksforgeeks.org/java/super-keyword/
 * </p>
 * @author Nandhana Sunil
 * @version 1.0.0
 * @since 1.0.0
 */

package com.swe.aiinsights.customexceptions;

import java.io.IOException;

/**
 * RateLimitException is an inheritance of IOException.
 */
public class RateLimitException extends IOException {
    public RateLimitException(String message) {
        super(message);
    }
}