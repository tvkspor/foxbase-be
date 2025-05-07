package com.be.java.foxbase.dto.request;

import com.be.java.foxbase.utils.InteractionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InteractionRequest {
    String interactUsername;
    String creatorUsername;
    Long ratedBookId;

    @Enumerated(EnumType.STRING)
    InteractionType action;
}
