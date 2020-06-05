package me.nuguri.account.repository;

import me.nuguri.account.dto.ClientSearchCondition;
import me.nuguri.common.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientRepositoryCustom {

    Page<Client> pageByConditionFetchAccounts(ClientSearchCondition condition, Pageable pageable);

}
