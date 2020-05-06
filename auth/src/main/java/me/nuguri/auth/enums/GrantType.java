package me.nuguri.auth.enums;

/**
 * 권한 부여 방식
 */
public enum GrantType {

    PASSWORD, AUTHORIZATION_CODE, IMPLICIT, CLIENT_CREDENTIALS, REFRESH_TOKEN;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
