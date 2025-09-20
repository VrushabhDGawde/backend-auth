package com.tgp2.auth.service;


import com.tgp2.auth.entity.Paper;
import com.tgp2.auth.entity.User;
import com.tgp2.auth.repository.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaperService {

    private final PaperRepository paperRepository;

    public Paper createPaper(Paper paper, User user) {
        paper.setUser(user);
        return paperRepository.save(paper);
    }

    public List<Paper> getUserPapers(User user) {
        return paperRepository.findByUser(user);
    }

    public void deletePaper(Long paperId, User user) {
        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new RuntimeException("Paper not found"));

        if (!paper.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this paper");
        }

        paperRepository.delete(paper);
    }
}