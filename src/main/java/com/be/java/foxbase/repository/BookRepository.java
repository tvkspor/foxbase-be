package com.be.java.foxbase.repository;

import com.be.java.foxbase.db.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByBookId(Long bookId);
    Page<Book> findByGenre(String genre, Pageable pageable);
    Page<Book> findByTitle(String title, Pageable pageable);
    Page<Book> findByAuthor(String author, Pageable pageable);

    @Override
    @NonNull
    Page<Book> findAll(@NonNull Pageable pageable);
}
