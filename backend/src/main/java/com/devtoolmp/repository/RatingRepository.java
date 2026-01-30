package com.devtoolmp.repository;

import com.devtoolmp.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByToolIdAndUserId(Long toolId, Long userId);

    Page<Rating> findByToolId(Long toolId, Pageable pageable);

    Page<Rating> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.toolId = :toolId")
    Double getAverageScoreByToolId(Long toolId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.toolId = :toolId AND r.score = :score")
    Long countByToolIdAndScore(Long toolId, Integer score);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.toolId = :toolId")
    Long countByToolId(Long toolId);

    @Query("SELECT r.score, COUNT(r) as count FROM Rating r WHERE r.toolId = :toolId GROUP BY r.score")
    List<Object[]> getScoreDistribution(Long toolId);
}
