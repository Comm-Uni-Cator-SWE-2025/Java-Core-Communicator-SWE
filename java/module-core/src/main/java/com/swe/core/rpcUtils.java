/**
 * RPC utility constants.
 * Contributed by Pushti Vasoya.
 */
package com.swe.core;

/**
 * RPC utility constants.
 */
// CHECKSTYLE:OFF: TypeName
public final class rpcUtils {

    /**
     * Register RPC method name.
     */
    // CHECKSTYLE:OFF: StaticVariableName
    // CHECKSTYLE:OFF: VisibilityModifier
    public static final String REGISTER = "Controller-Register";

    /**
     * Create meeting RPC method name.
     */
    public static final String CREATE_MEETING = "Controller-CreateMeet";

    /**
     * Join meeting RPC method name.
     */
    public static final String JOIN_MEETING = "Controller-JoinMeet";
    // CHECKSTYLE:ON: StaticVariableName
    // CHECKSTYLE:ON: VisibilityModifier

    private rpcUtils() {
    }
}
// CHECKSTYLE:ON: TypeName
