package com.journeyplus.iam.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.journeyplus.iam.entity.User;
import com.journeyplus.iam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public User getUserByUsername(String username) {
        log.info("Fetching user details for username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Fetch failed: User '{}' not found in database", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
    }
}
