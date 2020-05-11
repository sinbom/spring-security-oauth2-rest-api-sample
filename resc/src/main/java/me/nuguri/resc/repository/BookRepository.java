package me.nuguri.resc.repository;

import me.nuguri.resc.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
