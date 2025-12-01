package com.swe.core;

import com.swe.core.Meeting.MeetingSession;
import com.swe.core.Meeting.UserProfile;

/**
 * Application context singleton.
 * Holds shared state for RPC, user profile, and meeting session.
 */
public class Context {
    /**
     * Singleton instance.
     */
    private static Context instance;

    /**
     * RPC instance.
     */
    private RPC rpc;

    /**
     * Current user profile.
     */
    private UserProfile self;

    /**
     * Self IP address.
     */
    private ClientNode selfIP;

    /**
     * Main server IP address.
     */
    private ClientNode mainServerIP;

    /**
     * Current meeting session.
     */
    private MeetingSession meetingSession;

    private Context() {
    }

    /**
     * Gets the singleton instance.
     *
     * @return The singleton instance
     */
    public static Context getInstance() {
        if (instance == null) {
            instance = new Context();
        }
        return instance;
    }

    /**
     * Gets the RPC instance.
     *
     * @return The RPC instance
     */
    public RPC getRpc() {
        return rpc;
    }

    /**
     * Sets the RPC instance.
     *
     * @param rpcParam The RPC instance to set
     */
    public void setRpc(final RPC rpcParam) {
        this.rpc = rpcParam;
    }

    /**
     * Gets the current user profile.
     *
     * @return The current user profile
     */
    public UserProfile getSelf() {
        return self;
    }

    /**
     * Sets the current user profile.
     *
     * @param selfParam The user profile to set
     */
    public void setSelf(final UserProfile selfParam) {
        this.self = selfParam;
    }

    /**
     * Gets the self IP address.
     *
     * @return The self IP address
     */
    public ClientNode getSelfIP() {
        return selfIP;
    }

    /**
     * Sets the self IP address.
     *
     * @param selfIPParam The self IP address to set
     */
    public void setSelfIP(final ClientNode selfIPParam) {
        this.selfIP = selfIPParam;
    }

    /**
     * Gets the main server IP address.
     *
     * @return The main server IP address
     */
    public ClientNode getMainServerIP() {
        return mainServerIP;
    }

    /**
     * Sets the main server IP address.
     *
     * @param mainServerIPParam The main server IP address to set
     */
    public void setMainServerIP(final ClientNode mainServerIPParam) {
        this.mainServerIP = mainServerIPParam;
    }

    /**
     * Gets the current meeting session.
     *
     * @return The current meeting session
     */
    public MeetingSession getMeetingSession() {
        return meetingSession;
    }

    /**
     * Sets the current meeting session.
     *
     * @param meetingSessionParam The meeting session to set
     */
    public void setMeetingSession(final MeetingSession meetingSessionParam) {
        this.meetingSession = meetingSessionParam;
    }
}
