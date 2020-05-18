package me.nuguri.auth.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.auth.domain.AccountAdapter;
import me.nuguri.auth.entity.Account;
import me.nuguri.auth.exception.UserNotExistException;
import me.nuguri.auth.repository.AccountRepository;
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

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return new AccountAdapter(accountRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email)));
    }

    @Transactional(readOnly = true)
    public Page<Account> findAll(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Account find(Long id) {
        return accountRepository.findById(id).orElseThrow(UserNotExistException::new);
    }

    @Transactional(readOnly = true)
    public Account find(String email) {
        return accountRepository.findByEmail(email).orElseThrow(UserNotExistException::new);
    }

    @Transactional(readOnly = true)
    public boolean exist(String email) {
        return accountRepository.existsByEmail(email);
    }

    public Account generate(Account account) {
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        return accountRepository.save(account);
    }

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

    public Account merge(Account account) {
        Account merge = accountRepository.findById(account.getId()).orElseThrow(UserNotExistException::new);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        modelMapper.map(account, merge);
        return merge;
    }

    public Account delete(Long id) {
        Account delete = accountRepository.findById(id).orElseThrow(UserNotExistException::new);
        accountRepository.delete(delete);
        return delete;
    }


}
