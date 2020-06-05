package me.nuguri.resc.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.common.entity.Creator;
import me.nuguri.common.entity.Product;
import me.nuguri.resc.domain.CreatorSearchCondition;
import me.nuguri.resc.repository.CreatorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class CreatorService {

    private final CreatorRepository creatorRepository;

    /**
     * 저자 엔티티 페이지 조회
     *
     * @param pageable 페이징
     * @return 조회한 저자 엔티티 페이징 객체
     */
    @Transactional(readOnly = true)
    public Page<Creator> pagingWithCondition(CreatorSearchCondition condition, Pageable pageable) {
        Page<Creator> page = creatorRepository.pageByCondition(condition, pageable);
        // TODO lazy Loading 부분 서비스 로직과 분리해야함
        page
                .stream()
                .findFirst()
                .ifPresent(c -> {
                    List<Product> products = c.getProducts();
                    if (!products.isEmpty()) {
                        // 페이징 조회로 패치 조인 불가능으로인해 컬렉션 Batch Size Lazy Loading 사용
                        products.get(0).getId();
                    }
                });
        return page;
    }

    public boolean exist(Long id) {
        return creatorRepository.existsById(id);
    }

    /**
     * 저자 엔티티 조회, 식별키 조회
     *
     * @param id 식별키
     * @return 조회한 저자 엔티티 객체
     */
    @Transactional(readOnly = true)
    public Creator find(Long id) {
        return creatorRepository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    /**
     * 저자 엔티티 생성, 입력 받은 파라미터 값으로 생성
     *
     * @param creator name 이름, birth 출생날짜, death 사망 날짜
     * @return 생성한 저자 엔티티 객체
     */
    public Creator generate(Creator creator) {
        return creatorRepository.generate(creator);
    }

    /**
     * 저자 엔티티 수정, 입력 받은 모든 파라미터로 대입해서 수정
     *
     * @param creator name 이름, gender 설병, birth 출생 날짜, death 사망 날짜
     * @return 수정한 저자 엔티티 객체
     */
    public Creator update(Creator creator) {
        Creator update = find(creator.getId());
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

    /**
     * 저자 엔티티 병합, 입력 받은 모든 파라미터 모두 대입해서 수정, 식별키에 해당하는 저자가 없는 경우 생성
     *
     * @param creator name 이름, gender 성별, birth 생년 날짜, death 사망 날짜
     * @return 병합한 유저 엔티티 객체
     */
    public Creator merge(Creator creator) {
        return creatorRepository.merge(creator);
    }

    /**
     * 저자 엔티티 제거
     * @param id 식별키
     */
    public void delete(Long id) {
        Creator creator = find(id);
        creatorRepository.delete(creator);
    }

    /**
     * 저자 엔티티 제거, in batch 쿼리
     * @param ids 식별키
     */
    public long deleteInBatch(List<Long> ids) {
        return creatorRepository.deleteByIdBatchInQuery(ids);
    }

}
