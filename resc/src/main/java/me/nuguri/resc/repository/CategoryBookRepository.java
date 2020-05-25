package me.nuguri.resc.repository;

import me.nuguri.resc.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryBookRepository extends JpaRepository<ProductCategory, Long> {
}
