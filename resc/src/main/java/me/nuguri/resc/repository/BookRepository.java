package me.nuguri.resc.repository;

import me.nuguri.resc.entity.Book;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface BookRepository extends BaseRepository<Book, Long> {
}
