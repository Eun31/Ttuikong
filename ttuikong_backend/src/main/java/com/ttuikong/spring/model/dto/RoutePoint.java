package com.ttuikong.spring.model.dto;

public class RoutePoint {
	private double lat;    // 위도
    private double lng;    // 경도
    private double elevation; // 고도 (미터)
    private String time;   // ISO 8601 형식 시간 (2024-05-07T09:30:00Z)
    
    public RoutePoint() {
	}

	public RoutePoint(double lat, double lng, double elevation, String time) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.elevation = elevation;
		this.time = time;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public double getElevation() {
		return elevation;
	}

	public void setElevation(double elevation) {
		this.elevation = elevation;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "RoutePoint [lat=" + lat + ", lng=" + lng + ", elevation=" + elevation + ", time=" + time + "]";
	}
    
}
