package com.message;

import com.fasterxml.jackson.annotation.JsonCreator;

public class WattMessage {
    private float watt;

    @JsonCreator
    public WattMessage(float watt){
        this.watt=watt;
    }


    public float getWatt() {
        return watt;
    }

    public void setWatt(float watt) {
        this.watt = watt;
    }
}
