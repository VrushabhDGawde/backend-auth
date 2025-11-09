package com.tgp2.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgp2.auth.dto.FeedbackRequest;
import com.tgp2.auth.dto.FeedbackResponse;
import com.tgp2.auth.security.UserDetailsImpl;
import com.tgp2.auth.service.FeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FeedbackControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FeedbackService feedbackService;

    @InjectMocks
    private FeedbackController feedbackController;

    private ObjectMapper mapper;
    private FeedbackRequest request;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(feedbackController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        request = FeedbackRequest.builder()
                .paperId(10L)
                .feedbackType("CONTENT_QUALITY")
                .role("REVIEWER")
                .rating(5)
                .feedbackText("Great work")
                .build();
    }

    @Test
    void submitFeedback_Returns201AndBody() throws Exception {
        FeedbackResponse resp = FeedbackResponse.builder()
                .id(100L)
                .paperId(10L)
                .userId(5L)
                .feedbackType("CONTENT_QUALITY")
                .role("REVIEWER")
                .rating(5)
                .feedbackText("Great work")
                .createdAt("2025-11-09T21:00:00")
                .build();

        when(feedbackService.submitFeedback(any(), any())).thenReturn(resp);

        UserDetailsImpl principal = new UserDetailsImpl();
        principal.setId(5L);

        mockMvc.perform(
                        post("/api/feedback/submit")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                                .principal(new UsernamePasswordAuthenticationToken(principal, null))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.paperId").value(10L))
                .andExpect(jsonPath("$.userId").value(5L))
                .andExpect(jsonPath("$.rating").value(5));

        verify(feedbackService).submitFeedback(any(), any());
    }
}
