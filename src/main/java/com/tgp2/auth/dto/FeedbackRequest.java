package com.tgp2.auth.dto;
import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {
    private Long paperId;
//    private Long userId; getting user id from securityContext instead of request.
    private String feedbackType;
    private String role;
    private Integer rating;
    private String feedbackText;
}
