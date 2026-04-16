package com.roshan.logmonitor.controller;

import com.roshan.logmonitor.dto.log.LogRequest;
import com.roshan.logmonitor.entity.LogEntry;
import com.roshan.logmonitor.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    // 1. Inject the WebSocket Messaging Template
    private final SimpMessagingTemplate messagingTemplate;

    // 1. INGESTION ENDPOINT (Now using the secure LogRequest DTO!)
    // POST http://localhost:8080/api/logs
    @PostMapping
    public ResponseEntity<LogEntry> ingestLog(@RequestBody LogRequest request) {
        // Save the log to the database
        LogEntry savedLog = logService.saveLog(request);

        // 2. THE MAGIC: Broadcast this log to this specific company's secure channel!
        if (savedLog.getCompany() != null) {
            String topic = "/topic/company/" + savedLog.getCompany().getId() + "/logs";
            messagingTemplate.convertAndSend(topic, savedLog);
        }

        return ResponseEntity.ok(savedLog);
    }

    // 2. SEARCH & FILTER ENDPOINT
    // GET http://localhost:8080/api/logs?level=ERROR&keyword=database
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