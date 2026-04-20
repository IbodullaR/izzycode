package com.code.algonix.discussion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Long> {
    Page<Discussion> findByProblemIdOrderByPinnedDescLikesDescCreatedAtDesc(Long problemId, Pageable pageable);
}
