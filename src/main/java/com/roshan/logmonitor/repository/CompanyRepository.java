package com.roshan.logmonitor.repository;

import com.roshan.logmonitor.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);
    Optional<Company> findByApiKey(String apiKey);
}