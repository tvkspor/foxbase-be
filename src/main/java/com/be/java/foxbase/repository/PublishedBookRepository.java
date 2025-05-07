package com.be.java.foxbase.repository;

import com.be.java.foxbase.db.entity.PublishedBook;
import com.be.java.foxbase.db.key.UserBookId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublishedBookRepository extends JpaRepository<PublishedBook, UserBookId> {
    Page<PublishedBook> findByUser_Username(String username, Pageable pageable);
}
