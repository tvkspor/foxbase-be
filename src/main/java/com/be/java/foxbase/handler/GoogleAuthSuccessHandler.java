package com.be.java.foxbase.handler;

import com.be.java.foxbase.dto.response.ApiResponse;
import com.be.java.foxbase.dto.response.AuthenticationResponse;
import com.be.java.foxbase.service.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class GoogleAuthSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Get OAuth2User information
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        // Generate JWT for your system (use email or any identifier)
        String jwt = authenticationService.generateToken(email);

        // Create an AuthenticationResponse object
        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .token(jwt)
                .authenticated(true) // User has been authenticated
                .build();

        // Wrap AuthenticationResponse in ApiResponse
        ApiResponse<AuthenticationResponse> apiResponse = ApiResponse.<AuthenticationResponse>builder()
                .statusCode(HttpServletResponse.SC_OK)
                .message("Authentication successful with Google")
                .data(authResponse)
                .build();

        // Return ApiResponse in the response body
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);

        // Convert ApiResponse to JSON and send it
        ResponseEntity<ApiResponse<AuthenticationResponse>> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);
        response.getWriter().write(Objects.requireNonNull(responseEntity.getBody()).toString());
    }
}
