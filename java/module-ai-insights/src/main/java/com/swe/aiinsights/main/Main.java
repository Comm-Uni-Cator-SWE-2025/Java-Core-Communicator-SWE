package com.swe.aiinsights.main;

import com.swe.aiinsights.apiendpoints.AiClientService;
import com.swe.aiinsights.aiinstance.AiInstance;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;


import java.io.IOException;

/**
 * Main class for running the Image Interpreter application.
 *
 * <p>This class loads the necessary classes,
 * prepares the request, and calls the Gemini API.
 * </p>
 */
public class Main {
    /**+
     * Entry point of application.
     *
     * @param args  arguments of main
     * @throws IOException throws error if any of the implementation fails
     */
    public static void main(final String[] args) throws IOException, URISyntaxException {
        AiClientService service = AiInstance.getInstance();
        URL url = Main.class.getClassLoader().getResource("images/test.png");
        Path file = Paths.get(url.toURI());
        // CompletableFuture<String> resp = service.describe(file);

        String points = "{\n" +
                "  \"ShapeId\": \"c585b84a-d56c-45b8-a0e1-827ae20a014a\",\n" +
                "  \"Type\": \"FreeHand\",\n" +
                "  \"Points\": [\n" +
                "    {\n" +
                "      \"X\": 450,\n" +
                "      \"Y\": 60\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 449,\n" +
                "      \"Y\": 60\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 446,\n" +
                "      \"Y\": 59\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 445,\n" +
                "      \"Y\": 58\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 443,\n" +
                "      \"Y\": 58\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 441,\n" +
                "      \"Y\": 58\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 439,\n" +
                "      \"Y\": 57\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 436,\n" +
                "      \"Y\": 57\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 435,\n" +
                "      \"Y\": 57\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 433,\n" +
                "      \"Y\": 57\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 431,\n" +
                "      \"Y\": 57\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 430,\n" +
                "      \"Y\": 57\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 428,\n" +
                "      \"Y\": 57\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 427,\n" +
                "      \"Y\": 57\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 425,\n" +
                "      \"Y\": 57\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 423,\n" +
                "      \"Y\": 57\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 422,\n" +
                "      \"Y\": 58\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 420,\n" +
                "      \"Y\": 59\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 419,\n" +
                "      \"Y\": 59\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 417,\n" +
                "      \"Y\": 59\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 416,\n" +
                "      \"Y\": 60\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 414,\n" +
                "      \"Y\": 60\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 413,\n" +
                "      \"Y\": 62\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 412,\n" +
                "      \"Y\": 62\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 411,\n" +
                "      \"Y\": 63\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 409,\n" +
                "      \"Y\": 65\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 408,\n" +
                "      \"Y\": 65\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 407,\n" +
                "      \"Y\": 67\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 405,\n" +
                "      \"Y\": 68\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 404,\n" +
                "      \"Y\": 70\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 403,\n" +
                "      \"Y\": 71\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 402,\n" +
                "      \"Y\": 74\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 401,\n" +
                "      \"Y\": 77\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 400,\n" +
                "      \"Y\": 80\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 399,\n" +
                "      \"Y\": 82\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 397,\n" +
                "      \"Y\": 86\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 396,\n" +
                "      \"Y\": 89\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 396,\n" +
                "      \"Y\": 92\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 395,\n" +
                "      \"Y\": 94\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 394,\n" +
                "      \"Y\": 97\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 393,\n" +
                "      \"Y\": 99\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 393,\n" +
                "      \"Y\": 101\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 392,\n" +
                "      \"Y\": 104\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 392,\n" +
                "      \"Y\": 107\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 392,\n" +
                "      \"Y\": 109\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 392,\n" +
                "      \"Y\": 112\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 392,\n" +
                "      \"Y\": 115\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 392,\n" +
                "      \"Y\": 117\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 392,\n" +
                "      \"Y\": 120\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 392,\n" +
                "      \"Y\": 123\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 392,\n" +
                "      \"Y\": 125\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 393,\n" +
                "      \"Y\": 129\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 394,\n" +
                "      \"Y\": 131\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 395,\n" +
                "      \"Y\": 133\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 395,\n" +
                "      \"Y\": 136\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 396,\n" +
                "      \"Y\": 138\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 397,\n" +
                "      \"Y\": 141\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 398,\n" +
                "      \"Y\": 143\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 399,\n" +
                "      \"Y\": 145\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 400,\n" +
                "      \"Y\": 147\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 401,\n" +
                "      \"Y\": 149\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 402,\n" +
                "      \"Y\": 150\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 403,\n" +
                "      \"Y\": 152\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 404,\n" +
                "      \"Y\": 153\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 406,\n" +
                "      \"Y\": 155\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 406,\n" +
                "      \"Y\": 157\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 408,\n" +
                "      \"Y\": 159\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 410,\n" +
                "      \"Y\": 160\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 410,\n" +
                "      \"Y\": 162\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 412,\n" +
                "      \"Y\": 163\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 414,\n" +
                "      \"Y\": 165\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 415,\n" +
                "      \"Y\": 166\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 418,\n" +
                "      \"Y\": 169\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 419,\n" +
                "      \"Y\": 170\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 422,\n" +
                "      \"Y\": 171\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 423,\n" +
                "      \"Y\": 173\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 426,\n" +
                "      \"Y\": 174\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 428,\n" +
                "      \"Y\": 175\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 430,\n" +
                "      \"Y\": 176\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 433,\n" +
                "      \"Y\": 177\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 435,\n" +
                "      \"Y\": 177\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 438,\n" +
                "      \"Y\": 178\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 441,\n" +
                "      \"Y\": 179\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 444,\n" +
                "      \"Y\": 179\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 448,\n" +
                "      \"Y\": 179\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 450,\n" +
                "      \"Y\": 179\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 452,\n" +
                "      \"Y\": 179\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 456,\n" +
                "      \"Y\": 179\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 459,\n" +
                "      \"Y\": 178\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 461,\n" +
                "      \"Y\": 178\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 465,\n" +
                "      \"Y\": 176\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 467,\n" +
                "      \"Y\": 175\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 470,\n" +
                "      \"Y\": 175\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 472,\n" +
                "      \"Y\": 174\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 475,\n" +
                "      \"Y\": 172\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 476,\n" +
                "      \"Y\": 171\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 479,\n" +
                "      \"Y\": 170\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 480,\n" +
                "      \"Y\": 168\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 482,\n" +
                "      \"Y\": 167\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 483,\n" +
                "      \"Y\": 165\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 486,\n" +
                "      \"Y\": 163\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 487,\n" +
                "      \"Y\": 162\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 488,\n" +
                "      \"Y\": 160\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 490,\n" +
                "      \"Y\": 159\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 490,\n" +
                "      \"Y\": 156\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 492,\n" +
                "      \"Y\": 155\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 494,\n" +
                "      \"Y\": 152\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 494,\n" +
                "      \"Y\": 150\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 496,\n" +
                "      \"Y\": 148\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 497,\n" +
                "      \"Y\": 144\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 498,\n" +
                "      \"Y\": 142\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 499,\n" +
                "      \"Y\": 138\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 499,\n" +
                "      \"Y\": 136\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 499,\n" +
                "      \"Y\": 134\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 500,\n" +
                "      \"Y\": 131\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 500,\n" +
                "      \"Y\": 130\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 500,\n" +
                "      \"Y\": 127\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 500,\n" +
                "      \"Y\": 126\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 500,\n" +
                "      \"Y\": 124\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 500,\n" +
                "      \"Y\": 121\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 500,\n" +
                "      \"Y\": 119\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 500,\n" +
                "      \"Y\": 117\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 500,\n" +
                "      \"Y\": 113\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 500,\n" +
                "      \"Y\": 111\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 500,\n" +
                "      \"Y\": 109\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 500,\n" +
                "      \"Y\": 105\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 500,\n" +
                "      \"Y\": 102\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 499,\n" +
                "      \"Y\": 99\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 498,\n" +
                "      \"Y\": 97\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 497,\n" +
                "      \"Y\": 94\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 496,\n" +
                "      \"Y\": 91\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 495,\n" +
                "      \"Y\": 88\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 494,\n" +
                "      \"Y\": 85\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 493,\n" +
                "      \"Y\": 83\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 492,\n" +
                "      \"Y\": 80\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 491,\n" +
                "      \"Y\": 77\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 490,\n" +
                "      \"Y\": 75\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 489,\n" +
                "      \"Y\": 72\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 488,\n" +
                "      \"Y\": 70\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 486,\n" +
                "      \"Y\": 67\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 486,\n" +
                "      \"Y\": 66\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 485,\n" +
                "      \"Y\": 64\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 484,\n" +
                "      \"Y\": 63\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 483,\n" +
                "      \"Y\": 60\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 481,\n" +
                "      \"Y\": 59\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 480,\n" +
                "      \"Y\": 59\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 479,\n" +
                "      \"Y\": 57\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 477,\n" +
                "      \"Y\": 56\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 476,\n" +
                "      \"Y\": 55\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 475,\n" +
                "      \"Y\": 55\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 474,\n" +
                "      \"Y\": 53\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 472,\n" +
                "      \"Y\": 52\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 470,\n" +
                "      \"Y\": 51\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 469,\n" +
                "      \"Y\": 51\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 467,\n" +
                "      \"Y\": 50\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 465,\n" +
                "      \"Y\": 50\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 463,\n" +
                "      \"Y\": 49\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 462,\n" +
                "      \"Y\": 49\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 460,\n" +
                "      \"Y\": 49\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 459,\n" +
                "      \"Y\": 49\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 458,\n" +
                "      \"Y\": 49\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 456,\n" +
                "      \"Y\": 49\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 454,\n" +
                "      \"Y\": 49\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 451,\n" +
                "      \"Y\": 49\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 449,\n" +
                "      \"Y\": 50\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 447,\n" +
                "      \"Y\": 50\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 445,\n" +
                "      \"Y\": 50\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 443,\n" +
                "      \"Y\": 51\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 441,\n" +
                "      \"Y\": 52\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 440,\n" +
                "      \"Y\": 53\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 438,\n" +
                "      \"Y\": 53\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 437,\n" +
                "      \"Y\": 54\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 435,\n" +
                "      \"Y\": 54\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 434,\n" +
                "      \"Y\": 55\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 433,\n" +
                "      \"Y\": 55\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 432,\n" +
                "      \"Y\": 56\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 431,\n" +
                "      \"Y\": 57\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 430,\n" +
                "      \"Y\": 58\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 429,\n" +
                "      \"Y\": 58\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 428,\n" +
                "      \"Y\": 59\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 427,\n" +
                "      \"Y\": 60\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 427,\n" +
                "      \"Y\": 61\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 426,\n" +
                "      \"Y\": 61\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 426,\n" +
                "      \"Y\": 62\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 425,\n" +
                "      \"Y\": 62\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 425,\n" +
                "      \"Y\": 63\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 424,\n" +
                "      \"Y\": 63\n" +
                "    },\n" +
                "    {\n" +
                "      \"X\": 424,\n" +
                "      \"Y\": 64\n" +
                "    }\n" +
                "  ],\n" +
                "  \"Color\": \"#FF000000\",\n" +
                "  \"Thickness\": 2,\n" +
                "  \"CreatedBy\": \"user_default\",\n" +
                "  \"LastModifiedBy\": \"user_default\",\n" +
                "  \"IsDeleted\": false\n" +
                "}";
        CompletableFuture<String> reg = service.regularise(points);

        String contextualQ = " What is Bob working on?";
        CompletableFuture<String> answer1 = service.answerQuestion(contextualQ);
        answer1.thenAccept(System.out::println);

        String chatJson1 = "[\n"
                + " {\"sender\": \"Alice\", \"message\": "
                +"\"Hey, how are you?\"},\n"
                + " {\"sender\": \"Bob\", \"message\":"
                +" \"I'm good! Just working on the llm project.\"},\n"
                + " {\"sender\": \"Alice\", \"message\": "
                +"\"That's great! Need any help?\"},\n"
                + " {\"sender\": \"Bob\", \"message\": "
                +"\"I'm almost done, thanks!\"}\n"
                + "]";
        CompletableFuture<String> summary = service.summariseText(chatJson1);
//        summary.thenAccept(System.out::println);
        String chatJson = "[\n"
                + " {\"sender\": \"Gouthami\", \"message\": "
                +"\"Hey, how are you?\"},\n"
                + " {\"sender\": \"Bob\", \"message\": "
                +"\"I'm good! Just working on the project.\"},\n"
                + " {\"sender\": \"Alice\", \"message\": "
                +"\"That's great! Need any help?\"},\n"
                + " {\"sender\": \"Bob\", \"message\": "
                +"\"I'm almost done, thanks!\"}\n"
                + "]";
        CompletableFuture<String> summary2 = service.summariseText(chatJson);

        String chatJson0 = "[\n"
                + " {\"sender\": \"jayati\", \"message\": \"Hey, how are you?\"},\n"
                + " {\"sender\": \"Bob\", \"message\": \"I'm good! Just working on the llm project.\"},\n"
                + " {\"sender\": \"Alice\", \"message\": \"That's great! Need any help?\"},\n"
                + " {\"sender\": \"Bob\", \"message\": \"I'm almost done, thanks!\"}\n"
                + "]";
        CompletableFuture<String> summary5 = service.summariseText(chatJson0);
        summary.thenAccept(System.out::println);
        summary2.thenAccept(System.out::println);
        summary5.thenAccept(System.out::println);



        // 2. Generic Question (LLM should ignore the summary)
        String genericQ = " is jayati in meeting?";
        CompletableFuture<String> answer2 = service.answerQuestion(genericQ);
        answer2.thenAccept(System.out::println);
        reg.thenAccept(System.out::println);
        ObjectMapper mapper = new ObjectMapper();

        JsonNode chat_data = mapper.readTree("""
                                {
  "messages": [
    {
      "from": "student",
      "to": "teacher",
      "timestamp": "2025-11-07T10:00:00Z",
      "message": "I am really excited about today's class!"
    },
    {
      "from": "teacher",
      "to": "student",
      "timestamp": "2025-11-07T10:01:45Z",
      "message": "I'm glad to hear that. Let's make it a productive session."
    },
    {
      "from": "student",
      "to": "teacher",
      "timestamp": "2025-11-07T10:03:20Z",
      "message": "Lately, I have been feeling a little overwhelmed with assignments."
    },
    {
      "from": "teacher",
      "to": "student",
      "timestamp": "2025-11-07T10:04:50Z",
      "message": "I understand. It's okay to feel that. We can work through it together."
    },
    {
      "from": "student",
      "to": "teacher",
      "timestamp": "2025-11-07T10:06:10Z",
      "message": "Thank you. That makes me feel more supported."
    },
    {
      "from": "teacher",
      "to": "student",
      "timestamp": "2025-11-07T10:07:30Z",
      "message": "You are doing well. Small consistent steps will help."
    },
    {
      "from": "student",
      "to": "teacher",
      "timestamp": "2025-11-07T10:08:55Z",
      "message": "I completed the practice exercises and I feel more confident."
    },
    {
      "from": "teacher",
      "to": "student",
      "timestamp": "2025-11-07T10:10:22Z",
      "message": "That's excellent! Your effort is showing great results."
    },
    {
      "from": "student",
      "to": "teacher",
      "timestamp": "2025-11-07T10:12:40Z",
      "message": "I still struggle sometimes when problems get harder though."
    },
    {
      "from": "teacher",
      "to": "student",
      "timestamp": "2025-11-07T10:14:00Z",
      "message": "Struggling is part of learning. You are progressing well. Keep going."
    }
  ]
}


                                """);

        CompletableFuture<String> resp = service.sentiment(chat_data);
        resp.thenAccept(response -> {System.out.println(response);});

        
        // CompletableFuture<String> reg = service.regularise(points);
        // reg.thenAccept(System.out::println);
        // resp.thenAccept(System.out::println);
        System.out.println("AI Process - Running in another thread");


    }
}
