package com.roshan.logmonitor.kafka;

import com.roshan.logmonitor.dto.log.LogRequest; // Updated import to use your DTO
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogProducer {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final KafkaTemplate<String, LogRequest> kafkaTemplate; // Changed to LogRequest

    private static final String TOPIC = "incoming-logs";

    public void sendLog(LogRequest logRequest) { // Changed to LogRequest
        kafkaTemplate.send(TOPIC, logRequest);
        System.out.println("Producer: Log sent to Kafka topic!");
    }
}