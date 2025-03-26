package com.message;

import com.fasterxml.jackson.annotation.JsonCreator;

public class RoomSetupMessage {
    private String name;
    @JsonCreator
    public RoomSetupMessage(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
