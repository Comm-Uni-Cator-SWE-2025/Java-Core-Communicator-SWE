package com.swe.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swe.ScreenNVideo.MediaCaptureManager;
import com.swe.aiinsights.aiinstance.AiInstance;
import com.swe.aiinsights.apiendpoints.AiClientService;
import com.swe.canvas.CanvasManager;
import com.swe.chat.ChatManager;
import com.swe.chat.ChatMessage;
import com.swe.core.Auth.AuthService;
import com.swe.core.ClientNode;
import com.swe.core.Meeting.MeetingSession;
import com.swe.core.Meeting.SessionMode;
import com.swe.core.Meeting.UserProfile;
import com.swe.core.RPC;
import com.swe.core.serialize.DataSerializer;
import com.swe.core.logging.SweLogger;
import com.swe.core.logging.SweLoggerFactory;
import com.swe.networking.Networking;
import functionlibrary.CloudFunctionLibrary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Initialization class for the controller application.
 * Sets up networking, RPC, and various service managers.
 */
// CHECKSTYLE:OFF: ClassDataAbstractionCoupling
// CHECKSTYLE:OFF: ClassFanOutComplexity
public class Init {
    static ChatManager chatmanager;

    /**
     * Default RPC port number.
     */
    private static final int DEFAULT_RPC_PORT = 6942;

    /**
     * Default media capture port.
     */
    private static final int DEFAULT_MEDIA_PORT = 6943;

    /**
     * Default capture frame rate.
     */
    private static final int DEFAULT_CAPTURE_FPS = 10;

    /**
     * Index for AI operations.
     */
    private static int indexAi = 0;

    /**
     * Logger for initialization operations.
     */
    private static final SweLogger LOG = SweLoggerFactory.getLogger("CONTROLLER-APP");

    /**
     * Main entry point for the controller application.
     *
     * @param args Command line arguments (optional port number)
     * @throws Exception If initialization fails
     */
    public static void main(final String[] args) throws Exception {

        int portNumber = DEFAULT_RPC_PORT;

        if (args.length > 0) {
            final String port = args[0];
            portNumber = Integer.parseInt(port);
        }

        final Level consoleLevel = resolveConsoleLevel();
        SweLoggerFactory.setConsoleLevel(consoleLevel);
        LOG.info("Console log level set to " + consoleLevel.getName());

        final SweLogger chatLogger = SweLoggerFactory.getLogger("CHAT");
        final SweLogger canvasLogger = SweLoggerFactory.getLogger("CANVAS");
        final SweLogger screenLogger = SweLoggerFactory.getLogger("SCREEN-VIDEO");

        final RPC rpc = new RPC();
        final CloudFunctionLibrary cloud = new CloudFunctionLibrary();
        final AiClientService service = AiInstance.getInstance();

        final ControllerServices controllerServices = ControllerServices.getInstance();
        controllerServices.getContext().setRpc(rpc);
        controllerServices.setCloud(cloud);
        controllerServices.setAi(service);

        // Provide RPC somehow here
        final NetworkingInterface networking = new NetworkingAdapter(Networking.getNetwork());
        networking.consumeRPC(rpc);

        controllerServices.setNetworking(networking);
        MeetingNetworkingCoordinator.initialize(networking);

        chatmanager = new ChatManager(Networking.getNetwork(), chatLogger);
        controllerServices.setCanvasManager(new CanvasManager(Networking.getNetwork(), canvasLogger));

        final MediaCaptureManager mediaCaptureManager =
            new MediaCaptureManager(Networking.getNetwork(), DEFAULT_MEDIA_PORT, screenLogger);
        final Thread mediaCaptureManagerThread = new Thread(() -> {
            try {
                mediaCaptureManager.startCapture(DEFAULT_CAPTURE_FPS);
            } catch (ExecutionException | InterruptedException e) {
                LOG.error("Media capture manager stopped unexpectedly", e);
                throw new RuntimeException(e);
            }
        });
        mediaCaptureManagerThread.start();
        LOG.info("Media Capture Manager started.");

        addRPCSubscriptions(rpc);

        // We need to get all subscriptions from frontend to also finish before this
        final Thread rpcThread = rpc.connect(portNumber);

        rpcThread.join();
        mediaCaptureManagerThread.join();
    }

