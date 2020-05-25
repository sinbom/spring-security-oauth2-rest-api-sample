package me.nuguri.resc.repository;

import me.nuguri.resc.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
