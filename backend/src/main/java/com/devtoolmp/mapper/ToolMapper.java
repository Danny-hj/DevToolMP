package com.devtoolmp.mapper;

import com.devtoolmp.entity.Tool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ToolMapper {

    Tool findById(@Param("id") Long id);

    Tool findByGithubOwnerAndGithubRepo(@Param("githubOwner") String githubOwner, @Param("githubRepo") String githubRepo);

    List<Tool> findByCategoryId(@Param("categoryId") Long categoryId);

    List<Tool> findByStatus(@Param("status") String status);

    List<Tool> findTop20ByStatusOrderByHotScoreDailyDesc();

    List<Tool> findTop20ByStatusOrderByHotScoreWeeklyDesc();

    List<Tool> findTop20ByStatusOrderByHotScoreAlltimeDesc();

    List<Tool> findByStatusOrderByHotScoreDailyDescWithPage(@Param("status") String status, @Param("offset") int offset, @Param("limit") int limit);

    List<Tool> findByStatusOrderByHotScoreWeeklyDescWithPage(@Param("status") String status, @Param("offset") int offset, @Param("limit") int limit);

    List<Tool> findByStatusOrderByHotScoreAlltimeDescWithPage(@Param("status") String status, @Param("offset") int offset, @Param("limit") int limit);

    List<Tool> searchByKeyword(@Param("keyword") String keyword);

    List<Tool> findByStatusWithPage(@Param("status") String status, @Param("offset") int offset, @Param("limit") int limit);

    int countByStatus(@Param("status") String status);

    int countSearchByKeyword(@Param("keyword") String keyword);

    void insert(Tool tool);

    void update(Tool tool);

    void deleteById(@Param("id") Long id);

    int countAll();

    List<Tool> findAll();
}
