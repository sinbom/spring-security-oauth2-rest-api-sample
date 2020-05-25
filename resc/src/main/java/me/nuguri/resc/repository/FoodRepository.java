package me.nuguri.resc.repository;

import me.nuguri.resc.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodRepository extends JpaRepository<Food, Long> {
}
