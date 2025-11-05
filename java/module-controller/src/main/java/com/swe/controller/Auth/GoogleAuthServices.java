package com.swe.controller.Auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Service class to handle Google OAuth2 authentication.
 * Provides method to obtain OAuth2 credentials for the user.
 */
public class GoogleAuthServices {

    /** Port for the local server receiver used during OAuth flow. */
    private static final int LOCAL_RECEIVER_PORT = 8888;

    /** Directory path to store OAuth2 tokens. */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /** JSON factory used by Google APIs. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** OAuth2 scopes required by the application. */
    private static final List<String> SCOPES = List.of(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email"
    );

    /** Path to the client credentials JSON file. */
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";

    /**
     * Returns a Google OAuth Credential object.
     *
     * @return Credential object with access and refresh tokens
     * @throws IOException              if credentials file is missing or cannot be read
     * @throws GeneralSecurityException if transport setup fails
     */
    public Credential getCredentials() throws IOException, GeneralSecurityException {
        final InputStream in = GoogleAuthServices.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }

        final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(in)
        );

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                SCOPES
        )
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        final LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(LOCAL_RECEIVER_PORT)
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
