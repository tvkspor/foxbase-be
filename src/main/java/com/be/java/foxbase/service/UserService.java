package com.be.java.foxbase.service;

import com.be.java.foxbase.db.entity.SecurityOTP;
import com.be.java.foxbase.dto.request.ResetPasswordRequest;
import com.be.java.foxbase.dto.request.SendOTPRequest;
import com.be.java.foxbase.dto.request.UserCreationRequest;
import com.be.java.foxbase.dto.request.VerifyOTPRequest;
import com.be.java.foxbase.dto.response.ResetPasswordResponse;
import com.be.java.foxbase.dto.response.SendOTPResponse;
import com.be.java.foxbase.dto.response.UserResponse;
import com.be.java.foxbase.db.entity.User;
import com.be.java.foxbase.dto.response.VerifyOTPResponse;
import com.be.java.foxbase.exception.AppException;
import com.be.java.foxbase.exception.ErrorCode;
import com.be.java.foxbase.mapper.UserMapper;
import com.be.java.foxbase.repository.SecurityOTPRepository;
import com.be.java.foxbase.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SecurityOTPRepository securityOTPRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    protected String fromEmail;

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e){
            throw new AppException(ErrorCode.USER_EXIST);
        }

        return userMapper.toUserResponse(user);
    }

    public UserResponse getMyInfo(){
        var context = SecurityContextHolder.getContext();
        var username = context.getAuthentication().getName();

        User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        return userMapper.toUserResponse(user);
    }

    private boolean sendOTP(int otp, String toEmail, String name){
        String messageBody = "Hello " + name + ",\n\n"
                + "Your One-Time Password (OTP) is: " + otp + "\n\n"
                + "This code is valid for the next 5 minutes. Please do not share this code with anyone.\n\n"
                + "If you did not request this, please ignore this email or contact support.\n\n"
                + "Thank you,\n"
                + "Foxbase Support Team";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Reset Password");
        message.setText(messageBody);

        try {
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public SendOTPResponse sendSecurityOTP(SendOTPRequest request) {
        User user = userRepository.findByUsernameAndEmail(getCurrentUsername(), request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.UNMATCHED_EMAIL));

        Random generator = new Random();
        int otp = generator.nextInt(999999);

        var success = sendOTP(otp, request.getEmail(), user.getFName() + " " + user.getLName());

        if (success){
            SecurityOTP securityOTP = securityOTPRepository.findByUser_username(getCurrentUsername()).orElse(null);

            if (securityOTP != null){
                securityOTP.setOtp(otp);
                securityOTP.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
            } else {
                securityOTP = SecurityOTP.builder()
                        .otp(otp)
                        .otpExpiryTime(LocalDateTime.now().plusMinutes(5))
                        .resetToken("")
                        .build();
            }
            securityOTPRepository.save(securityOTP);
            return new SendOTPResponse(true);
        } else {
            return new SendOTPResponse(false);
        }
    }

    public VerifyOTPResponse verifySecurityOTP(VerifyOTPRequest request) {
        SecurityOTP securityOTP = securityOTPRepository.findByUser_username(getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.NO_SECURITY_OTP));

        if (request.getOtp() == securityOTP.getOtp()
                && securityOTP.getOtpExpiryTime().isAfter(LocalDateTime.now())
        ){
            var token = authenticationService.generateToken(getCurrentUsername());
            securityOTP.setResetToken(token);
            securityOTP.setTokenExpiryTime(LocalDateTime.now().plusMinutes(15));
            securityOTPRepository.save(securityOTP);
            return VerifyOTPResponse.builder()
                    .resetToken(token)
                    .verified(true)
                    .build();
        } else {
            return VerifyOTPResponse.builder()
                    .resetToken(null)
                    .verified(false)
                    .build();
        }
    }

    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        SecurityOTP securityOTP = securityOTPRepository.findByUser_username(getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.NO_SECURITY_OTP));

        if (Objects.equals(securityOTP.getResetToken(), request.getResetToken())
                && securityOTP.getTokenExpiryTime().isAfter(LocalDateTime.now()))
        {
            User user = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                    () -> new AppException(ErrorCode.USER_NOT_EXIST)
            );
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
            return ResetPasswordResponse.builder()
                    .success(true)
                    .build();
        } else {
            return ResetPasswordResponse.builder()
                    .success(false)
                    .build();
        }
    }
}
