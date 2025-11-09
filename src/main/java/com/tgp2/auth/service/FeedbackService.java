package com.tgp2.auth.service;

import com.tgp2.auth.dto.FeedbackRequest;
import com.tgp2.auth.dto.FeedbackResponse;

public interface FeedbackService {
    FeedbackResponse submitFeedback(FeedbackRequest req, Long userId);
}
