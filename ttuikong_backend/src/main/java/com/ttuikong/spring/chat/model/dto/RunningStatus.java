package com.ttuikong.spring.chat.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RunningStatus {
    
    @JsonProperty("startTime")
    private String startTime;
    
    @JsonProperty("status")
    private String status;

    public RunningStatus() {}

    public RunningStatus(String startTime, String status) {
        this.startTime = startTime;
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
