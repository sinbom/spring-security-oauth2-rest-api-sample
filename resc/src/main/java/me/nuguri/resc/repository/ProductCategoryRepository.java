package me.nuguri.resc.repository;

import me.nuguri.common.entity.ProductCategory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ProductCategoryRepository extends BaseRepository<ProductCategory, Long> {
}
