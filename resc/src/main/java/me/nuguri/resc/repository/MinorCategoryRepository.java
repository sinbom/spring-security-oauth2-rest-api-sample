package me.nuguri.resc.repository;

import me.nuguri.common.entity.MinorCategory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MinorCategoryRepository extends BaseRepository<MinorCategory, Long> {
}
