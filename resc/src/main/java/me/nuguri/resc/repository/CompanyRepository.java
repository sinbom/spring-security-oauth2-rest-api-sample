package me.nuguri.resc.repository;

import me.nuguri.common.entity.Company;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CompanyRepository extends BaseRepository<Company, Long> {
}