    /**
     * Adds RPC subscriptions for core operations.
     *
     * @param rpc The RPC instance to subscribe to
     */
    // CHECKSTYLE:OFF: CyclomaticComplexity
    // CHECKSTYLE:OFF: MethodLength
    // CHECKSTYLE:OFF: JavaNCSS
    // CHECKSTYLE:OFF: NPathComplexity
    private static void addRPCSubscriptions(final RPC rpc) {
        final ControllerServices controllerServices = ControllerServices.getInstance();

        rpc.subscribe("core/register", (byte[] userData) -> {
            LOG.info("Registering user");
            UserProfile registeredUser = null;
            try {
                registeredUser = AuthService.register();
                LOG.info("Registered user with emailId: " + registeredUser.getEmail());
            } catch (GeneralSecurityException | IOException e) {
                LOG.error("Error registering user", e);
                return new byte[0];
            }

            controllerServices.getContext().setSelf(registeredUser);

            try {
                return DataSerializer.serialize(registeredUser);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        rpc.subscribe("core/createMeeting", (byte[] meetMode) -> {
            LOG.info("[CONTROLLER] Creating meeting");
            final MeetingSession meetingSession = MeetingServices.createMeeting(
                controllerServices.getContext().getSelf(), SessionMode.CLASS);
            controllerServices.getContext().setMeetingSession(meetingSession);

            try {
                final ClientNode localClientNode = Utils.getLocalClientNode();

                Utils.setServerClientNode(meetingSession.getMeetingId(), controllerServices.getCloud());
                controllerServices.getNetworking().addUser(localClientNode, localClientNode);

                // Initialize Canvas Manager for Host
                controllerServices.getCanvasManager().setIsHost(true);
                controllerServices.getCanvasManager().setHostClientNode(localClientNode);

                MeetingNetworkingCoordinator.handleMeetingCreated(meetingSession);
            } catch (Exception e) {
                LOG.error("Error initializing networking for meeting host", e);
                throw new RuntimeException(e);
            }

            try {
                LOG.info("Returning meeting session");
                final byte[] serializedMeetingSession = DataSerializer.serialize(meetingSession);
                LOG.debug("Serialized meeting session: "
                    + new String(serializedMeetingSession, StandardCharsets.UTF_8));
                return serializedMeetingSession;
            } catch (Exception e) {
                LOG.error("Error serializing meeting session", e);
                throw new RuntimeException(e);
            }
        });

        rpc.subscribe("core/joinMeeting", (byte[] meetId) -> {
            final String id;
            try {
                id = DataSerializer.deserialize(meetId, String.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            LOG.info("Joining meeting with id: " + id);

            try {
                final ClientNode localClientNode = Utils.getLocalClientNode();
                final ClientNode serverClientNode =
                    Utils.getServerClientNode(id, controllerServices.getCloud());
                LOG.debug("Server client node: " + serverClientNode);

                controllerServices.getNetworking().addUser(localClientNode, serverClientNode);

                // Initialize Canvas Manager for Client
                controllerServices.getCanvasManager().setIsHost(false);
                controllerServices.getCanvasManager().setHostClientNode(serverClientNode);

                MeetingNetworkingCoordinator.handleMeetingJoin(id, serverClientNode);
            } catch (Exception e) {
                LOG.error("Error getting server client node", e);
                throw new RuntimeException(e);
            }

            return meetId;
        });

        rpc.subscribe("core/logout", (byte[] userData) -> {
            LOG.info("Logging out user");
            try {
                AuthService.logout();
                LOG.info("User logged out successfully");
                // Clear the current user profile from context
                controllerServices.getContext().setSelf(null);
                return "Logged out successfully".getBytes(StandardCharsets.UTF_8);
            } catch (GeneralSecurityException | IOException e) {
                LOG.error("Error logging out user", e);
                return ("Error logging out: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
            }
        });

        rpc.subscribe("core/endMeeting", (byte[] data) -> {
            LOG.info("Ending meeting");
            try {
                Networking.getNetwork().closeNetworking();
                LOG.info("Meeting ended successfully");
                // Clear the meeting session from context
                controllerServices.getContext().setMeetingSession(null);
                return "Meeting ended successfully".getBytes(StandardCharsets.UTF_8);
            } catch (Exception e) {
                LOG.error("Error ending meeting", e);
                return ("Error ending meeting: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
            }
        });

        rpc.subscribe("core/AiSentiment", (byte[] data) -> {
            try {
                LOG.info("Performing Sentiment Analysis");
                final List<ChatMessage> messages = chatmanager.getAnalyticsService().getFullMessageHistory();
                final String cache = chatmanager.getAnalyticsService().generateChatHistoryJson(messages);
                final ObjectMapper mapper = new ObjectMapper();
                final JsonNode cachNode = mapper.readTree(cache);
                LOG.debug("Chat History JSON for Sentiment: " + cachNode);
                final String val = cleanMarkdownJson(controllerServices.getAi().sentiment(cachNode).get());

                LOG.info("Sentiment Analysis Result: " + val);
                indexAi = messages.size();
                return DataSerializer.serialize(val);
            } catch (Exception e) {
                LOG.error("Sentiment analysis failed", e);
                return new byte[0];
            }
        });

        rpc.subscribe("core/AiAction", (byte[] data) -> {
            try {
                LOG.info("Generating Action Items");
                final List<ChatMessage> messages = chatmanager.getAnalyticsService().getFullMessageHistory();
                final String cache = chatmanager.getAnalyticsService().generateChatHistoryJson(messages);

                final ObjectMapper mapper = new ObjectMapper();
                final JsonNode cachNode = mapper.readTree(cache);
                LOG.debug("Chat History JSON for Action: " + cachNode);
                final String val = cleanMarkdownJson(controllerServices.getAi().action(cachNode).get());

                LOG.info("Action Analysis Result: " + val);
                return DataSerializer.serialize(val);
            } catch (Exception e) {
                LOG.error("Action item generation failed", e);
                return new byte[0];
            }
        });
    }
    // CHECKSTYLE:ON: CyclomaticComplexity
    // CHECKSTYLE:ON: MethodLength
    // CHECKSTYLE:ON: JavaNCSS
    // CHECKSTYLE:ON: NPathComplexity

    private static Level resolveConsoleLevel() {
        final String configuredLevel = System.getProperty("swecomm.console.level", "INFO")
                .toUpperCase(Locale.ROOT);
        try {
            return Level.parse(configuredLevel);
        } catch (IllegalArgumentException ex) {
            LOG.warn("Unsupported console level '" + configuredLevel + "', defaulting to INFO");
            return Level.INFO;
        }
    }

    /**
     * Cleans markdown formatting from JSON strings.
     *
     * @param raw The raw string to clean
     * @return The cleaned string
     */
    public static String cleanMarkdownJson(final String raw) {
        if (raw == null) {
            return "";
        }

        String cleaned = raw.trim();

        // Remove leading/trailing quotes
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        // Remove ```json and ``` markers
        cleaned = cleaned.replace("```json", "").replace("```", "").trim();

        return cleaned;
    }
}