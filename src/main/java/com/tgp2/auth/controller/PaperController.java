package com.tgp2.auth.controller;
import com.tgp2.auth.dto.ApiResponse;
import com.tgp2.auth.entity.Paper;
import com.tgp2.auth.entity.User;
import com.tgp2.auth.service.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/papers")
@RequiredArgsConstructor
public class PaperController {

    private final PaperService paperService;

    @PostMapping
    public ResponseEntity<Paper> createPaper(@RequestBody Paper paper,
                                             @AuthenticationPrincipal User user) {
        Paper saved = paperService.createPaper(paper, user);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Paper>> getMyPapers(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paperService.getUserPapers(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePaper(@PathVariable Long id,
                                                   @AuthenticationPrincipal User user) {
        paperService.deletePaper(id, user);
        return ResponseEntity.ok(new ApiResponse(true, "Paper deleted successfully"));
    }
}