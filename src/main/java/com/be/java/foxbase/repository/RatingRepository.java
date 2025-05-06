package com.be.java.foxbase.repository;

import com.be.java.foxbase.entity.Book;
import com.be.java.foxbase.entity.Rating;
import com.be.java.foxbase.entity.UserBookRatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UserBookRatingId> {
    List<Rating> findByBook(Book book);
    Rating create(Rating rating);
}
