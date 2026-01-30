package com.devtoolmp.mapper;

import com.devtoolmp.entity.TelemetryData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TelemetryDataMapper {

    TelemetryData findById(@Param("id") Long id);

    List<TelemetryData> findByToolIdAndCreatedAtBetween(
            @Param("toolId") Long toolId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<TelemetryData> findByEventTypeAndCreatedAtBetween(
            @Param("eventType") String eventType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    void insert(TelemetryData telemetryData);

    void deleteById(@Param("id") Long id);
}
