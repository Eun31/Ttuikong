package com.ttuikong.spring.model.dto;

public class User {
	private int id;
	private String email;
	private String password;
	private String nickname;
	private String gender;
	private int age;
	private float height;
	private float weight;
	private String activityLevel;
	private String activityGoal;
	private String role;
	
	private float avgDistance;
	private float totalDistance;
	private int totalRuns;

	public User() {
	}

	public User(String email, String password) {
		this.email = email;
		this.password = password;
	}

	public User(int id, String email, String password, String nickname, String gender, int age, float height,
			Float weight, String activityLevel, String activityGoal, String role) {
		super();
		this.id = id;
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.gender = gender;
		this.age = age;
		this.height = height;
		this.weight = weight;
		this.activityLevel = activityLevel;
		this.activityGoal = activityGoal;
		this.role = role;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public Float getWeight() {
		return weight;
	}

	public void setWeight(Float weight) {
		this.weight = weight;
	}

	public String getActivityLevel() {
		return activityLevel;
	}

	public void setActivityLevel(String activityLevel) {
		this.activityLevel = activityLevel;
	}

	public String getActivityGoal() {
		return activityGoal;
	}

	public void setActivityGoal(String activityGoal) {
		this.activityGoal = activityGoal;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Float getAvgDistance() {
		return avgDistance;
	}
	
	public void setAvgDistance(float avgDistance) {
		this.avgDistance = avgDistance;
	}
	
	public Float getTotalDistance() {
		return totalDistance;
	}
	
	public void setTotalDistance(float totalDistance) {
		this.totalDistance = totalDistance;
	}
	
	public int getTotalRuns() {
		return totalRuns;
	}
	
	public void setTotalRuns(int totalRuns) {
		this.totalRuns = totalRuns;
	}
	
	@Override
	public String toString() {
		return "User [id=" + id + ", email=" + email + ", password=" + password + ", nickname=" + nickname + ", gender="
				+ gender + ", age=" + age + ", height=" + height + ", weight=" + weight + ", activityLevel="
				+ activityLevel + ", activityGoal=" + activityGoal + ", role=" + role + "]";
	}

}
