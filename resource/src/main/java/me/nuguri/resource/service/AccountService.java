package me.nuguri.resource.service;

import lombok.RequiredArgsConstructor;
import me.nuguri.resource.domain.AccountAdapter;
import me.nuguri.resource.entity.Account;
import me.nuguri.resource.repository.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return new AccountAdapter(accountRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email)));
    }

    public Page<Account> findAll(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    public Account find(Long id) {
        return accountRepository.findById(id).orElseThrow(RuntimeException::new);
    }

    public Account generate(Account account) {
        return accountRepository.save(account);
    }

    public void delete(Long id) {
        accountRepository.deleteById(id);
    }

}
