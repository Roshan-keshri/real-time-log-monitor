package com.roshan.logmonitor.service;

import com.roshan.logmonitor.dto.log.LogRequest;
import com.roshan.logmonitor.entity.Company;
import com.roshan.logmonitor.entity.LogEntry;
import com.roshan.logmonitor.entity.User;
import com.roshan.logmonitor.repository.CompanyRepository;
import com.roshan.logmonitor.repository.LogRepository;
import com.roshan.logmonitor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.roshan.logmonitor.specification.LogSpecification;
import org.springframework.data.jpa.domain.Specification;
import com.roshan.logmonitor.entity.Alert;
import com.roshan.logmonitor.repository.AlertRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final AlertRepository alertRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Helper method to figure out exactly which Company this request belongs to
    private Company getCurrentCompany() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nametag = auth.getName();

        if (nametag.startsWith("API_KEY_")) {
            // It's a Machine! Extract the ID and find the company directly.
            Long companyId = Long.parseLong(nametag.replace("API_KEY_", ""));
            return companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found"));
        } else {
            // It's a Human! Find the User, then get their Company.
            User user = userRepository.findByUsername(nametag)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return user.getCompany();
        }
    }

    public LogEntry saveLog(LogRequest request) {
        Company company = getCurrentCompany(); // From API Key

        LogEntry logEntry = new LogEntry();
        logEntry.setLevel(request.getLevel());
        logEntry.setServiceName(request.getServiceName());
        logEntry.setMessage(request.getMessage());
        logEntry.setCompany(company);
        logEntry.setTimestamp(LocalDateTime.now());

        LogEntry savedLog = logRepository.save(logEntry);

        // 🔥 THE ALERT ENGINE 🔥
        // Only check if this incoming log is an ERROR
        if ("ERROR".equalsIgnoreCase(savedLog.getLevel())) {

            // Look back 1 minute ago
            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);

            // Count how many errors happened in the last minute
            long errorCount = logRepository.countRecentErrors(company, oneMinuteAgo);

            // THRESHOLD: If there are 3 or more errors in 1 minute, TRIGGER ALERT!
            if (errorCount >= 3) {
                triggerAlert(company, errorCount);
            }
        }

        return savedLog;
    }

    private void triggerAlert(Company company, long errorCount) {
        // 1. Save Alert to Database
        Alert alert = new Alert();
        alert.setCompany(company);
        alert.setTimestamp(LocalDateTime.now());
        alert.setMessage("🚨 CRITICAL ALERT: " + errorCount + " errors detected in the last minute!");
        alertRepository.save(alert);

        // 2. Broadcast the Alert over WebSockets instantly!
        String alertTopic = "/topic/company/" + company.getId() + "/alerts";
        messagingTemplate.convertAndSend(alertTopic, alert);

        System.out.println("🚨 ALERT TRIGGERED FOR " + company.getName() + "!");
    }

    public Page<LogEntry> searchLogs(String level, String keyword, String serviceName, LocalDateTime start, LocalDateTime end, int page, int size) {
        Company company = getCurrentCompany();

        // 1. Setup Pagination & Sorting
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        // 2. Build the Dynamic Query
        Specification<LogEntry> spec = LogSpecification.buildQuery(company, level, serviceName, keyword, start, end);

        // 3. Execute!
        return logRepository.findAll(spec, pageable);
    }
}