package com.be.java.foxbase.controller;

import com.be.java.foxbase.dto.request.BookCreationRequest;
import com.be.java.foxbase.dto.response.ApiResponse;
import com.be.java.foxbase.dto.response.BookResponse;
import com.be.java.foxbase.dto.response.PaginatedResponse;
import com.be.java.foxbase.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {
    @Autowired
    private BookService bookService;

    @PostMapping("/publish")
    ApiResponse<BookResponse> publish(@RequestBody BookCreationRequest request){
        return ApiResponse.<BookResponse>builder()
                .data(bookService.publish(request))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<BookResponse> get(@PathVariable Long id){
        return ApiResponse.<BookResponse>builder()
                .data(bookService.getBookById(id))
                .build();
    }

    @GetMapping("/genre")
    ApiResponse<PaginatedResponse<BookResponse>> getByGenre(
            @RequestParam String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<PaginatedResponse<BookResponse>>builder()
                .data(bookService.getBooksByGenre(genre, pageable))
                .build();
    }

    @GetMapping("/title")
    ApiResponse<PaginatedResponse<BookResponse>> getByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<PaginatedResponse<BookResponse>>builder()
                .data(bookService.getBooksByTitle(title, pageable))
                .build();
    }

    @GetMapping("/author")
    ApiResponse<PaginatedResponse<BookResponse>> getByAuthor(
            @RequestParam String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<PaginatedResponse<BookResponse>>builder()
                .data(bookService.getBooksByAuthor(author, pageable))
                .build();
    }

    @GetMapping("/collection")
    ApiResponse<PaginatedResponse<BookResponse>> getMyCollection(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<PaginatedResponse<BookResponse>>builder()
                .data(bookService.getMyBooks(pageable))
                .build();
    }

    @GetMapping("/favorites")
    ApiResponse<PaginatedResponse<BookResponse>> getMyFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<PaginatedResponse<BookResponse>>builder()
                .data(bookService.getFavoriteBooks(pageable))
                .build();
    }

    @GetMapping("/purchased")
    ApiResponse<List<Long>> getPurchasedBookIds(){
        return ApiResponse.<List<Long>>builder()
                .data(bookService.getPurchasedBookIds())
                .build();
    }
 }
