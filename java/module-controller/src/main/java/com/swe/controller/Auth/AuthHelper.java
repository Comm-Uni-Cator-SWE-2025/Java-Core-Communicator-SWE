package com.swe.controller.Auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.swe.controller.Meeting.ParticipantRole;
import com.swe.controller.Meeting.UserProfile;

import java.io.IOException;
import java.util.Map;

/**
 * Helper class to handle Google OAuth login.
 */
public class AuthHelper {

    /** DataStore used to save user profiles. */
    private final DataStore dataStore;

    /** Google API URL to fetch user info. */
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    /**
     * Constructor.
     *
     * @param store DataStore to save user profiles
     */
    public AuthHelper(final DataStore store) {
        this.dataStore = store;
    }

    /**
     * Handles Google login using a Credential object.
     *
     * @param credential OAuth2 credential containing access token
     * @return UserProfile created or retrieved from DataStore
     * @throws IOException if HTTP request or JSON parsing fails
     */
    public UserProfile handleGoogleLogin(final Credential credential) throws IOException {

        final HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(credential);

        final GenericUrl url = new GenericUrl(GOOGLE_USERINFO_URL);

        final HttpRequest request = requestFactory.buildGetRequest(url);
        final HttpResponse response = request.execute();

        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> userInfo = mapper.readValue(response.getContent(), Map.class);

        final String email = (String) userInfo.get("email");
        final String name = (String) userInfo.get("name");
        final String logoUrl = (String) userInfo.get("picture");

        final UserProfile user = new UserProfile(email, name, logoUrl, ParticipantRole.GUEST);
        dataStore.addUser(user);

        return user;
    }
}
