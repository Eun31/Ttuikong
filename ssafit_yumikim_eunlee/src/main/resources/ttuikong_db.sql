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
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER'
);

-- 운동 경로 테이블
CREATE TABLE Route (
    id INT AUTO_INCREMENT PRIMARY KEY, -- 운동 경로 아이디
    start_point VARCHAR(255),
    end_point VARCHAR(255),
    distance FLOAT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 운동 기록 테이블
CREATE TABLE Daily_Record (
    id INT AUTO_INCREMENT PRIMARY KEY, -- 기록 아이디
    user_id INT NOT NULL,
    date DATE NOT NULL,
    exercise_time INT, -- 운동 시간 (분)
    distance FLOAT, -- 거리 (km)
    calories INT, -- 소모한 칼로리
    mood ENUM('기쁨', '뿌듯함', '아쉬움', '화남', '슬픔', '쏘쏘') DEFAULT NULL, -- 오늘의 기분 
    route_id INT,
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

-- 채팅방 테이블
CREATE TABLE Chat_Room (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 채팅 메시지 테이블
CREATE TABLE Chat_Message (
    id INT AUTO_INCREMENT PRIMARY KEY,
    chat_room_id INT NOT NULL,
    sender_id INT NOT NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (chat_room_id) REFERENCES Chat_Room(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES User(id) ON DELETE CASCADE
);
