package me.nuguri.auth.common;

public enum Scope {

    READ, WRITE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
