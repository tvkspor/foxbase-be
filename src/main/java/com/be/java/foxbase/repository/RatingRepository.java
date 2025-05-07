package com.be.java.foxbase.repository;

import com.be.java.foxbase.db.entity.Rating;
import com.be.java.foxbase.db.key.UserBookRatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UserBookRatingId> {
    List<Rating> findByBook_BookId(Long id);
    Optional<Rating> findByUserBookRatingId(UserBookRatingId id);
}
