package me.nuguri.account.repository;

import me.nuguri.account.dto.ClientSearchCondition;
import me.nuguri.common.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ClientRepositoryCustom {

    Page<Client> pageByConditionFetchAccounts(ClientSearchCondition condition, Pageable pageable);

    long deleteByIdsBatchInQuery(List<Long> ids);
}
