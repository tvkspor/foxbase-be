package com.be.java.foxbase.service;

import com.be.java.foxbase.db.entity.Book;
import com.be.java.foxbase.db.entity.FavoriteBook;
import com.be.java.foxbase.db.entity.PublishedBook;
import com.be.java.foxbase.db.entity.User;
import com.be.java.foxbase.db.key.UserBookId;
import com.be.java.foxbase.dto.request.BookCreationRequest;
import com.be.java.foxbase.dto.response.BookResponse;
import com.be.java.foxbase.dto.response.InFavoriteResponse;
import com.be.java.foxbase.dto.response.PaginatedResponse;
import com.be.java.foxbase.dto.response.ToggleFavoriteResponse;
import com.be.java.foxbase.exception.AppException;
import com.be.java.foxbase.exception.ErrorCode;
import com.be.java.foxbase.mapper.BookMapper;
import com.be.java.foxbase.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    @Autowired
    BookRepository bookRepository;

    @Autowired
    PublishedBookRepository publishedBookRepository;

    @Autowired
    FavoriteBookRepository favoriteBookRepository;

    @Autowired
    PurchasedBookRepository purchasedBookRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookMapper bookMapper;

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public BookResponse publish(BookCreationRequest request){
        var book = bookMapper.toBook(request);

        User publisher = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        var publishedBook = new PublishedBook(new UserBookId(getCurrentUsername(), book.getBookId()), publisher, book);


        bookRepository.save(book);
        publishedBookRepository.save(publishedBook);

        return bookMapper.toBookResponse(book);
    }

    public BookResponse getBookById(Long id){
        var book = bookRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));
        return bookMapper.toBookResponse(book);
    }

    public PaginatedResponse<BookResponse> getBooksByGenre(String genre, Pageable pageable) {
        var books = bookRepository.findByGenre(genre, pageable);
        var bookResponses = books.map(bookMapper::toBookResponse);
        return PaginatedResponse.<BookResponse>builder()
                        .content(bookResponses.toList())
                        .totalPages(books.getTotalPages())
                        .totalElements(books.getTotalElements())
                        .size(books.getSize())
                        .page(books.getNumber())
                        .build();
    }

    public PaginatedResponse<BookResponse> getBooksByAuthor(String author, Pageable pageable) {
        var books = bookRepository.findByAuthor(author, pageable);
        var bookResponses = books.map(bookMapper::toBookResponse);
        return PaginatedResponse.<BookResponse>builder()
                .content(bookResponses.toList())
                .totalPages(books.getTotalPages())
                .totalElements(books.getTotalElements())
                .size(books.getSize())
                .page(books.getNumber())
                .build();
    }

    public PaginatedResponse<BookResponse> getBooksByTitle(String title, Pageable pageable) {
        var books = bookRepository.findByTitle(title, pageable);
        var bookResponses = books.map(bookMapper::toBookResponse);
        return PaginatedResponse.<BookResponse>builder()
                .content(bookResponses.toList())
                .totalPages(books.getTotalPages())
                .totalElements(books.getTotalElements())
                .size(books.getSize())
                .page(books.getNumber())
                .build();
    }

    public PaginatedResponse<BookResponse> getMyBooks(Pageable pageable) {
        var books = publishedBookRepository.findByUser_Username(getCurrentUsername(), pageable);
        var bookResponses = books.map(item -> bookMapper.toBookResponse(item.getBook()));
        return PaginatedResponse.<BookResponse>builder()
                .content(bookResponses.toList())
                .totalPages(books.getTotalPages())
                .totalElements(books.getTotalElements())
                .size(books.getSize())
                .page(books.getNumber())
                .build();
    }

    public PaginatedResponse<BookResponse> getFavoriteBooks(Pageable pageable) {
        var books = favoriteBookRepository.findByUser_Username(getCurrentUsername(), pageable);
        var bookResponses = books.map(item -> bookMapper.toBookResponse(item.getBook()));
        return PaginatedResponse.<BookResponse>builder()
                .content(bookResponses.toList())
                .totalPages(books.getTotalPages())
                .totalElements(books.getTotalElements())
                .size(books.getSize())
                .page(books.getNumber())
                .build();
    }

    public InFavoriteResponse checkInFavorite(Long bookId) {
        FavoriteBook favoriteBook = favoriteBookRepository.findById(new UserBookId(getCurrentUsername(), bookId)).orElse(null);
        return InFavoriteResponse.builder()
                .username(getCurrentUsername())
                .bookId(bookId)
                .isAdded(favoriteBook != null)
                .build();
    }

    public ToggleFavoriteResponse toggleAddToFavoriteBooks(Long bookId) {
        FavoriteBook favoriteBook = favoriteBookRepository.findById(new UserBookId(getCurrentUsername(), bookId)).orElse(null);

        if (favoriteBook != null) {
            favoriteBookRepository.delete(favoriteBook);
            return ToggleFavoriteResponse.builder()
                    .username(getCurrentUsername())
                    .bookId(bookId)
                    .isAdded(false)
                    .message("Item has been removed from your favorite collection")
                    .build();
        }

        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new AppException(ErrorCode.BOOK_NOT_FOUND)
        );

        User user = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        FavoriteBook favBook = new FavoriteBook(new UserBookId(getCurrentUsername(), bookId), user, book);
        favoriteBookRepository.save(favBook);

        return ToggleFavoriteResponse.builder()
                .username(getCurrentUsername())
                .bookId(bookId)
                .isAdded(true)
                .message("Item has been added to your favorite collection")
                .build();
    }

    public List<Long> getPurchasedBookIds() {
        var books = purchasedBookRepository.findByUser_Username(getCurrentUsername());
        var bookResponses = books.stream().map(item -> item.getId().getBookId());
        return bookResponses.toList();
    }
}
