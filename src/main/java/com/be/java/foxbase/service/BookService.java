package com.be.java.foxbase.service;

import com.be.java.foxbase.dto.request.BookCreationRequest;
import com.be.java.foxbase.dto.response.BookResponse;
import com.be.java.foxbase.dto.response.PaginatedResponse;
import com.be.java.foxbase.exception.AppException;
import com.be.java.foxbase.exception.ErrorCode;
import com.be.java.foxbase.mapper.BookMapper;
import com.be.java.foxbase.repository.BookRepository;
import com.be.java.foxbase.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookMapper bookMapper;

    public BookResponse publish(BookCreationRequest request){
        var owner = userRepository.findByUsername(request.getPublisher()).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        var book = bookMapper.toBook(request, owner);
        bookRepository.save(book);
        return bookMapper.toBookResponse(book);
    }

    public PaginatedResponse<BookResponse> getBooksByGenre(String genre, Pageable pageable) {
        var books = bookRepository.findByGenre(genre, pageable);
        var bookResponse = books.map(bookMapper::toBookResponse);
        return PaginatedResponse.<BookResponse>builder()
                        .content(bookResponse.toList())
                        .totalPages(books.getTotalPages())
                        .totalElements(books.getTotalElements())
                        .size(books.getSize())
                        .page(books.getNumber())
                        .build();
    }

    public PaginatedResponse<BookResponse> getBooksByAuthor(String author, Pageable pageable) {
        var books = bookRepository.findByAuthor(author, pageable);
        var bookResponse = books.map(bookMapper::toBookResponse);
        return PaginatedResponse.<BookResponse>builder()
                .content(bookResponse.toList())
                .totalPages(books.getTotalPages())
                .totalElements(books.getTotalElements())
                .size(books.getSize())
                .page(books.getNumber())
                .build();
    }

    public PaginatedResponse<BookResponse> getBooksByTitle(String title, Pageable pageable) {
        var books = bookRepository.findByTitle(title, pageable);
        var bookResponse = books.map(bookMapper::toBookResponse);
        return PaginatedResponse.<BookResponse>builder()
                .content(bookResponse.toList())
                .totalPages(books.getTotalPages())
                .totalElements(books.getTotalElements())
                .size(books.getSize())
                .page(books.getNumber())
                .build();
    }
}
