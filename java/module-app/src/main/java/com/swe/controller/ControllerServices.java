package com.swe.controller;

import com.swe.canvas.CanvasManager;
import com.swe.controller.canvas.CanvasNetworkService;
import functionlibrary.CloudFunctionLibrary;
import com.swe.core.Context;

public class ControllerServices {
    private static ControllerServices instance;

    public NetworkingInterface networking;
    public CloudFunctionLibrary cloud;
    public Context context;
    public CanvasNetworkService canvasNetworkService;
    public CanvasManager canvasManager;

    private ControllerServices() {
        context = Context.getInstance();
    }

    public static ControllerServices getInstance() {
        if (instance == null)
            instance = new ControllerServices();
        return instance;
    }
}



