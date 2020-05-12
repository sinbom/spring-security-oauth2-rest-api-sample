package me.nuguri.resc.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.resc.entity.Author;
import me.nuguri.resc.repository.AuthorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorService {

    private final AuthorRepository authorRepository;

    @Transactional(readOnly = true)
    public Page<Author> findAll(Pageable pageable) {
        return authorRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Author find(Long id) {
        return authorRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    public Author generate(Author author) {
        return authorRepository.save(author);
    }

}
