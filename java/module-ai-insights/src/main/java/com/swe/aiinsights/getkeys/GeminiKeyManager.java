/*
 * -----------------------------------------------------------------------------
 *  File: GeminiKeyManager.java
 *  Owner: Nandhana Sunil
 *  Roll Number : 112201008
 *  Module : com.swe.aiinsights.getkeys
 * -----------------------------------------------------------------------------
 */

/**
 * <p>
 *     Used to get Gemini key list from cloud.
 * </p>
 * @author : Nandhana Sunil
 */

package com.swe.aiinsights.getkeys;

import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import datastructures.Entity;
import datastructures.Response;
import datastructures.TimeRange;
import functionlibrary.CloudFunctionLibrary;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The key manager class to get Gemini Key from cloud.
 */
public final class GeminiKeyManager {
    /**
     * Get the log file path.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("AI-INSIGHTS");
    /**
     * THe cloud function library which will fetch the keys.
     */
    private final CloudFunctionLibrary cloud;

    /**
     * The list of string of apiKeys.
     */
    private final List<String> apiKeys;

    /**
     * Current index of the api Key used.
     */
    private final AtomicInteger apiKeyIndex = new AtomicInteger(0);

    /**
     * Constructor to create the key manager.
     */
    public GeminiKeyManager() {
        LOG.info("Constructing Key manager");
        this.cloud = new CloudFunctionLibrary();
        this.apiKeys = Collections.unmodifiableList(getKeyList());
    }

    /**
     * Get the next key available.
     * @return thw next key available
     */
    public String getCurrentKey() {
        LOG.info("Fetching current key");
        final int index = apiKeyIndex.get();
        return apiKeys.get(Math.abs(index));
    }

    /**
     * Using compare and swap, get the currently used keys index.
     * @param expiredKey the expired key - max token count reached
     */
    public void setKeyIndex(final String expiredKey) {
        LOG.debug("Fetching currently used key using compare and swap");
        final int currentIndex = apiKeyIndex.get();
        final String currentKey = apiKeys.get(Math.abs(currentIndex));
        if (currentKey.equals(expiredKey)) {
            apiKeyIndex.compareAndSet(currentIndex, currentIndex + 1);
            LOG.info("API key index: " + apiKeyIndex);
        }
    }

    /**
            * This method is used to get the list of API Keys.
     * @return list of Gemini API KEYS
     */
    private List<String> getKeyList() {
        final Entity req = new Entity("AI_INSIGHT", "credentials", "gemini_list", "Key",
                -1, new TimeRange(0, 0), null
        );

        final AtomicReference<Object> keyList = new AtomicReference<>();
        LOG.debug("Getting key list from Cloud");
        try {
            final Response response = cloud.cloudGet(req);

                final ObjectMapper objectMapper = new ObjectMapper();
                keyList.set(objectMapper.convertValue(
                        response.data(),
                        new TypeReference<List<String>>() { }
                ));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return (List<String>) keyList.get();
    }

    /**
     * This function is used to get the number of keys in the api key list.
     * @return number of keys in the key list
     */
    public int getNumberOfKeys() {
        return this.apiKeys.size();
    }
}
