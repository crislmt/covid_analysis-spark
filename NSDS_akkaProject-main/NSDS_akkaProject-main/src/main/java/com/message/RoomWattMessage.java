package com.message;

import com.fasterxml.jackson.annotation.JsonCreator;

public class RoomWattMessage {
    private final Float watt;
    private final String roomName;

    @JsonCreator
    public RoomWattMessage(Float watt, String roomName){
        this.watt=watt;
        this.roomName=roomName;
    }


    public Float getWatt() {
        return watt;
    }
    public String getRoomName(){return roomName;}
}
