package me.nuguri.resc.repository;

import me.nuguri.resc.entity.Creator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CreatorRepository extends JpaRepository<Creator, Long> {

    @Override
    Page<Creator> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "products", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Creator> findById(Long id);
}
