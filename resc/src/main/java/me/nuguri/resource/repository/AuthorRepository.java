package me.nuguri.resource.repository;

import me.nuguri.resource.entity.Author;
import org.hibernate.FetchMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.FetchType;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    @Override
    @EntityGraph(attributePaths = "books", type = EntityGraph.EntityGraphType.FETCH)
    Page<Author> findAll(Pageable pageable);
}
