package com.ecommerce.users.model;

import lombok.Getter;

@Getter
public class JwtResponse {
    private static final long serialVersionUID = -8091879091924046844L;

    private final String accessToken;
    private final String refreshToken;

    public JwtResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}