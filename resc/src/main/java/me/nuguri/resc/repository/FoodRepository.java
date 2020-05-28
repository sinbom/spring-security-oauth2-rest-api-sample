package me.nuguri.resc.repository;

import me.nuguri.common.entity.Food;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface FoodRepository extends BaseRepository<Food, Long> {
}
