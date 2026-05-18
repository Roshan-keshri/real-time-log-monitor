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

    public Company getCurrentCompany() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String nametag = auth.getName();

        if (nametag.startsWith("API_KEY_")) {
            Long companyId = Long.parseLong(nametag.replace("API_KEY_", ""));
            return companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found"));
        } else {
            User user = userRepository.findByUsername(nametag)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return user.getCompany();
        }
    }

    public LogEntry saveLog(LogRequest request) {
        // Fetch the company using the ID passed through Kafka!
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        LogEntry logEntry = new LogEntry();
        logEntry.setLevel(request.getLevel());
        logEntry.setServiceName(request.getServiceName());
        logEntry.setMessage(request.getMessage());
        logEntry.setCompany(company);
        logEntry.setTimestamp(LocalDateTime.now());

        LogEntry savedLog = logRepository.save(logEntry);

        if ("ERROR".equalsIgnoreCase(savedLog.getLevel())) {
            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
            long errorCount = logRepository.countRecentErrors(company, oneMinuteAgo);

            if (errorCount >= 3) {
                triggerAlert(company, errorCount);
            }
        }

        return savedLog;
    }

    private void triggerAlert(Company company, long errorCount) {
        Alert alert = new Alert();
        alert.setCompany(company);
        alert.setTimestamp(LocalDateTime.now());
        alert.setMessage("🚨 CRITICAL ALERT: " + errorCount + " errors detected in the last minute!");
        alertRepository.save(alert);

        String alertTopic = "/topic/company/" + company.getId() + "/alerts";
        messagingTemplate.convertAndSend(alertTopic, alert);

        System.out.println("🚨 ALERT TRIGGERED FOR " + company.getName() + "!");
    }

    public Page<LogEntry> searchLogs(String level, String keyword, String serviceName, LocalDateTime start, LocalDateTime end, int page, int size) {
        Company company = getCurrentCompany();
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Specification<LogEntry> spec = LogSpecification.buildQuery(company, level, serviceName, keyword, start, end);
        return logRepository.findAll(spec, pageable);
    }
}