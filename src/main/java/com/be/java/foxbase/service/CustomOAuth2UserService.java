package com.be.java.foxbase.service;

import com.be.java.foxbase.repository.UserRepository;
import com.be.java.foxbase.db.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Default behavior: fetch user info from the provider
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // Extract user attributes from Google
        String email = oAuth2User.getAttribute("email");
        String fName = oAuth2User.getAttribute("given_name");
        String lName = oAuth2User.getAttribute("family_name");
        String picture = oAuth2User.getAttribute("picture");

        // Check if the user exists in your DB, otherwise register
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> registerNewUser(email, fName, lName, picture));

        // Map to Spring Security's OAuth2User
        Map<String, Object> attributes = oAuth2User.getAttributes();

        return new DefaultOAuth2User(
                null,
                attributes,
                "email" // name attribute key
        );
    }

    private User registerNewUser(String email, String fName, String lName, String picture) {
        User user = User.builder()
                .username(email)
                .email(email)
                .fName(fName)
                .lName(lName)
                .balance(0.0)
                .avatar(picture)
                .password("")
                .build();

        return userRepository.save(user);
    }
}

