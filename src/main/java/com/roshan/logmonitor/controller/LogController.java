package com.roshan.logmonitor.controller;

import com.roshan.logmonitor.dto.log.LogRequest;
import com.roshan.logmonitor.entity.Company; // Added this import
import com.roshan.logmonitor.entity.LogEntry;
import com.roshan.logmonitor.kafka.LogProducer;
import com.roshan.logmonitor.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;
    private final LogProducer logProducer;

    @PostMapping
    public ResponseEntity<String> ingestLog(@RequestBody LogRequest request) {
        // 1. Grab the company while we still have the HTTP Security Context!
        Company company = logService.getCurrentCompany();

        // 2. Attach the ID to the request object so Kafka can carry it to the background thread
        request.setCompanyId(company.getId());

        // 3. Send to Kafka instantly
        logProducer.sendLog(request);

        // 4. Return 202 Accepted immediately!
        return ResponseEntity.accepted().body("Log received and queued successfully!");
    }

    @GetMapping
    public ResponseEntity<Page<LogEntry>> getLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(logService.searchLogs(level, keyword, serviceName, start, end, page, size));
    }
}