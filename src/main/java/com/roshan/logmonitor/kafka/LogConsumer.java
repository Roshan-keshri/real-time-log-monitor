package com.roshan.logmonitor.kafka;

import com.roshan.logmonitor.dto.log.LogRequest; // Make sure to import your LogRequest!
import com.roshan.logmonitor.entity.LogEntry;
import com.roshan.logmonitor.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogConsumer {

    private final LogService logService;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "incoming-logs", groupId = "log-monitoring-group")
    public void consume(LogRequest logRequest) { // Changed to LogRequest here!
        System.out.println("Consumer: Grabbed log from Kafka!");

        // 1. Save to the Database (Now it perfectly matches what LogService expects!)
        LogEntry savedLog = logService.saveLog(logRequest);

        // 2. Push to the WebSocket
        if (savedLog.getCompany() != null) {
            String destination = "/topic/company/" + savedLog.getCompany().getId() + "/logs";
            messagingTemplate.convertAndSend(destination, savedLog);
            System.out.println("Consumer: Sent log to WebSocket: " + destination);
        } else {
            messagingTemplate.convertAndSend("/topic/logs", savedLog);
        }
    }
}