package com.message;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SetAttributeMessage {
    private String roomName;

    private String valueName;
    private float attribute;

    @JsonCreator
    public SetAttributeMessage(String roomName,String valueName,float attribute){
        this.roomName=roomName;
        this.valueName=valueName;
        this.attribute=attribute;
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

    public float getAttribute() {
        return attribute;
    }

    public void setAttribute(float attribute) {
        this.attribute = attribute;
    }
}
