package com.devtoolmp.mapper;

import com.devtoolmp.entity.Codehub;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CodehubMapper {

    Codehub findById(@Param("id") Long id);

    Codehub findByOwnerAndRepo(@Param("owner") String owner, @Param("repo") String repo);

    List<Codehub> findAll();

    Codehub insert(Codehub codehub);

    void update(Codehub codehub);

    void deleteById(@Param("id") Long id);

    int countAll();
}
