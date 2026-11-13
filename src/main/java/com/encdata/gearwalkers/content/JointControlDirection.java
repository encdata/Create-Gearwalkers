package com.encdata.gearwalkers.content;

public enum JointControlDirection {
    FORWARD("forward"),
    BACKWARD("backward"),
    LEFT("left"),
    RIGHT("right");

    private final String id;

    JointControlDirection(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
