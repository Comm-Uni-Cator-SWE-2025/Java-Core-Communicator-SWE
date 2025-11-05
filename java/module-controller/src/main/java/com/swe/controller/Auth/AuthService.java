package com.swe.controller.Auth;

import com.google.api.client.auth.oauth2.Credential;
import com.swe.controller.Meeting.UserProfile;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Handles authentication and meeting management.
 */
public class AuthService {

    private final DataStore dataStore;

    public AuthService(DataStore dataStore) {
        this.dataStore = dataStore;
    }
    /**
     * Registers a new user with domain-based role validation.
     *
     * @return UserProfile if registration succeeds, null otherwise
     */
    public UserProfile register() throws GeneralSecurityException, IOException {
        final GoogleAuthServices googleAuthService = new GoogleAuthServices();
        final Credential credential = googleAuthService.getCredentials();

        final AuthHelper authHelper = new AuthHelper(dataStore);
        final UserProfile googleStudent = authHelper.handleGoogleLogin(credential);

//        dataStore.users.put(googleStudent.getUserId(), googleStudent);
        return googleStudent;
    }
}