package com.roshan.logmonitor.dto.log;

import lombok.Data;

@Data
public class LogRequest {
    private String level;
    private String serviceName;
    private String message;
}