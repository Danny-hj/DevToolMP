package com.devtoolmp.mapper;

import com.devtoolmp.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    Favorite findById(@Param("id") Long id);

    Favorite findByClientIdentifierAndToolId(@Param("clientIdentifier") String clientIdentifier, @Param("toolId") Long toolId);

    List<Favorite> findByClientIdentifier(@Param("clientIdentifier") String clientIdentifier, @Param("offset") int offset, @Param("limit") int limit);

    int countByClientIdentifier(@Param("clientIdentifier") String clientIdentifier);

    void insert(Favorite favorite);

    void deleteById(@Param("id") Long id);

    void deleteByClientIdentifierAndToolId(@Param("clientIdentifier") String clientIdentifier, @Param("toolId") Long toolId);

    boolean existsByClientIdentifierAndToolId(@Param("clientIdentifier") String clientIdentifier, @Param("toolId") Long toolId);
}
