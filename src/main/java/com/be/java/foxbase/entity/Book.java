package com.be.java.foxbase.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long bookId;

    String title;
    String author;
    String description;
    String contentUrl;
    String imageUrl;
    String genre;
    Double price;
    Double averageRating;

    @ManyToOne
    @JoinColumn(name = "owner_username")
    User owner;
}
