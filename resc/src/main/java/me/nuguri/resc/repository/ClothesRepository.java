package me.nuguri.resc.repository;

import me.nuguri.resc.entity.Clothes;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ClothesRepository extends BaseRepository<Clothes, Long> {
}
