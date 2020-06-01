package me.nuguri.account.repository;

import me.nuguri.account.dto.AccountSearchCondition;
import me.nuguri.common.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountRepositoryCustom {

    Page<Account> pageByCondition(AccountSearchCondition condition, Pageable pageable);

}
