package com.be.java.foxbase.service;

import com.be.java.foxbase.db.entity.Book;
import com.be.java.foxbase.db.entity.Rating;
import com.be.java.foxbase.db.entity.User;
import com.be.java.foxbase.dto.request.RatingRequest;
import com.be.java.foxbase.dto.response.RatingResponse;
import com.be.java.foxbase.exception.AppException;
import com.be.java.foxbase.exception.ErrorCode;
import com.be.java.foxbase.mapper.RatingMapper;
import com.be.java.foxbase.repository.BookRepository;
import com.be.java.foxbase.repository.RatingRepository;
import com.be.java.foxbase.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<RatingResponse> getBookRatings(Long bookId){
        var ratings = ratingRepository.findByBook_BookId(bookId);
        return ratings.stream().map(ratingMapper::toRatingResponse).toList();
    }

    public RatingResponse createRating(RatingRequest ratingRequest){
        User creator = userRepository.findByUsername(ratingRequest.getCreatorUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXIST)
        );

        Book ratedBook = bookRepository.findByBookId(ratingRequest.getRatedBookId()).orElseThrow(
                () -> new AppException(ErrorCode.BOOK_NOT_FOUND)
        );

        Rating rating = ratingMapper.toRating(ratingRequest, creator, ratedBook);
        ratingRepository.save(ratingMapper.toRating(ratingRequest, creator, ratedBook));
        return ratingMapper.toRatingResponse(rating);
    }
}
