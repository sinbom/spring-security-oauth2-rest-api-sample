package me.nuguri.resc.repository;

import me.nuguri.common.entity.Category;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CategoryRepository extends BaseRepository<Category, Long> {
}
