package com.swe.aiinsights.modeladapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.aiinsights.generaliser.RequestGeneraliser;
import okhttp3.Response;

import java.io.IOException;

public interface ModelAdapter {

    public String buildRequest(RequestGeneraliser req) throws JsonProcessingException;


    String getResponse(Response response) throws IOException;
}
