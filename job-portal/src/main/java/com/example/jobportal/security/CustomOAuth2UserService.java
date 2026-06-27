package com.example.jobportal.security;

import com.example.jobportal.entity.User;
import com.example.jobportal.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        User user = userRepository.findByEmail(email);
        
        if (user == null) {
            // New user from Google
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setUsername(email != null ? email : java.util.UUID.randomUUID().toString()); 
            user.setPassword(java.util.UUID.randomUUID().toString()); // Generate a random password for OAuth2 users
            user.setRole("ROLE_STUDENT"); // Default role
            user.setEnabled(true);
            userRepository.save(user);
        } else {
            // Update existing user if needed
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(name);
                userRepository.save(user);
            }
        }

        return oauth2User;
    }
}
