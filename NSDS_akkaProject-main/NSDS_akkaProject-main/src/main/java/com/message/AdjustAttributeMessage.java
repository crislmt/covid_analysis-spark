package com.message;

import com.fasterxml.jackson.annotation.JsonCreator;

public class AdjustAttributeMessage {
    private float attribute;

    @JsonCreator
    public AdjustAttributeMessage(float attribute){
        this.attribute=attribute;
    }

    public float getAttribute() {
        return attribute;
    }

}
