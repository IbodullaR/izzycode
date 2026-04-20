package com.code.algonix.discussion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussionLikeRepository extends JpaRepository<DiscussionLike, Long> {
    boolean existsByUserIdAndDiscussionId(Long userId, Long discussionId);
    void deleteByUserIdAndDiscussionId(Long userId, Long discussionId);
}
