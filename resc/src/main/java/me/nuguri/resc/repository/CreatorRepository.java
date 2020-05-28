package me.nuguri.resc.repository;

import me.nuguri.common.entity.Creator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface CreatorRepository extends BaseRepository<Creator, Long>, CreatorRepositoryCustom {

    @Override
    @Transactional(readOnly = true)
    Page<Creator> findAll(Pageable pageable);

    @Override
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = "products", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Creator> findById(Long id);

}
