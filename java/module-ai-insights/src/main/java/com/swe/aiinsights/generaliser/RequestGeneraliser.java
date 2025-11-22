package com.swe.aiinsights.generaliser;

import com.swe.aiinsights.request.AiRequestable;
import com.swe.aiinsights.response.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RequestGeneraliser {

    private String prompt;

    private String textData;

    private String imgData;

    private String reqType;

    private AiResponse aiResponse;

    private ArrayList<String> registeredKeys = new ArrayList<>(
            List.of("DESC", "REG", "INS", "SUM", "ACTION", "QNA"));


    public RequestGeneraliser (AiRequestable request){

        System.out.println("DEBUG >>> ReqType: " + request.getReqType());
        System.out.println("DEBUG >>> Registered keys: " + registeredKeys);

        setPrompt(request.getContext());

        this.reqType = request.getReqType();
        if (Objects.equals(reqType, "DESC")){
            setImgData(request.getInput().toString());
        }
        else {
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

    public String getReqType(){
        return reqType;
    }
    public void setImgData(String imgData) {
        this.imgData = imgData;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public void setTextData(String textData) {
        this.textData = textData;
    }

    public AiResponse getAiResponse() {
        return aiResponse;
    }
}
