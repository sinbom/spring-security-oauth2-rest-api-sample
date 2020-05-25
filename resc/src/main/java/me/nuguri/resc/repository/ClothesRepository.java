package me.nuguri.resc.repository;

import me.nuguri.resc.entity.Clothes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothesRepository extends JpaRepository<Clothes, Long> {
}
