package com.tgp2.auth.repository;

import com.tgp2.auth.entity.Paper;
import com.tgp2.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaperRepository extends JpaRepository<Paper, Long> {
    List<Paper> findByUser(User user);
}