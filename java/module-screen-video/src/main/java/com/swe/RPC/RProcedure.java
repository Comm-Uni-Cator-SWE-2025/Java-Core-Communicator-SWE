package com.swe.RPC;

/**
 * Represents a remote procedure that can be invoked via a RPC mechanism.
 */
public interface RProcedure {
    byte[] call(byte[] args);
}
