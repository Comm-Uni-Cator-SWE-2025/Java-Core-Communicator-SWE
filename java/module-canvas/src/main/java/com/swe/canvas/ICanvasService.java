package com.swe.canvas;

public interface ICanvasService {
    byte[] submitAction(byte[] payload);

    byte[] getCanvasState(byte[] ignored);
}
