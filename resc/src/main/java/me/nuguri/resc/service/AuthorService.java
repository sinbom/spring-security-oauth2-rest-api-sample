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

    /**
     * 저자 엔티티 페이지 조회
     * @param pageable 페이징
     * @return 조회한 저자 엔티티 페이징 객체
     */
    @Transactional(readOnly = true)
    public Page<Author> findAll(Pageable pageable) {
        return authorRepository.findAll(pageable);
    }

    /**
     * 저자 엔티티 조회, 식별키 조회
     * @param id 식별키
     * @return 조회한 저자 엔티티 객체
     */
    @Transactional(readOnly = true)
    public Author find(Long id) {
        return authorRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    /**
     * 저자 엔티티 생성, 입력 받은 파라미터 값으로 생성
     * @param author name 이름, birth 출생날짜, death 사망 날짜
     * @return 생성한 저자 엔티티 객체
     */
    public Author generate(Author author) {
        return authorRepository.save(author);
    }

}
