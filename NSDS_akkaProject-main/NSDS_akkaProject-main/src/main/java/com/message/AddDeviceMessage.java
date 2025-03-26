package com.message;

import com.ApplianceOrSensor;
import com.fasterxml.jackson.annotation.JsonCreator;

public class AddDeviceMessage {
    private final String roomName;
    private final String deviceType;

    @JsonCreator
    public AddDeviceMessage(String roomName, String deviceType) {
        this.roomName = roomName;
        this.deviceType = deviceType;

    }

    public String getRoomName() {
        return roomName;
    }

    public String getDeviceType() {
        return deviceType;
    }

}
