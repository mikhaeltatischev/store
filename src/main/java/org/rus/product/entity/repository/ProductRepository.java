package org.rus.product.entity.repository;

import org.rus.product.domain.Product;
import org.rus.product.dto.PageRequest;
import org.rus.product.dto.PageResponse;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);
    Optional<Product> findById(UUID id);
    void delete(Product product);

    PageResponse<Product> findAvailableProducts(PageRequest pageRequest);

}