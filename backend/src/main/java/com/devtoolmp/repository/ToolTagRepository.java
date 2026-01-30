package com.devtoolmp.repository;

import com.devtoolmp.entity.ToolTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolTagRepository extends JpaRepository<ToolTag, Long> {

    List<ToolTag> findByToolId(Long toolId);

    void deleteByToolId(Long toolId);

    @Query("SELECT DISTINCT t.tagName FROM ToolTag t")
    List<String> findDistinctTagNames();
}
