package com.devtoolmp.mapper;

import com.devtoolmp.entity.ViewRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ViewRecordMapper {

    ViewRecord findById(@Param("id") Long id);

    List<ViewRecord> findByToolIdAndCreatedAtBetween(
            @Param("toolId") Long toolId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    void insert(ViewRecord viewRecord);

    void deleteById(@Param("id") Long id);

    // 统计相关方法
    int countByToolId(@Param("toolId") Long toolId);

    int countByToolIdAndCreatedAtBetween(
            @Param("toolId") Long toolId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
