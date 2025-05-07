package com.be.java.foxbase.repository;

import com.be.java.foxbase.db.entity.Interaction;
import com.be.java.foxbase.db.key.InteractionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, InteractionId> {
}
