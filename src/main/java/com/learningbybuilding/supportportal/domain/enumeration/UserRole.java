package com.learningbybuilding.supportportal.domain.enumeration;

import java.util.HashSet;
import java.util.Set;

import static com.learningbybuilding.supportportal.domain.enumeration.UserAuthority.*;

public enum UserRole {
    ROLE_USER(Set.of(USER_READ)),
    ROLE_HR(Set.of(USER_READ, USER_UPDATE)),
    ROLE_MANAGER(Set.of(USER_READ, USER_UPDATE)),
    ROLE_ADMIN(Set.of(USER_READ, USER_UPDATE, USER_CREATE)),
    ROLE_SUPER_ADMIN(Set.of(USER_READ, USER_UPDATE, USER_CREATE, USER_DELETE));

    private final Set<UserAuthority> authorities;

    UserRole(Set<UserAuthority> authorities) {
        this.authorities = authorities;
    }

    public String[] getAuthorities() {
        return authorities.stream()
                .map(auth -> auth.getAuthority())
                .toArray(String[]::new);
    }
}
