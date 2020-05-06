package me.nuguri.auth.enums;

/**
 * 접근 범위
 */
public enum Scope {

    READ, WRITE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
