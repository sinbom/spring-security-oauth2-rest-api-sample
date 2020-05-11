package me.nuguri.common.enums;

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
