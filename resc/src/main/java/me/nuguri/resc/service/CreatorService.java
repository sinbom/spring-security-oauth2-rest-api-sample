package me.nuguri.resc.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.resc.controller.api.CreatorApiController;
import me.nuguri.resc.entity.Creator;
import me.nuguri.resc.entity.Product;
import me.nuguri.resc.repository.CreatorRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CreatorService {

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
     * 저자 엔티티 페이지 조회 + 상품 엔티티 지연 로딩 조회
     * @param pageable 페이징
     * @return 조회한 저자 엔티티 페이징 객체
     */
    @Transactional(readOnly = true)
    public Page<Creator> findAllWithProduct(Pageable pageable) {
        Page<Creator> page = creatorRepository.findAll(pageable);
        Optional<Creator> first = page.get().findFirst();
        first.ifPresent(creator -> {
            Product product = creator.getProducts().get(0);
            if (product != null) {
                product.getId(); // 컬렉션 페치 조인으로 인해 Batch Size 지연 로딩 사용
            }
        });
        return page;
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

    /**
     * 저자 엔티티 수정, 입력 받은 모든 파라미터로 대입해서 수정
     * @param creator name 이름, gender 설병, birth 출생 날짜, death 사망 날짜
     * @return 수정한 저자 엔티티 객체
     */
    public Creator update(Creator creator) {
        Creator update = creatorRepository.findById(creator.getId()).orElseThrow(NoSuchElementException::new);
        if (!StringUtils.isEmpty(creator.getName())) {
            update.setName(creator.getName());
        }
        if (creator.getGender() != null) {
            update.setGender(creator.getGender());
        }
        if (creator.getBirth() != null) {
            update.setBirth(creator.getBirth());
        }
        if (creator.getDeath() != null) {
            update.setDeath(creator.getDeath());
        }
        return update;
    }
}
