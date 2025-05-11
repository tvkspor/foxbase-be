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
import org.springframework.data.domain.Page;
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

    @Autowired
    private RatingRepository ratingRepository;

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
        var response = bookMapper.toBookResponse(book);
        var avgRating = ratingRepository.findBookAverageRating(book.getBookId());
        response.setAverageRating(avgRating);
        book.setAverageRating(avgRating);
        bookRepository.save(book);
        return response;
    }

    private PaginatedResponse<BookResponse> buildPaginatedBookResponse(Page<Book> books) {
        var bookResponses = books.map(bookMapper::toBookResponse);
        return toPaginatedResponse(books, bookResponses);
    }

    private PaginatedResponse<BookResponse> buildPaginatedBookResponseWithRating(Page<Book> books) {
        var bookResponses = books.map(book -> {
            var response = bookMapper.toBookResponse(book);
            var avgRating = ratingRepository.findBookAverageRating(book.getBookId());
            response.setAverageRating(avgRating);
            book.setAverageRating(avgRating);
            bookRepository.save(book);
            return response;
        });
        return toPaginatedResponse(books, bookResponses);
    }

    private <T> PaginatedResponse<T> toPaginatedResponse(Page<?> sourcePage, Page<T> mappedPage) {
        return PaginatedResponse.<T>builder()
                .content(mappedPage.toList())
                .totalPages(sourcePage.getTotalPages())
                .totalElements(sourcePage.getTotalElements())
                .size(sourcePage.getSize())
                .page(sourcePage.getNumber())
                .build();
    }


    public PaginatedResponse<BookResponse> getBooksByGenre(String genre, Pageable pageable) {
        var books = bookRepository.findByGenreContaining(genre, pageable);
        return buildPaginatedBookResponse(books);
    }

    public PaginatedResponse<BookResponse> getBooksByAuthor(String author, Pageable pageable) {
        var books = bookRepository.findByAuthorContaining(author, pageable);
        return buildPaginatedBookResponse(books);
    }

    public PaginatedResponse<BookResponse> getBooksByTitle(String title, Pageable pageable) {
        var books = bookRepository.findByTitleContaining(title, pageable);
        return buildPaginatedBookResponse(books);
    }

    public PaginatedResponse<BookResponse> getMyBooks(Pageable pageable) {
        var books = publishedBookRepository.findByUser_Username(getCurrentUsername(), pageable)
                .map(PublishedBook::getBook);
        return buildPaginatedBookResponseWithRating(books);
    }

    public PaginatedResponse<BookResponse> getFavoriteBooks(Pageable pageable) {
        var books = favoriteBookRepository.findByUser_Username(getCurrentUsername(), pageable)
                .map(FavoriteBook::getBook);
        return buildPaginatedBookResponseWithRating(books);
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
