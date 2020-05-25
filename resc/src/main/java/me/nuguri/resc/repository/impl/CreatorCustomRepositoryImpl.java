package me.nuguri.resc.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import me.nuguri.resc.repository.custom.CreatorCustomRepository;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CreatorCustomRepositoryImpl implements CreatorCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

}
