package org.rus.product.entity.repository.port;

import lombok.extern.slf4j.Slf4j;
import org.rus.product.domain.Product;
import org.rus.product.dto.PageRequest;
import org.rus.product.dto.PageResponse;
import org.rus.product.enums.Status;
import org.rus.product.entity.model.ProductEntity;
import org.rus.product.entity.repository.ProductJpaRepository;
import org.rus.product.entity.repository.ProductRepository;
import org.rus.product.mapper.ProductMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Slf4j
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    public ProductRepositoryAdapter(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = ProductMapper.fromDomain(product);
        ProductEntity saved = jpaRepository.save(entity);
        return ProductMapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaRepository.findById(id).map(ProductMapper::toDomain);
    }

    @Override
    public PageResponse<Product> findAvailableProducts(PageRequest pageRequest) {
        log.debug("Finding available products, page: {}, size: {}",
                pageRequest.getPage(), pageRequest.getSize());

        Pageable pageable = createPageable(pageRequest);
        Page<ProductEntity> page = jpaRepository.findAvailable(Status.ACTIVE, pageable);

        return createPageResponse(page, pageRequest);
    }

    @Override
    public void delete(Product product) {
        product.markAsDeleted();  // soft delete with product method
        save(product);  // save changes
    }

    /**
     * Create Pageable from PageRequest
     */
    private Pageable createPageable(PageRequest pageRequest) {
        return org.springframework.data.domain.PageRequest.of(
                pageRequest.getPage(),
                pageRequest.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    /**
     * Create PageResponse from Page
     */
    private PageResponse<Product> createPageResponse(Page<ProductEntity> page, PageRequest pageRequest) {
        return PageResponse.of(
                page.getContent().stream()
                        .map(ProductMapper::toDomain)
                        .toList(),
                pageRequest,
                page.getTotalElements()
        );
    }

}