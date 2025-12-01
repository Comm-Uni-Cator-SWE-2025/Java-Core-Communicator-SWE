/*
 * -----------------------------------------------------------------------------
 *  File: RegulariserParserTest.java
 *  Owner: Abhirami R Iyer
 *  Roll Number : 112201001
 *  Module : com.swe.aiinsights
 * -----------------------------------------------------------------------------
 */

package com.swe.aiinsights;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swe.aiinsights.parser.RegulariserParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;


/**
 * Test class for RegulariserParser.
 */
class RegulariserParserTest {

    /**
     * regulariser to test.
     */
    private RegulariserParser parser;

    /**
     * object mapper to build requests.
     */
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        parser = new RegulariserParser();
        objectMapper = new ObjectMapper();
    }


    @Test
    void testParseInputValidResponse() throws JsonProcessingException {
        final String inputJson = """
                  {
                  "ShapeId": "c585b84a",
                  "Type": "FREEHAND",
                  "Points": [
                    {
                      "X": 10,
                      "Y": 20
                    },
                    {
                      "X": 30,
                      "Y": 40
                    },
                    {
                      "X": 11,
                      "Y": 19
                    }
                  ],
                  "Color": "#FF000000",
                  "Thickness": 2,
                  "CreatedBy": "user_default",
                  "LastModifiedBy": "user_default",
                  "IsDeleted": false
                }
                """;

        final String aiResponse = """
               {
                  "ShapeId": "abcd",
                  "Type": "ELLIPSE",
                  "Points": [
                    {
                      "X": 10,
                      "Y": 20
                    },
                    {
                      "X": 30,
                      "Y": 40
                    },
                    {
                      "X": 11,
                      "Y": 19
                    },
                    {
                      "X": 10,
                      "Y": 20
                    }
                  ],
                  "Color": "#FF000000",
                  "Thickness": 3,
                  "CreatedBy": "user1",
                  "LastModifiedBy": "user1",
                  "IsDeleted": false
                }
               """;


        final String result = parser.parseInput(inputJson, aiResponse);


        assertNotNull(result);
        final JsonNode resultNode = objectMapper.readTree(result);

        assertEquals("c585b84a", resultNode.get("ShapeId").asText());
        assertEquals("ELLIPSE", resultNode.get("Type").asText());
        assertEquals("#FF000000", resultNode.get("Color").asText());
        assertEquals(2, resultNode.get("Thickness").asInt());
        assertEquals("user_default", resultNode.get("CreatedBy").asText());
        assertEquals("user_default", resultNode.get("LastModifiedBy").asText());
        assertFalse(resultNode.get("IsDeleted").asBoolean());

        // Verify points (should only have first 2)
        final JsonNode points = resultNode.get("Points");
        assertEquals(2, points.size());
    }


    @Test
    void testParseInputValidResponse2() throws JsonProcessingException {
        final String inputJson = """
                  {
                  "ShapeId": "c585b84a",
                  "Type": "FREEHAND",
                  "Points": [
                    {
                      "X": 10,
                      "Y": 20
                    },
                    {
                      "X": 30,
                      "Y": 40
                    },
                    {
                      "X": 11,
                      "Y": 19
                    }
                  ],
                  "Color": "#FF000000",
                  "Thickness": 2,
                  "CreatedBy": "user_default",
                  "LastModifiedBy": "user_default",
                  "IsDeleted": false
                }
                """;

        final String aiResponse = """
               ```json{
                  "ShapeId": "abcd",
                  "type": "ELLIPSE",
                  "Points": [
                    {
                      "X": 10,
                      "Y": 20
                    },
                    {
                      "X": 30,
                      "Y": 40
                    },
                    {
                      "X": 11,
                      "Y": 19
                    },
                    {
                      "X": 10,
                      "Y": 20
                    }
                  ],
                  "Color": "#FF000000",
                  "Thickness": 3,
                  "CreatedBy": "user1",
                  "LastModifiedBy": "user1",
                  "IsDeleted": false
                }```
               """;


        final String result = parser.parseInput(inputJson, aiResponse);


        assertNotNull(result);
        final JsonNode resultNode = objectMapper.readTree(result);

        assertEquals("c585b84a", resultNode.get("ShapeId").asText());
        assertEquals("ELLIPSE", resultNode.get("Type").asText());
        assertEquals("#FF000000", resultNode.get("Color").asText());
        assertEquals(2, resultNode.get("Thickness").asInt());
        assertEquals("user_default", resultNode.get("CreatedBy").asText());
        assertEquals("user_default", resultNode.get("LastModifiedBy").asText());
        assertFalse(resultNode.get("IsDeleted").asBoolean());

        final JsonNode points = resultNode.get("Points");
        assertEquals(2, points.size());
    }


    @Test
    void testParseInputNoPoints() throws JsonProcessingException {
        final String inputJson = """
                  {
                  "ShapeId": "c585b84a",
                  "Type": "FREEHAND",
                  "Points": [
                    {
                      "X": 10,
                      "Y": 20
                    },
                    {
                      "X": 30,
                      "Y": 40
                    },
                    {
                      "X": 11,
                      "Y": 19
                    }
                  ],
                  "Color": "#FF000000",
                  "Thickness": 2,
                  "CreatedBy": "user_default",
                  "LastModifiedBy": "user_default",
                  "IsDeleted": false
                }
                """;

        final String aiResponse = """
               {
                  "ShapeId": "abcd",
                  "Type": "ELLIPSE",
                  "Points": [
                    {
                      "X": 10,
                      "Y": 20
                    }
                  ],
                  "Color": "#FF000000",
                  "Thickness": 3,
                  "CreatedBy": "user1",
                  "LastModifiedBy": "user1",
                  "IsDeleted": false
                }
               """;

        final String result = parser.parseInput(inputJson, aiResponse);

        assertEquals(result, inputJson);
    }

    @Test
    void testParseInputNoType() throws JsonProcessingException {
        final String inputJson = """
                  {
                  "ShapeId": "c585b84a",
                  "Type": "FREEHAND",
                  "Points": [
                    {
                      "X": 10,
                      "Y": 20
                    },
                    {
                      "X": 30,
                      "Y": 40
                    },
                    {
                      "X": 11,
                      "Y": 19
                    }
                  ],
                  "Color": "#FF000000",
                  "Thickness": 2,
                  "CreatedBy": "user_default",
                  "LastModifiedBy": "user_default",
                  "IsDeleted": false
                }
                """;

        final String aiResponse = """
               {
                  "ShapeId": "abcd",
                  "ShapeType": "ELLIPSE",
                  "Points": [
                    {
                      "X": 10,
                      "Y": 20
                    },
                    {
                      "X": 11,
                      "Y": 21
                    }
                  ],
                  "Color": "#FF000000",
                  "Thickness": 3,
                  "CreatedBy": "user1",
                  "LastModifiedBy": "user1",
                  "IsDeleted": false
                }
               """;

        final String result = parser.parseInput(inputJson, aiResponse);

        final JsonNode resultNode = objectMapper.readTree(result);

        assertNotNull(resultNode.get("Type"));
    }


    @Test
    void testParseInputInvalidAiResponse() throws JsonProcessingException {

        final String inputJson = """
                {
                  "ShapeId": "c585b84a-d56c-45b8-a0e1-827ae20a014a",
                  "Type": "FREEHAND",
                  "Points": [
                    {
                      "X": 10,
                      "Y": 20
                    },
                    {
                      "X": 30,
                      "Y": 40
                    },
                    {
                      "X": 11,
                      "Y": 19
                    }
                  ],
                  "Color": "#FF000000",
                  "Thickness": 2,
                  "CreatedBy": "user_default",
                  "LastModifiedBy": "user_default",
                  "IsDeleted": false
                }
                """;


        final String result = parser.parseInput(inputJson, "ABCD");

        assertEquals(inputJson, result);
    }



    @Test
    void testParseInputInvalidJsonAiResponse() throws JsonProcessingException {

        final String inputJson = """
                {
                  "ShapeId": "c585b84a-d56c-45b8-a0e1-827ae20a014a",
                  "Type": "FREEHAND",
                  "Points": [
                    {
                      "X": 10,
                      "Y": 20
                    },
                    {
                      "X": 30,
                      "Y": 40
                    },
                    {
                      "X": 11,
                      "Y": 19
                    }
                  ],
                  "Color": "#FF000000",
                  "Thickness": 2,
                  "CreatedBy": "user_default",
                  "LastModifiedBy": "user_default",
                  "IsDeleted": false
                }
                """;

        final String invalidAiResponse = "{invalid json}";


        final String result = parser.parseInput(inputJson, invalidAiResponse);

        final String nullString = parser.parseInput(inputJson, null);


        assertEquals(inputJson, result);
    }

}