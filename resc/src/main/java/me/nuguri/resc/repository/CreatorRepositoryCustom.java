package me.nuguri.resc.repository;

import me.nuguri.common.entity.Creator;
import me.nuguri.resc.domain.CreatorSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface CreatorRepositoryCustom {

    long deleteByIds(List<Long> ids);

    Page<Creator> findByCondition(CreatorSearchCondition condition, Pageable pageable);

}
