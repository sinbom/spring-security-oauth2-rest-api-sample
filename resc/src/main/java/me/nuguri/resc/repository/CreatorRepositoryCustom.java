package me.nuguri.resc.repository;

import me.nuguri.resc.domain.CreatorSearchCondition;
import me.nuguri.resc.entity.Creator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CreatorRepositoryCustom {

    void deleteByIds(Long... ids);

    Page<Creator> findByCondition(CreatorSearchCondition condition, Pageable pageable);

}
