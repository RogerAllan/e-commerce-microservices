package com.ecommerce.users.service;

import com.ecommerce.users.model.User;
import com.ecommerce.users.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException ("Usuário não encontrado"));
    }

    @Override
    @Transactional
    public User registerUser(User user) throws UserAlreadyExistsException {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email já cadastrado");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkPassword(String email, String rawPassword) {
        User user = getByEmail(email);
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }


}