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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swe.aiinsights.logging.CommonLogger;
import com.swe.cloud.datastructures.Entity;
import com.swe.cloud.datastructures.TimeRange;
import com.swe.cloud.functionlibrary.CloudFunctionLibrary;
import org.slf4j.Logger;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The key manager class to get Gemini Key from cloud.
 */
public final class GeminiKeyManager {
    /**
     * Get the log file path.
     */
    private static final Logger LOG = CommonLogger.getLogger(GeminiKeyManager.class);
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
            apiKeyIndex.compareAndSet(currentIndex, (currentIndex + 1) % apiKeys.size());
            // System.out.println(apiKeyIndex);
        }
    }

    /**
     * This method is used to get the Ollama URL from cloud.
     */
    public static String getOllamaUrl() {
        LOG.info("Fetching Ollama URL from cloud\n");
        final Entity req = new Entity("AI_INSIGHT", "credentials", "ollama_url", "Key",
                -1, new TimeRange(0, 0), null
        );
        try {
            final CloudFunctionLibrary cloud = new CloudFunctionLibrary();
            return cloud.cloudGet(req)
                .thenApply(response -> response.data())  
                .thenApply(Object::toString)            
                .get();  
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
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
        cloud.cloudGet(req).thenAccept(response -> {
            final ObjectMapper objectMapper = new ObjectMapper();
            keyList.set(objectMapper.convertValue(
                    response.data(),
                    new TypeReference<List<String>>() { }
            ));
        }).join();
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
