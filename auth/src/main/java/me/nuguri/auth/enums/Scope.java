package me.nuguri.auth.enums;

public enum Scope {

    READ, WRITE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
