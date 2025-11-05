package com.swe.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface MessageListener {
    void receiveData(byte[] data) throws JsonProcessingException;
}
