package me.nuguri.resource.repository;

import me.nuguri.resource.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
