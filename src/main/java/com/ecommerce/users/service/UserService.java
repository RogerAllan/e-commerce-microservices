package com.ecommerce.users.service;

import com.ecommerce.users.model.User;

public interface UserService {
    User getByEmail(String email);
    User registerUser(User user) throws UserAlreadyExistsException;

    boolean checkPassword(String email, String rawPassword);
}