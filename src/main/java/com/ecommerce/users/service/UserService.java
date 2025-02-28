package com.ecommerce.users.service;
import com.ecommerce.users.model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface UserService {
    User getEmail(String email);
    User registerUser(User user);


    @Transactional(readOnly = true)
    User getToken(String token);

    @Transactional(readOnly = true)
    boolean checkPassword(String email, String rawPassword);

    @Transactional(readOnly = true)
    User user(User user);
}