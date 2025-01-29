package com.vectoredu.backend.service;

import com.vectoredu.backend.model.User;
import com.vectoredu.backend.repository.UserRepository;
import com.vectoredu.backend.service.authservice.EmailService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }
}
