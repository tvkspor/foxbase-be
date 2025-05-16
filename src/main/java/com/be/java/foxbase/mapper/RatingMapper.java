package com.be.java.foxbase.mapper;

import com.be.java.foxbase.db.entity.Book;
import com.be.java.foxbase.db.entity.Rating;
import com.be.java.foxbase.db.entity.User;
import com.be.java.foxbase.db.key.UserBookRatingId;
import com.be.java.foxbase.dto.request.RatingRequest;
import com.be.java.foxbase.dto.response.RatingResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

@Component
public class RatingMapper {
    public RatingResponse toRatingResponse(Rating rating, User user){
        return RatingResponse.builder()
                .creatorUsername(rating.getUser().getUsername())
                .creatorFName(user.getFName())
                .creatorLName(user.getLName())
                .creatorAvatar(user.getAvatar())
                .ratedBookId(rating.getUserBookRatingId().getRatedBookId())
                .rate(rating.getRate())
                .loves(rating.getLoves())
                .likes(rating.getLikes())
                .dislikes(rating.getDislikes())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }

    public RatingResponse toRatingResponse(Rating rating){
        return RatingResponse.builder()
                .creatorUsername(rating.getUser().getUsername())
                .ratedBookId(rating.getUserBookRatingId().getRatedBookId())
                .rate(rating.getRate())
                .loves(rating.getLoves())
                .likes(rating.getLikes())
                .dislikes(rating.getDislikes())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }

    public Rating toRating(RatingRequest ratingRequest, User creator, Book ratedBook){
        return Rating.builder()
                .userBookRatingId(new UserBookRatingId(creator.getUsername(), ratingRequest.getRatedBookId()))
                .user(creator)
                .book(ratedBook)
                .rate(ratingRequest.getRate())
                .likes(0)
                .dislikes(0)
                .loves(0)
                .comment(ratingRequest.getComment())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
