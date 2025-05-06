package com.be.java.foxbase.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    String username;
    String password;
    String email;
    String fName;
    String lName;
    Double balance;

    @ManyToMany
    @JoinTable(
            name = "user_favorite_books",
            joinColumns = @JoinColumn(name = "username"),
            inverseJoinColumns = @JoinColumn(name = "book_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"username","book_id"})
    )
    List<Book> favoriteBooks;

    @ManyToMany
    @JoinTable(
            name = "user_bought_books",
            joinColumns = @JoinColumn(name = "username"),
            inverseJoinColumns = @JoinColumn(name = "book_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"username","book_id"})
    )
    List<Book> boughtBooks;

    @OneToMany(mappedBy = "owner")
    List<Book> myBooks;

    @OneToMany(mappedBy = "interact")
    List<Interaction> interactions;
}
