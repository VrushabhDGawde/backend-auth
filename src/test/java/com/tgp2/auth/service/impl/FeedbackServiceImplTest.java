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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest {

    @Mock private FeedbackRepository feedbackRepository;
    @Mock private UserRepository userRepository;
    @Mock private PaperRepository paperRepository;

    @InjectMocks
    private FeedbackServiceImpl feedbackService;

    private FeedbackRequest request;
    private User user;
    private Paper paper;

    @BeforeEach
    void setup() {
        request = FeedbackRequest.builder()
                .paperId(10L)
                .feedbackType("CONTENT_QUALITY")
                .role("REVIEWER")
                .rating(4)
                .feedbackText("Good material")
                .build();

        user = new User();
        user.setId(5L);

        paper = new Paper();
        paper.setId(10L);
    }

    @Test
    void submitFeedback_Saves_WhenValid() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(paperRepository.findById(10L)).thenReturn(Optional.of(paper));
        when(feedbackRepository.findByPaperIdAndUserId(10L, 5L)).thenReturn(null);
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(inv -> {
            Feedback f = inv.getArgument(0);
            f.setId(100L);
            f.setCreatedAt(LocalDateTime.now());
            return f;
        });

        FeedbackResponse resp = feedbackService.submitFeedback(request, 5L);

        assertNotNull(resp);
        assertEquals(100L, resp.getId());
        assertEquals(10L, resp.getPaperId());
        assertEquals(5L, resp.getUserId());
        assertEquals(4, resp.getRating());
        assertNotNull(resp.getCreatedAt());

        verify(userRepository).findById(5L);
        verify(paperRepository).findById(10L);
        verify(feedbackRepository).findByPaperIdAndUserId(10L, 5L);
        verify(feedbackRepository).save(any(Feedback.class));
    }

    @Test
    void submitFeedback_Throws_WhenUserMissing() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> feedbackService.submitFeedback(request, 5L));

        verify(paperRepository, never()).findById(any());
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void submitFeedback_Throws_WhenPaperMissing() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(paperRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> feedbackService.submitFeedback(request, 5L));

        verify(feedbackRepository, never()).save(any());
    }

    @Test
    void submitFeedback_UpdatesExistingFeedback_WhenDuplicate() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(paperRepository.findById(10L)).thenReturn(Optional.of(paper));

        Feedback existing = new Feedback();
        existing.setId(77L);
        existing.setPaper(paper);
        existing.setUser(user);
        existing.setRating(3); // old rating

        when(feedbackRepository.findByPaperIdAndUserId(10L, 5L)).thenReturn(existing);

        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(inv -> {
            Feedback f = inv.getArgument(0);
            f.setCreatedAt(LocalDateTime.now());
            return f;
        });

        FeedbackResponse resp = feedbackService.submitFeedback(request, 5L);

        assertNotNull(resp);
        assertEquals(77L, resp.getId()); // same id because updating existing row
        assertEquals(5L, resp.getUserId());
        assertEquals(10L, resp.getPaperId());
        assertEquals(4, resp.getRating()); // updated new rating

        verify(feedbackRepository).findByPaperIdAndUserId(10L, 5L);
        verify(feedbackRepository).save(any(Feedback.class));
    }

    @Test
    void submitFeedback_Throws_WhenRatingInvalid() {
        request.setRating(0);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(paperRepository.findById(10L)).thenReturn(Optional.of(paper));

        assertThrows(IllegalArgumentException.class,
                () -> feedbackService.submitFeedback(request, 5L));
    }

    @Test
    void submitFeedback_Throws_WhenTextTooLong() {
        request.setFeedbackText("a".repeat(2001));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(paperRepository.findById(10L)).thenReturn(Optional.of(paper));

        assertThrows(IllegalArgumentException.class,
                () -> feedbackService.submitFeedback(request, 5L));
    }
}
