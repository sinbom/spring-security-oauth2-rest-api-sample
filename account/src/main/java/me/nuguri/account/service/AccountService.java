package me.nuguri.account.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.account.repository.AccountRepository;
import me.nuguri.common.dto.AccountAdapter;
import me.nuguri.common.entity.Account;
import me.nuguri.common.entity.Address;
import me.nuguri.common.enums.Gender;
import me.nuguri.common.enums.Role;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

import static org.springframework.util.StringUtils.hasText;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    /**
     * 시큐리티 로그인 및 인증 토큰 발급(password 방식) 수행 시 사용, 유저 엔티티 대리키(email) 조회
     *
     * @param email 이메일
     * @return 유저 엔티티 래핑 + 시큐리티 인증 객체
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        Account account = accountRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        return new AccountAdapter(account);
    }

    /**
     * 유저 엔티티 생성, 입력 받은 파라미터 값으로 생성
     *
     * @param account email 이메일, password 비밀번호, name 이름, roles 권한
     * @return 생성한 유저 엔티티 객체
     */
    public Account generate(Account account) {
        String password = account.getPassword();
        password = passwordEncoder.encode(password);
        account.setPassword(password);
        return accountRepository.save(account);
    }

    /**
     * 유저 엔티티 수정, 입력 받은 파라미터(Not Null Fields)만 대입해서 수정
     *
     * @param account password 비밀번호, name 이름, gender 성별, address 주소, roles 권한
     * @return 수정한 유저 엔티티 객체
     */
    public Account update(Account account) {
        Long id = account.getId();
        Account update = accountRepository
                .findById(id)
                .orElseThrow(EntityNotFoundException::new);
        String password = account.getPassword();
        String name = account.getName();
        Gender gender = account.getGender();
        Address address = account.getAddress();
        Role role = account.getRole();
        if (hasText(password)) {
            password = passwordEncoder.encode(password);
            update.setPassword(password);
        }
        if (hasText(name)) {
            update.setName(name);
        }
        if (gender != null) {
            update.setGender(gender);
        }
        if (address != null) {
            update.setAddress(address);
        }
        if (role != null) {
            update.setRole(role);
        }
        return update;
    }

    /**
     * 유저 엔티티 병합, 입력 받은 모든 파라미터 모두 대입해서 수정, 식별키에 해당하는 유저가 없는 경우 생성하지는 않음
     *
     * @param account password 비밀번호, name 이름, gender 성별, address 주소, roles 권한
     * @return 병합한 유저 엔티티 객체
     */
    public Account merge(Account account) {
        Long id = account.getId();
        Account merge = accountRepository
                .findById(id)
                .orElseThrow(EntityNotFoundException::new);
        String password = account.getPassword();
        password = passwordEncoder.encode(password);
        String name = account.getName();
        Gender gender = account.getGender();
        Address address = account.getAddress();
        Role role = account.getRole();
        merge.setPassword(password);
        merge.setName(name);
        merge.setGender(gender);
        merge.setAddress(address);
        merge.setRole(role);
        return merge;
    }

}
