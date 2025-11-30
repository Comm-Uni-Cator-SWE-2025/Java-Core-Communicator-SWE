/**
 *  Contributed by Pushti Vasoya.
 */

package com.swe.core.Auth;

import com.google.api.client.auth.oauth2.Credential;
import com.swe.core.Meeting.UserProfile;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Handles authentication and meeting management.
 */
public class AuthService {

    public AuthService() {}
    /**
     * Registers a new user with domain-based role validation.
     *
     * @return UserProfile if registration succeeds, null otherwise
     */
    public static UserProfile register() throws GeneralSecurityException, IOException {
        final GoogleAuthServices googleAuthService = new GoogleAuthServices();
        final Credential credential = googleAuthService.getCredentials();

        final AuthHelper authHelper = new AuthHelper();

//        dataStore.users.put(googleStudent.getUserId(), googleStudent);
        return authHelper.handleGoogleLogin(credential);
    }

    /**
     * Logs out the current user by clearing stored OAuth2 tokens.
     * This will force a new login prompt on the next authentication attempt.
     *
     * @throws IOException              if logout fails
     * @throws GeneralSecurityException if logout fails
     */
    public static void logout() throws GeneralSecurityException, IOException {
        final GoogleAuthServices googleAuthService = new GoogleAuthServices();
        googleAuthService.logout();
    }
}