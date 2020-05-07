package me.nuguri.resource.repository;

import me.nuguri.resource.entity.MajorCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorCategoryRepository extends JpaRepository<MajorCategory, Long> {
}
