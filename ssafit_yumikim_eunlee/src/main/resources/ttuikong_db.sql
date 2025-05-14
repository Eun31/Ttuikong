-- drop database if exists ttuikong_db;
create database ttuikong_db;
USE ttuikong_db;

-- User 테이블
CREATE TABLE User (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    gender ENUM('남성', '여성') DEFAULT NULL,
    age INT DEFAULT NULL,
    height FLOAT DEFAULT NULL,
    weight FLOAT DEFAULT NULL,
    activity_level ENUM('느긋한 코알라', '산책하는 거북이', '신나는 강아지', '힘찬 질주 말', '전광석화 치타') DEFAULT NULL, -- 활동성
    activity_goal ENUM('느긋한 코알라', '산책하는 거북이', '신나는 강아지', '힘찬 질주 말', '전광석화 치타') DEFAULT NULL, -- 운동 목표
    avg_distance DOUBLE,
    total_distance DOUBLE,
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER'
);

-- 운동 경로 테이블
CREATE TABLE Route (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    route_name VARCHAR(100) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    duration BIGINT NOT NULL,
    distance DOUBLE NOT NULL,
	calories DOUBLE,
    points TEXT NOT NULL,  -- JSON 형식의 경로 포인트 데이터
    status ENUM('running', 'ended') DEFAULT NULL,
    image_url VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(id)
);

-- 운동 기록 테이블
CREATE TABLE Daily_Record (
    id INT AUTO_INCREMENT PRIMARY KEY, -- 기록 아이디
    user_id INT NOT NULL,
    route_id INT,
    date DATE,
    duration BIGINT DEFAULT NULL, -- 운동 시간 (분)
    distance DOUBLE DEFAULT NULL, -- 거리 (km)
    calories DOUBLE DEFAULT NULL, -- 소모한 칼로리
    mood ENUM('기쁨', '뿌듯함', '아쉬움', '화남', '슬픔', '쏘쏘') DEFAULT NULL, -- 오늘의 기분 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (route_id) REFERENCES Route(id) ON DELETE SET NULL
);

-- 게시판 글 테이블
CREATE TABLE Board (
    id INT AUTO_INCREMENT PRIMARY KEY, -- 게시판 아이디 
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    category VARCHAR(50) DEFAULT '자유',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
);

-- 댓글 테이블
CREATE TABLE Comment (
    id INT AUTO_INCREMENT PRIMARY KEY, -- 댓글 아이디
    board_id INT NOT NULL,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (board_id) REFERENCES Board(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
);

-- 게시글 좋아요 테이블 
CREATE TABLE Board_Like (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    board_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (board_id) REFERENCES Board(id) ON DELETE CASCADE,
    UNIQUE KEY unique_board_like (user_id, board_id) -- 하나의 게시글에는 하나의 좋아요만 허용
);

-- 사용자 팔로우/팔로잉 테이블
CREATE TABLE Follow (
    id INT AUTO_INCREMENT PRIMARY KEY,
    follower_id INT NOT NULL, -- 나를 팔로우하는 사람
    following_id INT NOT NULL, -- 내가 팔로우하는 사람
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (follower_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (following_id) REFERENCES User(id) ON DELETE CASCADE,
    UNIQUE KEY unique_follow (follower_id, following_id) -- 중복 팔로우 방지
);

-- 크루 테이블
CREATE TABLE Crew (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(255),
    creator_id INT NOT NULL,
	room_description TEXT NOT NULL,
	goal_type ENUM('SUM', 'AVERAGE') NOT NULL,
	goal_time DOUBLE NOT NULL,  -- 총 목표 시간 또는 평균 목표 시간
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	end_date DATE NOT NULL,
    start_date DATE NOT NULL,
    
    FOREIGN KEY (creator_id) REFERENCES user(id)
);


-- 크루-멤버 테이블
CREATE TABLE Crew_Member (
    id INT AUTO_INCREMENT PRIMARY KEY,
    crew_id INT NOT NULL,
    user_id INT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (crew_id) REFERENCES Crew(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,

    UNIQUE (crew_id, user_id)  -- 중복 가입 방지
);

-- 채팅 테이블
CREATE TABLE Chat (
    id INT AUTO_INCREMENT PRIMARY KEY,
    crew_id INT NOT NULL,  
    sender_id INT NOT NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (crew_id) REFERENCES Crew(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES User(id) ON DELETE CASCADE
);

