package me.nuguri.auth.enums;

public enum GrantType {

    PASSWORD, AUTHORIZATION_CODE, IMPLICIT, CLIENT_CREDENTIALS, REFRESH_TOKEN;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
