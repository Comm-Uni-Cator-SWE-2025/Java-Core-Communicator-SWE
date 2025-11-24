package com.swe.core.Auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.swe.core.Meeting.ParticipantRole;
import com.swe.core.Meeting.UserProfile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.HttpsURLConnection;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class AuthHelperTest {

    private static final String GOOGLE_USERINFO = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final AtomicReference<String> RESPONSE_JSON = new AtomicReference<>(
        "{\"email\":\"default@example.com\",\"name\":\"Default\"}"
    );
    private static final AtomicBoolean HANDLER_INSTALLED = new AtomicBoolean(false);

    @BeforeClass
    public static void installHttpsHandler() throws Exception {
        if (HANDLER_INSTALLED.compareAndSet(false, true)) {
            try {
                URL.setURLStreamHandlerFactory(new GoogleHttpsInterceptor());
            } catch (Error alreadySet) {
                // Another test might have installed a handler already; ignore.
            }
        }
    }

    @After
    public void resetResponse() {
        RESPONSE_JSON.set("{\"email\":\"default@example.com\",\"name\":\"Default\"}");
    }

    @Test
    public void handleGoogleLoginParsesUserInfo() throws Exception {
        RESPONSE_JSON.set("{\"email\":\"student@example.com\",\"name\":\"Test User\"}");
        final Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
            .setAccessToken("token");

        final UserProfile profile = new AuthHelper().handleGoogleLogin(credential);

        assertEquals("student@example.com", profile.getEmail());
        assertEquals("Test User", profile.getDisplayName());
        assertSame(ParticipantRole.GUEST, profile.getRole());
    }

    @Test
    public void handleGoogleLoginReflectsLatestPayload() throws Exception {
        RESPONSE_JSON.set("{\"email\":\"other@example.com\",\"name\":\"Another User\"}");
        final Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
            .setAccessToken("token");

        final UserProfile profile = new AuthHelper().handleGoogleLogin(credential);

        assertEquals("other@example.com", profile.getEmail());
        assertEquals("Another User", profile.getDisplayName());
    }

    @Test(expected = NullPointerException.class)
    public void handleGoogleLoginWithNullCredentialThrows() throws Exception {
        new AuthHelper().handleGoogleLogin(null);
    }

    private static final class GoogleHttpsInterceptor implements URLStreamHandlerFactory {

        @Override
        public URLStreamHandler createURLStreamHandler(final String protocol) {
            if (!"https".equals(protocol)) {
                return null;
            }
            return new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(final URL url) throws IOException {
                    if (GOOGLE_USERINFO.equals(url.toString())) {
                        return new FakeHttpsURLConnection(url);
                    }
                    throw new UnsupportedOperationException("Unexpected HTTPS URL in test: " + url);
                }
            };
        }
    }

    private static final class FakeHttpsURLConnection extends HttpsURLConnection {

        protected FakeHttpsURLConnection(final URL url) {
            super(url);
        }

        @Override
        public void disconnect() {
            // no-op
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() {
            // no-op
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(RESPONSE_JSON.get().getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public int getResponseCode() {
            return 200;
        }

        @Override
        public String getResponseMessage() {
            return "OK";
        }

        @Override
        public Map<String, java.util.List<String>> getHeaderFields() {
            return Collections.emptyMap();
        }

        @Override
        public String getCipherSuite() {
            return "TLS_FAKE";
        }

        @Override
        public Certificate[] getLocalCertificates() {
            return new Certificate[0];
        }

        @Override
        public Certificate[] getServerCertificates() {
            return new Certificate[0];
        }

        @Override
        public Principal getPeerPrincipal() {
            return null;
        }

        @Override
        public Principal getLocalPrincipal() {
            return null;
        }

        @Override
        public InputStream getErrorStream() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public OutputStream getOutputStream() {
            return OutputStream.nullOutputStream();
        }
    }
}

