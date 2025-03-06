package com.ecommerce.users.model;

import lombok.Data;

@Data
public class JwtRefreshRequest {
    private String refreshToken;
}