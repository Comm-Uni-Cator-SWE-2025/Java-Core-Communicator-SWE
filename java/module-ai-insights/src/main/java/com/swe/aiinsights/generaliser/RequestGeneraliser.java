/**
 *
 * <p>
 * The RequestGeneraliser acts as the generalising layer b/w adapters.
 * It extracts request metadata,
 * normalises input formats (text / image), and attaches the
 * correct response container based on the request type.
 * </p>
 *
 * <p>
 * This class ensures that downstream adapters (Gemini, Ollama, etc.)
 * do not need to understand request categories such as DESC, REG,
 * SUM, INS, ACTION, or QNA. It also applies specialised output json
 * formatting rules (e.g., regularisation, insights generation).
 * </p>
 *
 * <p>
 * References:
  *     1. Strategy & Factory Method Patterns (GoF)
 * </p>
 *
 * @author Abhirami R Iyer
 * @editedby Nandhana Sunil
 */

package com.swe.aiinsights.generaliser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swe.aiinsights.parser.RegulariserParser;
import com.swe.aiinsights.request.AiRequestable;
import com.swe.aiinsights.response.AiResponse;
import com.swe.aiinsights.response.InterpreterResponse;
import com.swe.aiinsights.response.RegulariserResponse;
import com.swe.aiinsights.response.SummariserResponse;
import com.swe.aiinsights.response.InsightsResponse;
import com.swe.aiinsights.response.ActionItemsResponse;
import com.swe.aiinsights.response.QuestionAnswerResponse;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Generalises any kind of request.
 * Generalising this way
 * the adapter modules need not know
 * the kinds of requests.
 */
public class RequestGeneraliser {

    /**
     * Holds the prompt of the request.
     */
    private String prompt;
    /**
     * Holds the supporting text data if any.
     */
    private String textData;
    /**
     * Holds supporting image data if any.
     */
    private String imgData;
    /**
     * Holds the reqType of that generalised request.
     */
    private String reqType;
    /**
     * Stores the AiResponse.
     */
    private AiResponse aiResponse;
    /**
     * Allowed request kinds.
     */
    private ArrayList<String> registeredKeys = new ArrayList<>(
            List.of("DESC", "REG", "INS", "SUM", "ACTION", "QNA"));


    public RequestGeneraliser(final AiRequestable request) {

        System.out.println("DEBUG >>> ReqType: " + request.getReqType());
        System.out.println("DEBUG >>> Registered keys: " + registeredKeys);

        setPrompt(request.getContext());

        this.reqType = request.getReqType();
        if (Objects.equals(reqType, "DESC")) {
            setImgData(request.getInput().toString());
        } else {
            setTextData(request.getInput().toString());
        }
        switch (this.reqType) {
            case "DESC" :
                aiResponse = new InterpreterResponse();
                break;
            case "REG":
                aiResponse = new RegulariserResponse();
                break;
            case "SUM" :
                aiResponse = new SummariserResponse();
                break;
            case "INS" :
                aiResponse = new InsightsResponse();
                break;
            case "ACTION" :
                aiResponse = new ActionItemsResponse();
                break;
            case "QNA" :
                aiResponse = new QuestionAnswerResponse();
                break;
            default:
                aiResponse = null;
                break;
        }
    }

    public String getImgData() {
        return imgData;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getTextData() {
        return textData;
    }

    public String getReqType() {
        return reqType;
    }

    public void setImgData(final String givenImgData) {
        this.imgData = givenImgData;
    }

    public void setPrompt(final String givenPrompt) {
        this.prompt = givenPrompt;
    }

    public void setTextData(final String givenTextData) {
        this.textData = givenTextData;
    }

    public AiResponse getAiResponse() {
        return aiResponse;
    }

    /**
     * formats the output in the required format.
     * @param response response from AI
     * @return Final string ouput
     * @throws JsonProcessingException error during json parsing
     */
    public String formatOutput(final AiResponse response) throws JsonProcessingException {
        if (Objects.equals(reqType, "REG")) {
            final RegulariserParser parser = new RegulariserParser();

            return parser.parseInput(this.textData, response.getResponse());
        }
        return response.getResponse();
    }
}
