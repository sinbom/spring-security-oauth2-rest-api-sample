package me.nuguri.resc.repository;

import me.nuguri.common.entity.MajorCategory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MajorCategoryRepository extends BaseRepository<MajorCategory, Long> {
}
