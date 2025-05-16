package com.be.java.foxbase.service;

import com.be.java.foxbase.db.entity.Book;
import com.be.java.foxbase.db.entity.Rating;
import com.be.java.foxbase.db.entity.User;
import com.be.java.foxbase.db.key.UserBookRatingId;
import com.be.java.foxbase.dto.request.RatingRequest;
import com.be.java.foxbase.dto.response.PaginatedResponse;
import com.be.java.foxbase.dto.response.RatingResponse;
import com.be.java.foxbase.exception.AppException;
import com.be.java.foxbase.exception.ErrorCode;
import com.be.java.foxbase.mapper.RatingMapper;
import com.be.java.foxbase.repository.BookRepository;
import com.be.java.foxbase.repository.RatingRepository;
import com.be.java.foxbase.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RatingService {
    @Autowired
    RatingRepository ratingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    RatingMapper ratingMapper;

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public PaginatedResponse<RatingResponse> getBookRatings(Long bookId, Pageable pageable) {
        var ratings = ratingRepository.findByBook_BookId(bookId, pageable);
        var responses = ratings.stream().map(item -> {
            var user = item.getUser();
            return ratingMapper.toRatingResponse(item, user);
        }).toList();
        return PaginatedResponse.<RatingResponse>builder()
                .content(responses)
                .totalElements(ratings.getTotalElements())
                .totalPages(ratings.getTotalPages())
                .size(ratings.getSize())
                .page(ratings.getNumber())
                .build();
    }

    public RatingResponse createRating(RatingRequest ratingRequest){
        User creator = userRepository.findByUsername(getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        Book ratedBook = bookRepository.findByBookId(ratingRequest.getRatedBookId()).orElseThrow(
                () -> new AppException(ErrorCode.BOOK_NOT_FOUND)
        );

        Rating rating = ratingMapper.toRating(ratingRequest, creator, ratedBook);
        ratingRepository.save(ratingMapper.toRating(ratingRequest, creator, ratedBook));
        return ratingMapper.toRatingResponse(rating);
    }

    public RatingResponse getMyRating(Long bookId){
        Rating rating = ratingRepository.findByUserBookRatingId(new UserBookRatingId(getCurrentUsername(), bookId)).orElseThrow(
                () -> new AppException(ErrorCode.RATING_NOT_FOUND)
        );
        return ratingMapper.toRatingResponse(rating);
    }

    public Long countingBookRating(Long bookId){
        return ratingRepository.countByBook_BookId(bookId);
    }
}
