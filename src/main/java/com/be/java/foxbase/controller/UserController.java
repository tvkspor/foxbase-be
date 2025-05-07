package com.be.java.foxbase.controller;

import com.be.java.foxbase.dto.request.UserCreationRequest;
import com.be.java.foxbase.dto.response.ApiResponse;
import com.be.java.foxbase.dto.response.UserResponse;
import com.be.java.foxbase.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/register")
    ApiResponse<UserResponse> register(@RequestBody UserCreationRequest userCreationRequest){
        return ApiResponse.<UserResponse>builder()
                .data(userService.createUser(userCreationRequest))
                .build();
    }

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo(){
        return ApiResponse.<UserResponse>builder()
                .data(userService.getMyInfo())
                .build();
    }
}
