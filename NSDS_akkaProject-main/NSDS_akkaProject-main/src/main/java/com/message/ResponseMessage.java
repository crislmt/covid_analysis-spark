package com.message;

import com.fasterxml.jackson.annotation.JsonCreator;

//Message used when smartHome give a response to the panel actor
public class ResponseMessage {
    private String response;

    @JsonCreator
    public ResponseMessage(String response){
        this.response=response;
    }
    public String getResponse() {
        return response;
    }
}
