package me.nuguri.resc.repository;

import me.nuguri.resc.entity.Creator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreatorRepository extends JpaRepository<Creator, Long> {

    @Override
    @EntityGraph(attributePaths = "books", type = EntityGraph.EntityGraphType.FETCH)
    Page<Creator> findAll(Pageable pageable);
}
