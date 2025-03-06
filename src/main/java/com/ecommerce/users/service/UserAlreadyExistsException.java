package com.ecommerce.users.service;

public class UserAlreadyExistsException extends Throwable {
    private final String emailJáCadastrado;

    public UserAlreadyExistsException(String emailJáCadastrado) {
        this.emailJáCadastrado = emailJáCadastrado;
    }
}
