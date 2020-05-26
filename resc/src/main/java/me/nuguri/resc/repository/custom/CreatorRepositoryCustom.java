package me.nuguri.resc.repository.custom;

import me.nuguri.common.domain.Pagination;
import me.nuguri.resc.domain.CreatorSearchCondition;
import me.nuguri.resc.entity.Creator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CreatorRepositoryCustom {

    Page<Creator> findWithCondition(CreatorSearchCondition condition, Pageable pageable);

}
