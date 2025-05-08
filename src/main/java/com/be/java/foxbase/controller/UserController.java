package com.be.java.foxbase.controller;

import com.be.java.foxbase.dto.request.ResetPasswordRequest;
import com.be.java.foxbase.dto.request.SendOTPRequest;
import com.be.java.foxbase.dto.request.UserCreationRequest;
import com.be.java.foxbase.dto.request.VerifyOTPRequest;
import com.be.java.foxbase.dto.response.*;
import com.be.java.foxbase.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/request-otp")
    ApiResponse<SendOTPResponse> requestOTP(@RequestBody SendOTPRequest otpRequest){
        return ApiResponse.<SendOTPResponse>builder()
                .data(userService.sendSecurityOTP(otpRequest))
                .build();
    }

    @PostMapping("/verify-otp")
    ApiResponse<VerifyOTPResponse> verifyOTP(@RequestBody VerifyOTPRequest otpRequest){
        return ApiResponse.<VerifyOTPResponse>builder()
                .data(userService.verifySecurityOTP(otpRequest))
                .build();
    }

    @PostMapping("/reset-password")
    ApiResponse<ResetPasswordResponse> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest){
        return ApiResponse.<ResetPasswordResponse>builder()
                .data(userService.resetPassword(resetPasswordRequest))
                .build();
    }
}
