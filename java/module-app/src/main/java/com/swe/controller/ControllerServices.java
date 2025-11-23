package com.swe.controller;

import functionlibrary.CloudFunctionLibrary;

import com.swe.aiinsights.apiendpoints.AiClientService;
import com.swe.core.Context;

public class ControllerServices {
    private static ControllerServices instance;

    public NetworkingInterface networking;
    public CloudFunctionLibrary cloud;
    public AiClientService ai;
    public Context context;

    private ControllerServices() {
        context = Context.getInstance();
    }

    public static ControllerServices getInstance() {
        if (instance == null)
            instance = new ControllerServices();
        return instance;
    }
}




