package com.tgp2.auth.service.impl;

import com.tgp2.auth.dto.FeedbackRequest;
import com.tgp2.auth.dto.FeedbackResponse;
import com.tgp2.auth.entity.Feedback;
import com.tgp2.auth.entity.Paper;
import com.tgp2.auth.entity.User;
import com.tgp2.auth.exception.DuplicateResponseException;
import com.tgp2.auth.exception.ResourceNotFoundException;
import com.tgp2.auth.repository.FeedbackRepository;
import com.tgp2.auth.repository.PaperRepository;
import com.tgp2.auth.repository.UserRepository;
import com.tgp2.auth.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final PaperRepository paperRepository;

    @Override
    public FeedbackResponse submitFeedback(FeedbackRequest req, Long userId) {
        // validate user and paper existence
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Paper paper = paperRepository.findById(req.getPaperId())
                .orElseThrow(() -> new ResourceNotFoundException("Paper not found: " + req.getPaperId()));

        // business validations
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (req.getFeedbackText() != null && req.getFeedbackText().length() > 2000) {
            throw new IllegalArgumentException("Feedback exceeds 2000 characters");
        }

        // prevent duplicate feedback by same user for same paper
//        if (feedbackRepository.existsByPaperIdAndUserId(paper.getId(), user.getId())) {
//            throw new DuplicateResponseException("Feedback already submitted for this paper by this user");
//        }
        Feedback existing = feedbackRepository.findByPaperIdAndUserId(paper.getId(), user.getId());
        if (existing != null) {
            existing.setFeedbackType(req.getFeedbackType());
            existing.setRole(req.getRole());
            existing.setRating(req.getRating());
            existing.setFeedbackText(req.getFeedbackText());
            return mapToResponse(feedbackRepository.save(existing));
        }

        Feedback saved = feedbackRepository.save(
                Feedback.builder()
                        .paper(paper)
                        .user(user)
                        .feedbackType(req.getFeedbackType())
                        .role(req.getRole())
                        .rating(req.getRating())
                        .feedbackText(req.getFeedbackText())
                        .build()
        );

        LocalDateTime created = saved.getCreatedAt(); // assumed @CreationTimestamp present
        return FeedbackResponse.builder()
                .id(saved.getId())
                .paperId(paper.getId())
                .userId(user.getId())
                .feedbackType(saved.getFeedbackType())
                .role(saved.getRole())
                .rating(saved.getRating())
                .feedbackText(saved.getFeedbackText())
                .createdAt(created != null ? created.toString() : null) // per your choice C
                .build();
    }

    private FeedbackResponse mapToResponse(Feedback f) {
        return FeedbackResponse.builder()
                .id(f.getId())
                .paperId(f.getPaper().getId())
                .userId(f.getUser().getId())
                .feedbackType(f.getFeedbackType())
                .role(f.getRole())
                .rating(f.getRating())
                .feedbackText(f.getFeedbackText())
                .createdAt(f.getCreatedAt() != null ? f.getCreatedAt().toString() : null)
                .build();
    }
}
