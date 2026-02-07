package com.devtoolmp.mapper;

import com.devtoolmp.entity.ToolTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ToolTagMapper {

    ToolTag findById(@Param("id") Long id);

    List<ToolTag> findByToolId(@Param("toolId") Long toolId);

    List<String> findDistinctTagNames();

    void insert(ToolTag toolTag);

    void deleteById(@Param("id") Long id);

    void deleteByToolId(@Param("toolId") Long toolId);
}
