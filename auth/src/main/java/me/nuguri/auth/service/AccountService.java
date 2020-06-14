package me.nuguri.auth.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.repository.AccountRepository;
import me.nuguri.common.adapter.AccountAdapter;
import me.nuguri.common.entity.Account;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    /**
     * 시큐리티 로그인 및 인증 토큰 발급(password 방식) 수행 시 사용, 유저 엔티티 대리키(email) 조회
     * @param email 이메일
     * @return 유저 엔티티 래핑 + 시큐리티 인증 객체
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        return new AccountAdapter(account);
    }

}
