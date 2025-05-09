package com.be.java.foxbase.service;

import com.be.java.foxbase.db.entity.Book;
import com.be.java.foxbase.db.entity.PurchasedBook;
import com.be.java.foxbase.db.entity.User;
import com.be.java.foxbase.db.key.UserBookId;
import com.be.java.foxbase.dto.request.PurchaseBookRequest;
import com.be.java.foxbase.dto.request.PurchaseWalletRequest;
import com.be.java.foxbase.dto.response.PurchaseBookResponse;
import com.be.java.foxbase.dto.response.PurchaseWalletResponse;
import com.be.java.foxbase.exception.AppException;
import com.be.java.foxbase.exception.ErrorCode;
import com.be.java.foxbase.repository.BookRepository;
import com.be.java.foxbase.repository.PurchasedBookRepository;
import com.be.java.foxbase.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PurchaseService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private PurchasedBookRepository purchasedBookRepository;

    private String getCurrentUsername(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public PurchaseWalletResponse purchaseWallet(PurchaseWalletRequest purchaseWalletRequest) {
        User user = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        user.setBalance(user.getBalance() + purchaseWalletRequest.getAmount());

        userRepository.save(user);
        return PurchaseWalletResponse.builder()
                .success(true)
                .newBalance(user.getBalance())
                .build();
    }

    public PurchaseBookResponse purchaseBook(PurchaseBookRequest purchaseBookRequest) {
        User user = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        Book book = bookRepository.findById(purchaseBookRequest.getBookId()).orElseThrow(
                () -> new AppException(ErrorCode.BOOK_NOT_FOUND)
        );

        user.setBalance(user.getBalance() - book.getPrice());

        PurchasedBook purchasedBook = PurchasedBook.builder()
                .book(book)
                .user(user)
                .id(new UserBookId(getCurrentUsername(), purchaseBookRequest.getBookId()))
                .build();

        userRepository.save(user);
        purchasedBookRepository.save(purchasedBook);
        return PurchaseBookResponse.builder()
                .bookTitle(book.getTitle())
                .bookPrice(book.getPrice())
                .buyer(getCurrentUsername())
                .purchaseAt(LocalDateTime.now())
                .newBalance(user.getBalance())
                .success(true)
                .build();
    }
}
