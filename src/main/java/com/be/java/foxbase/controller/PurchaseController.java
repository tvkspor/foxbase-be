package com.be.java.foxbase.controller;

import com.be.java.foxbase.dto.request.PurchaseBookRequest;
import com.be.java.foxbase.dto.request.PurchaseWalletRequest;
import com.be.java.foxbase.dto.response.ApiResponse;
import com.be.java.foxbase.dto.response.PurchaseBookResponse;
import com.be.java.foxbase.dto.response.PurchaseWalletResponse;
import com.be.java.foxbase.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    @PostMapping("/book")
    public ApiResponse<PurchaseBookResponse> purchaseBook(@RequestBody PurchaseBookRequest request) {
        return ApiResponse.<PurchaseBookResponse>builder()
                .data(purchaseService.purchaseBook(request))
                .build();
    }

    @PostMapping("/wallet")
    public ApiResponse<PurchaseWalletResponse> purchaseWallet(@RequestBody PurchaseWalletRequest request) {
        return ApiResponse.<PurchaseWalletResponse>builder()
                .data(purchaseService.purchaseWallet(request))
                .build();
    }
}
