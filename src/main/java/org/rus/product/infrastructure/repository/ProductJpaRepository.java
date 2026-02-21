package org.rus.product.infrastructure.repository;

import org.rus.product.enums.Status;
import org.rus.product.infrastructure.model.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {

    // Методы с пагинацией
    Page<ProductEntity> findByCreatorId(UUID creatorId, Pageable pageable);
    Page<ProductEntity> findByCategoryId(UUID categoryId, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE p.status = :status AND p.count > 0")
    Page<ProductEntity> findAvailable(@Param("status") Status status, Pageable pageable);

    // Методы для подсчета
    long countByCategoryId(UUID categoryId);
    long countByCreatorId(UUID creatorId);

    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.status = :status AND p.count > 0")
    long countAvailable(@Param("status") Status status);

}