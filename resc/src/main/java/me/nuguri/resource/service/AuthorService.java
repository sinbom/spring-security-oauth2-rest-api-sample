package me.nuguri.resource.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.resource.entity.Author;
import me.nuguri.resource.repository.AuthorRepository;
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

}
