package me.nuguri.resc.repository;

import me.nuguri.resc.entity.Product;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ProductRepository extends BaseRepository<Product, Long> {
}
