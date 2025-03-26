package com.message;
import com.fasterxml.jackson.annotation.JsonCreator;
public class ReadValueMessage {
    private final String roomName;
    private final String sensorType;

    public ReadValueMessage(String roomName, String sensorType) {
        this.roomName = roomName;
        this.sensorType = sensorType;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getSensorType() {
        return sensorType;
    }
}
