package me.nuguri.resource.repository;

import me.nuguri.resource.entity.CategoryBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryBookRepository extends JpaRepository<CategoryBook, Long> {
}
