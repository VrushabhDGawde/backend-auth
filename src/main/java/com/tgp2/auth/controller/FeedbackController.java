package com.tgp2.auth.controller;

import com.tgp2.auth.dto.FeedbackRequest;
import com.tgp2.auth.dto.FeedbackResponse;
import com.tgp2.auth.security.UserDetailsImpl;
import com.tgp2.auth.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/submit")
    public ResponseEntity<FeedbackResponse> submitFeedback(@RequestBody FeedbackRequest req,
                                                           @AuthenticationPrincipal UserDetailsImpl principal) {
        FeedbackResponse resp = feedbackService.submitFeedback(req, principal.getId());
        return ResponseEntity.status(201).body(resp);
    }
}
