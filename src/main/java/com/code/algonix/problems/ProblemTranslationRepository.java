package com.code.algonix.problems;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemTranslationRepository extends JpaRepository<ProblemTranslation, Long> {
    Optional<ProblemTranslation> findByProblemIdAndLanguage(Long problemId, String language);
    List<ProblemTranslation> findByProblemId(Long problemId);
    void deleteByProblemIdAndLanguage(Long problemId, String language);
}
