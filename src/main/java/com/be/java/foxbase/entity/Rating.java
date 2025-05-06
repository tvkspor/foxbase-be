package com.be.java.foxbase.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Rating {
    @EmbeddedId
    UserBookRatingId userBookRatingId;

    @ManyToOne
    @MapsId("creatorUsername")
    @JoinColumn(name = "creator_username")
    User user;

    @ManyToOne
    @MapsId("ratedBookId")
    @JoinColumn(name = "rated_book_id")
    Book book;

    @OneToMany(mappedBy = "rating")
    List<Interaction> interactions;

    Double rate;
    Integer likes;
    Integer dislikes;
    Integer loves;

    String comment;
    LocalDateTime createdAt;
}
