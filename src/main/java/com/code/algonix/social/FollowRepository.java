package com.code.algonix.social;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.code.algonix.user.UserEntity;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    List<Follow> findByFollowerIdOrderByFollowedAtDesc(Long followerId);   // men kuzatayotganlar
    List<Follow> findByFollowingIdOrderByFollowedAtDesc(Long followingId); // meni kuzatayotganlar

    long countByFollowerId(Long followerId);
    long countByFollowingId(Long followingId);

    // Do'stlar — ikki tomonlama follow
    @Query("SELECT f.following FROM Follow f WHERE f.follower.id = :userId " +
           "AND EXISTS (SELECT f2 FROM Follow f2 WHERE f2.follower.id = f.following.id AND f2.following.id = :userId)")
    List<UserEntity> findMutualFriends(@Param("userId") Long userId);
}
