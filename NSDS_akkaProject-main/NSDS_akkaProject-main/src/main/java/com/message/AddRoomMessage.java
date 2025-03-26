package com.message;

import com.fasterxml.jackson.annotation.JsonCreator;

public class AddRoomMessage {
    private final String name;

    @JsonCreator
    public AddRoomMessage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
