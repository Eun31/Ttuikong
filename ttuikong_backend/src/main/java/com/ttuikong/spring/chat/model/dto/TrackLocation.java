package com.ttuikong.spring.chat.model.dto;

public class TrackLocation {
    private String startTime;
    private String endTime;
    private Double distance; // null 허용 

    public TrackLocation() {}

    public TrackLocation(String startTime, String endTime, Double distance) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.distance = distance;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}
