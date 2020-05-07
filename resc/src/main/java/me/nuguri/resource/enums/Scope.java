package me.nuguri.resource.enums;

public enum Scope {

    READ, WRITE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
