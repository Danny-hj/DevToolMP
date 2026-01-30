package com.devtoolmp.repository;

import com.devtoolmp.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndToolId(Long userId, Long toolId);

    Page<Favorite> findByUserId(Long userId, Pageable pageable);

    void deleteByUserIdAndToolId(Long userId, Long toolId);

    boolean existsByUserIdAndToolId(Long userId, Long toolId);
}
