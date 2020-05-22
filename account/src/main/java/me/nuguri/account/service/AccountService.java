package me.nuguri.account.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.account.exception.UserNotExistException;
import me.nuguri.account.repository.AccountRepository;
import me.nuguri.common.domain.AccountAdapter;
import me.nuguri.common.entity.Account;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    private final ModelMapper modelMapper;

    /**
     * 시큐리티 로그인 수행 시 사용, 유저 엔티티 대리키(email) 조회
     * @param email 이메일
     * @return 유저 엔티티 래핑 + 시큐리티 인증 객체
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        return new AccountAdapter(accountRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email)));
    }

    /**
     * 유저 엔티티 페이지 조회
     * @param pageable 페이징
     * @return 조회한 유저 엔티티 페이징 객체
     */
    @Transactional(readOnly = true)
    public Page<Account> findAll(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    /**
     * 유저 엔티티 조회, 식별키 조회
     * @param id 식별키
     * @return 조회한 유저 엔티티 객체
     */
    @Transactional(readOnly = true)
    public Account find(Long id) {
        return accountRepository.findById(id).orElseThrow(UserNotExistException::new);
    }

    /**
     * 유저 엔티티 조회, 대리키(email) 조회
     * @param email 이메일
     * @return 조회한 유저 엔티티 객체
     */
    @Transactional(readOnly = true)
    public Account find(String email) {
        return accountRepository.findByEmail(email).orElseThrow(UserNotExistException::new);
    }

    /**
     * 유저 엔티티 조회, 대리키(email) exist 조회
     * @param email 이메일
     * @return 존재 여부
     */
    @Transactional(readOnly = true)
    public boolean exist(String email) {
        return accountRepository.existsByEmail(email);
    }

    /**
     * 유저 엔티티 생성, 입력 받은 파라미터 값으로 생성
     * @param account email 이메일, password 비밀번호, name 이름, roles 권한
     * @return 생성한 유저 엔티티 객체
     */
    public Account generate(Account account) {
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        return accountRepository.save(account);
    }

    /**
     * 유저 엔티티 수정, 입력 받은 모든 파라미터로 대입해서 수정
     * @param account password 비밀번호, name 이름, roles 권한
     * @return 수정한 유저 엔티티 객체
     */
    public Account update(Account account) {
        Account update = accountRepository.findById(account.getId()).orElseThrow(UserNotExistException::new);
        if (!StringUtils.isEmpty(account.getPassword())) {
            update.setPassword(passwordEncoder.encode(account.getPassword()));
        }
        if (!StringUtils.isEmpty(account.getName())) {
            update.setName(account.getName());
        }
        if (!account.getRoles().isEmpty()) {
            update.setRoles(account.getRoles());
        }
        return update;
    }

    /**
     * 유저 엔티티 병합, 전달 받은 값이 없어도 수정할 수 있는(updatable) 프로퍼티 모두 대입해서 수정, 식별키에 해당 하는 유저가 없는 경우 생성하지는 않음
     * @param account password 비밀번호, name 이름, roles 권한
     * @return 병합한 유저 엔티티 객체
     */
    public Account merge(Account account) {
        Account merge = accountRepository.findById(account.getId()).orElseThrow(UserNotExistException::new);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        modelMapper.map(account, merge);
        return merge;
    }

    /**
     * 유저 엔티티 제거
     * @param id 식별키
     * @return 제거한 유저 엔티티 객체
     */
    public Account delete(Long id) {
        Account delete = accountRepository.findById(id).orElseThrow(UserNotExistException::new);
        accountRepository.delete(delete);
        return delete;
    }


}
