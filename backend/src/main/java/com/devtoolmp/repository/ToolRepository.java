package com.devtoolmp.repository;

import com.devtoolmp.entity.Tool;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToolRepository extends JpaRepository<Tool, Long> {

    Optional<Tool> findByGithubOwnerAndGithubRepo(String githubOwner, String githubRepo);

    List<Tool> findByCategoryId(Long categoryId);

    List<Tool> findByStatus(String status);

    @Query("SELECT t FROM Tool t WHERE t.status = 'active' ORDER BY t.hotScoreDaily DESC")
    List<Tool> findTop20ByStatusOrderByHotScoreDailyDesc();

    @Query("SELECT t FROM Tool t WHERE t.status = 'active' ORDER BY t.hotScoreWeekly DESC")
    List<Tool> findTop20ByStatusOrderByHotScoreWeeklyDesc();

    @Query("SELECT t FROM Tool t WHERE t.status = 'active' ORDER BY t.hotScoreAlltime DESC")
    List<Tool> findTop20ByStatusOrderByHotScoreAlltimeDesc();

    @Query("SELECT t FROM Tool t WHERE t.status = 'active' AND " +
           "(t.name LIKE %:keyword% OR t.description LIKE %:keyword%)")
    Page<Tool> searchByKeyword(String keyword, Pageable pageable);

    Page<Tool> findByStatus(String status, Pageable pageable);
}
