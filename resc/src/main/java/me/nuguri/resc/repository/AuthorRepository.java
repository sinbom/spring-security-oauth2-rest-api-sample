package me.nuguri.resc.repository;

import me.nuguri.resc.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    @Override
    @EntityGraph(attributePaths = "books", type = EntityGraph.EntityGraphType.FETCH)
    Page<Author> findAll(Pageable pageable);
}
