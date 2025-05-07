package com.be.java.foxbase.controller;

import com.be.java.foxbase.dto.request.InteractionRequest;
import com.be.java.foxbase.dto.response.ApiResponse;
import com.be.java.foxbase.dto.response.InteractionResponse;
import com.be.java.foxbase.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/interactions")
public class InteractionController {
    @Autowired
    InteractionService interactionService;

    @PostMapping("interact")
    ApiResponse<InteractionResponse> interact(@RequestBody InteractionRequest interactionRequest) {
        return ApiResponse.<InteractionResponse>builder()
                .data(interactionService.interact(interactionRequest))
                .build();
    }
}
