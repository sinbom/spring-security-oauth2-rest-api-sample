package me.nuguri.auth.common;

public enum GrantType {

    PASSWORD, AUTHORIZATION_CODE, IMPLICIT, REFRESH_TOKEN;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
