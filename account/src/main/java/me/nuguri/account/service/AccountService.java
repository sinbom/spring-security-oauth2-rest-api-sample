package me.nuguri.account.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.account.repository.AccountRepository;
import me.nuguri.common.adapter.AccountAdapter;
import me.nuguri.common.adapter.AuthenticationAdapter;
import me.nuguri.common.entity.Account;
import me.nuguri.common.entity.Address;
import me.nuguri.common.enums.Gender;
import me.nuguri.common.enums.Roles;
import me.nuguri.common.exception.NoAuthorityException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

import java.util.List;

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
     * 유저 정보 조회, 관리자 권한이거나 리소스 소유자인 경우 조회 허용
     *
     * @param id             식별키
     * @param authentication 토큰 정보
     * @return
     */
    @Transactional(readOnly = true)
    public Account findById(Long id, AuthenticationAdapter authentication) {
        Long ownerId = authentication.getId();
        List<Roles> authorities = authentication.getAuthorities();
        if (authorities.stream().noneMatch(r -> r.equals(Roles.ADMIN)) && !id.equals(ownerId)) {
            throw new NoAuthorityException();
        }
        return accountRepository
                .findById(id)
                .orElseThrow(EntityNotFoundException::new);
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
     * @param account        password 비밀번호, name 이름, gender 성별, address 주소, roles 권한
     * @param authentication 토큰 정보
     * @return 수정한 유저 엔티티 객체
     */
    public Account update(Account account, AuthenticationAdapter authentication) {
        Long id = account.getId();
        Account update = findById(id, authentication);
        String password = account.getPassword();
        String name = account.getName();
        Gender gender = account.getGender();
        Address address = account.getAddress();
        Roles roles = account.getRoles();
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
        if (roles != null) {
            update.setRoles(roles);
        }
        return update;
    }

    /**
     * 유저 엔티티 병합, 입력 받은 모든 파라미터 모두 대입해서 수정, 식별키에 해당하는 유저가 없는 경우 생성하지는 않음
     *
     * @param account        password 비밀번호, name 이름, gender 성별, address 주소, roles 권한
     * @param authentication 토큰 정보
     * @return 병합한 유저 엔티티 객체
     */
    public Account merge(Account account, AuthenticationAdapter authentication) {
        Long id = account.getId();
        Account merge = findById(id, authentication);
        String password = account.getPassword();
        password = passwordEncoder.encode(password);
        String name = account.getName();
        Gender gender = account.getGender();
        Address address = account.getAddress();
        Roles roles = account.getRoles();
        merge.setPassword(password);
        merge.setName(name);
        merge.setGender(gender);
        merge.setAddress(address);
        merge.setRoles(roles);
        return merge;
    }

    /**
     * 유저 엔티티 삭제
     *
     * @param id             식별키
     * @param authentication 토큰 정보
     */
    public void delete(Long id, AuthenticationAdapter authentication) {
        Account account = findById(id, authentication);
        accountRepository.delete(account);
    }

}
