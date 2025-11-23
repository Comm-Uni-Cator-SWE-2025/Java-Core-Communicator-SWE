package com.swe.core.Meeting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Represents a user's core, permanent profile in the system.
 * This object is the single source of truth for a user's identity.
 * It is created during registration and persists between sessions.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfile {

    /**
     * The user's email address, used as their login username.
     */
    @JsonProperty("email")
    private String email;

    /**
     * The user's display name.
     */
    @JsonProperty("displayName")
    private String displayName;

    /**
     * The user's role, either "student" or "instructor".
     */
    @JsonProperty("role")
    private ParticipantRole role;

    /**
     * Default constructor.
     * Required for frameworks like Jackson for deserialization.
     */
    public UserProfile() {
        // Default constructor
    }

    /**
     * Constructs a new UserProfile.
     * This is the 5-argument constructor expected by AuthService and UserAnalytics.
     *
     * @param finalEmail        The user's unique email address.
     * @param finalDisplayName  The user's display name.
     * @param finalUserRole     The user's role (e.g., "student").
     */
    public UserProfile(final String finalEmail,
                       final String finalDisplayName,
                       final ParticipantRole finalUserRole) {
        this.email = finalEmail;
        this.displayName = finalDisplayName;
        this.role = finalUserRole;
    }

    // --- Getters (These match the methods expected by the rest of the code) ---

    /**
     * Gets the user's email address.
     *
     * @return The user email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the user's display name.
     *
     * @return The display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the user's role.
     *
     * @return The user role.
     */
    public ParticipantRole getRole() {
        return role;
    }

    // --- Setters ---

    /**
     * Sets the user's display name.
     *
     * @param finalDisplayName The new display name.
     */
    public void setDisplayName(final String finalDisplayName) {
        this.displayName = finalDisplayName;
    }

    /**
     * Sets the user's email address.
     *
     * @param finalEmail The new user email.
     */
    public void setEmail(final String finalEmail) {
        this.email = finalEmail;
    }

    /**
     * Sets the user's role.
     *
     * @param finalRole The new user role.
     */
    public void setRole(final ParticipantRole finalRole) {
        this.role = finalRole;
    }

    // --- Methods expected by DataStore.java ---

    // --- Overridden Methods (for Checkstyle and Robustness) ---

    /**
     * Returns a string representation of the UserProfile object for logging.
     * Note: The passwordHash is intentionally omitted for security.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        return "UserProfile{"
                + ", email='" + email + '\''
                + ", displayName='" + displayName + '\''
                + ", role='" + role + '\''
                + '}';
    }

    /**
     * Compares this UserProfile to another object for equality.
     *
     * @param obj The object to compare with.
     * @return True if the objects are equal (all fields match), false otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final UserProfile that = (UserProfile) obj;
        return Objects.equals(email, that.email)
                && Objects.equals(displayName, that.displayName)
                && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, displayName, role);
    }
}