package com.tgp2.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class FeedbackResponse {
    private Long id;
    private Long paperId;
    private Long userId;
    private String feedbackType;
    private String role;
    private Integer rating;
    private String feedbackText;
    private String createdAt; // Convert LocalDateTime to String in service for consistent API formatting
}
