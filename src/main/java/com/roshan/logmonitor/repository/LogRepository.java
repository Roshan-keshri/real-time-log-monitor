package com.roshan.logmonitor.repository;

import com.roshan.logmonitor.entity.Company;
import com.roshan.logmonitor.entity.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LogRepository extends JpaRepository<LogEntry, Long>, JpaSpecificationExecutor<LogEntry> {

    // Count how many errors a company had since a specific time
    @Query("SELECT COUNT(l) FROM LogEntry l WHERE l.company = :company AND l.level = 'ERROR' AND l.timestamp >= :since")
    long countRecentErrors(@Param("company") Company company, @Param("since") LocalDateTime since);

}