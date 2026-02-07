package com.devtoolmp.mapper;

import com.devtoolmp.entity.InstallRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface InstallRecordMapper {

    InstallRecord findById(@Param("id") Long id);

    List<InstallRecord> findByToolIdAndCreatedAtBetween(
            @Param("toolId") Long toolId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    void insert(InstallRecord installRecord);

    void deleteById(@Param("id") Long id);

    // 统计相关方法
    int countByToolId(@Param("toolId") Long toolId);

    int countByToolIdAndCreatedAtBetween(
            @Param("toolId") Long toolId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
