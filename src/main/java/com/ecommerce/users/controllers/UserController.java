package com.ecommerce.users.controllers;

import com.ecommerce.users.model.User;
import com.ecommerce.users.service.UserAlreadyExistsException;
import com.ecommerce.users.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/controller")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) throws UserAlreadyExistsException {
        User savedUser = userService.registerUser(user);
        return new ResponseEntity<> (savedUser, HttpStatus.CREATED);
    }
}
