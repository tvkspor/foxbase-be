package com.be.java.foxbase.controller;

import com.be.java.foxbase.dto.request.InteractionRequest;
import com.be.java.foxbase.dto.response.ApiResponse;
import com.be.java.foxbase.dto.response.InteractionResponse;
import com.be.java.foxbase.service.InteractionService;
import com.be.java.foxbase.utils.InteractionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @GetMapping("/count")
    ApiResponse<Map<InteractionType, Integer>> countInteractions(@RequestBody InteractionRequest interactionRequest) {
        return ApiResponse.<Map<InteractionType, Integer>>builder()
                .data(interactionService.countInteractions(interactionRequest))
                .build();
    }
}
