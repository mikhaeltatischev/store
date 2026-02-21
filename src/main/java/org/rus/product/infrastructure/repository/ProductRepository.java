package org.rus.product.infrastructure.repository;

import org.rus.product.domain.Product;
import org.rus.product.dto.PageRequest;
import org.rus.product.dto.PageResponse;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);
    Optional<Product> findById(UUID id);
    void delete(Product product);

    PageResponse<Product> findByCreatorId(UUID creatorId, PageRequest pageRequest);
    PageResponse<Product> findByCategoryId(UUID categoryId, PageRequest pageRequest);
    PageResponse<Product> findAvailableProducts(PageRequest pageRequest);

}