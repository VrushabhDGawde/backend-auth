package com.tgp2.auth.repository;

import com.tgp2.auth.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
//    boolean existsByPaperIdAndUserId(Long paperId, Long userId);
    Feedback findByPaperIdAndUserId(Long paperId, Long userId);

}
