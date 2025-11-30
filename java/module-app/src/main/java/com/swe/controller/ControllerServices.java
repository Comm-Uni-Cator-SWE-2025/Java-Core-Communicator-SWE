package com.swe.controller;

import functionlibrary.CloudFunctionLibrary;

import com.swe.aiinsights.apiendpoints.AiClientService;
import com.swe.core.Context;
import com.swe.canvas.CanvasManager;

/**
 * Central service container for controller components.
 * Provides singleton access to networking, cloud, AI, context, and canvas services.
 */
public class ControllerServices {
    /**
     * Singleton instance.
     */
    private static ControllerServices instance;

    /**
     * Networking interface for communication.
     */
    private NetworkingInterface networking;

    /**
     * Cloud function library for cloud operations.
     */
    private CloudFunctionLibrary cloud;

    /**
     * AI client service for AI operations.
     */
    private AiClientService ai;

    /**
     * Application context.
     */
    private Context context;

    /**
     * Canvas manager for canvas operations.
     */
    private CanvasManager canvasManager;

    private ControllerServices() {
        context = Context.getInstance();
    }

    /**
     * Gets the singleton instance of ControllerServices.
     *
     * @return The singleton instance
     */
    public static ControllerServices getInstance() {
        if (instance == null) {
            instance = new ControllerServices();
        }
        return instance;
    }

    /**
     * Gets the networking interface.
     *
     * @return The networking interface
     */
    public NetworkingInterface getNetworking() {
        return networking;
    }

    /**
     * Sets the networking interface.
     *
     * @param networkingParam The networking interface to set
     */
    public void setNetworking(final NetworkingInterface networkingParam) {
        this.networking = networkingParam;
    }

    /**
     * Gets the cloud function library.
     *
     * @return The cloud function library
     */
    public CloudFunctionLibrary getCloud() {
        return cloud;
    }

    /**
     * Sets the cloud function library.
     *
     * @param cloudParam The cloud function library to set
     */
    public void setCloud(final CloudFunctionLibrary cloudParam) {
        this.cloud = cloudParam;
    }

    /**
     * Gets the AI client service.
     *
     * @return The AI client service
     */
    public AiClientService getAi() {
        return ai;
    }

    /**
     * Sets the AI client service.
     *
     * @param aiParam The AI client service to set
     */
    public void setAi(final AiClientService aiParam) {
        this.ai = aiParam;
    }

    /**
     * Gets the application context.
     *
     * @return The application context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Sets the application context.
     *
     * @param contextParam The application context to set
     */
    public void setContext(final Context contextParam) {
        this.context = contextParam;
    }

    /**
     * Gets the canvas manager.
     *
     * @return The canvas manager
     */
    public CanvasManager getCanvasManager() {
        return canvasManager;
    }

    /**
     * Sets the canvas manager.
     *
     * @param canvasManagerParam The canvas manager to set
     */
    public void setCanvasManager(final CanvasManager canvasManagerParam) {
        this.canvasManager = canvasManagerParam;
    }
}
