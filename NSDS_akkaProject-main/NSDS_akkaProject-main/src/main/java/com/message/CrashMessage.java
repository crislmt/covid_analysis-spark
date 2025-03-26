package com.message;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CrashMessage {
    private String roomName;

    private String valueName;


    @JsonCreator
    public CrashMessage(String roomName,String valueName){
        this.roomName=roomName;
        this.valueName=valueName;
    }


    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

}
