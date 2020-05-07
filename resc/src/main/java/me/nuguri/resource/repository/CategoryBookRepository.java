package me.nuguri.resource.repository;

import me.nuguri.resource.entity.CategoryBook;
import me.nuguri.resource.entity.embedded.CategoryBookId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryBookRepository extends JpaRepository<CategoryBook, CategoryBookId> {
}
