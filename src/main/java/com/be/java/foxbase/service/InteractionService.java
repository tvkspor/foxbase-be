package com.be.java.foxbase.service;

import com.be.java.foxbase.db.entity.Interaction;
import com.be.java.foxbase.db.entity.Rating;
import com.be.java.foxbase.db.entity.User;
import com.be.java.foxbase.db.key.InteractionId;
import com.be.java.foxbase.db.key.UserBookRatingId;
import com.be.java.foxbase.dto.request.InteractionRequest;
import com.be.java.foxbase.dto.response.InteractionResponse;
import com.be.java.foxbase.exception.AppException;
import com.be.java.foxbase.exception.ErrorCode;
import com.be.java.foxbase.mapper.InteractionMapper;
import com.be.java.foxbase.repository.InteractionRepository;
import com.be.java.foxbase.repository.RatingRepository;
import com.be.java.foxbase.repository.UserRepository;
import com.be.java.foxbase.utils.InteractionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class InteractionService {
    @Autowired
    InteractionRepository interactionRepository;

    @Autowired
    InteractionMapper interactionMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RatingRepository ratingRepository;

    public InteractionResponse interact(InteractionRequest request) {
        User interactUser = userRepository.findByUsername(request.getInteractUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        UserBookRatingId ratingId = new UserBookRatingId(request.getCreatorUsername(), request.getRatedBookId());
        Rating rating = ratingRepository.findByUserBookRatingId(ratingId)
                .orElseThrow(() -> new AppException(ErrorCode.RATING_NOT_FOUND));

        InteractionId interactionId = new InteractionId(
                request.getInteractUsername(), request.getCreatorUsername(), request.getRatedBookId()
        );

        Interaction interaction = interactionRepository.findById(interactionId).orElse(null);

        if (interaction == null) {
            interaction = interactionMapper.toInteraction(request, interactUser, rating);
        } else {
            interaction.setAction(request.getAction());
        }

        ratingRepository.save(rating);
        interactionRepository.save(interaction);
        return interactionMapper.toInteractionResponse(interaction);
    }

    public Map<InteractionType, Integer> countInteractions(InteractionRequest request) {
        var like = interactionRepository.countInteraction(
                InteractionType.LIKE,
                request.getCreatorUsername(),
                request.getInteractUsername(),
                request.getRatedBookId()
        );
        var dislike = interactionRepository.countInteraction(
                InteractionType.DISLIKE,
                request.getCreatorUsername(),
                request.getInteractUsername(),
                request.getRatedBookId()
        );
        var love = interactionRepository.countInteraction(
                InteractionType.LOVE,
                request.getCreatorUsername(),
                request.getInteractUsername(),
                request.getRatedBookId()
        );

        Rating rating = ratingRepository.findByUserBookRatingId(new UserBookRatingId(request.getCreatorUsername(), request.getRatedBookId())).orElseThrow(
                () -> new AppException(ErrorCode.RATING_NOT_FOUND)
        );

        rating.setLikes(like);
        rating.setDislikes(dislike);
        rating.setLoves(love);

        ratingRepository.save(rating);

        return Map.of(InteractionType.LIKE, like, InteractionType.DISLIKE, dislike, InteractionType.LOVE, love);
    }
}
