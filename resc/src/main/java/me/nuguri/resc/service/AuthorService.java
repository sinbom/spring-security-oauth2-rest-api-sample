package me.nuguri.resc.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.resc.entity.Creator;
import me.nuguri.resc.repository.CreatorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorService {

    private final CreatorRepository creatorRepository;

    /**
     * 저자 엔티티 페이지 조회
     * @param pageable 페이징
     * @return 조회한 저자 엔티티 페이징 객체
     */
    @Transactional(readOnly = true)
    public Page<Creator> findAll(Pageable pageable) {
        return creatorRepository.findAll(pageable);
    }

    /**
     * 저자 엔티티 조회, 식별키 조회
     * @param id 식별키
     * @return 조회한 저자 엔티티 객체
     */
    @Transactional(readOnly = true)
    public Creator find(Long id) {
        return creatorRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    /**
     * 저자 엔티티 생성, 입력 받은 파라미터 값으로 생성
     * @param creator name 이름, birth 출생날짜, death 사망 날짜
     * @return 생성한 저자 엔티티 객체
     */
    public Creator generate(Creator creator) {
        return creatorRepository.save(creator);
    }

}
