package me.nuguri.account.repository;

import me.nuguri.common.entity.Client;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface ClientRepository extends JpaRepository<Client, Long>, ClientRepositoryCustom {

    /**
     * 클라이언트 엔티티 식별키 조회, 계정 엔티티 패치 조인
     * @param id 식별키
     * @return 조회한 클라이언트 엔티티 객체
     */
    @Override
    @Transactional(readOnly = true)
    @EntityGraph(attributePaths = "account", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Client> findById(Long id);

    /**
     * 클라이언트 엔티티 조회, 대리키(clientId) 조회
     *
     * @param clientId 클라이언트 id
     * @return 조회한 클라이언트 엔티티 객체
     */
    @Transactional(readOnly = true)
    Optional<Client> findByClientId(String clientId);

}

