package com.message;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SensorReadMessage {
    private String room;
    private String sensorName;
    private float value;


    @JsonCreator
    public SensorReadMessage(float value,String sensorName){
        this.sensorName=sensorName;
        this.value=value;
    }


    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
