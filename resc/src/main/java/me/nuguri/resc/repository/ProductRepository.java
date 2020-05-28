package me.nuguri.resc.repository;

import me.nuguri.common.entity.Product;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ProductRepository extends BaseRepository<Product, Long> {
}
