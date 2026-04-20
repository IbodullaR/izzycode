package com.code.algonix.discussion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussionReplyRepository extends JpaRepository<DiscussionReply, Long> {
    List<DiscussionReply> findByDiscussionIdAndParentReplyIsNullOrderByCreatedAtAsc(Long discussionId);
    List<DiscussionReply> findByParentReplyIdOrderByCreatedAtAsc(Long parentReplyId);
}
