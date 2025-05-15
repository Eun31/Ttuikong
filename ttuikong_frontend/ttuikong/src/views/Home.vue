<template>
  <div class="home-container">
    <!-- 오늘의 추천 러닝 정보 박스 -->
    <div class="welcome-card">
      <div class="welcome-info">
        <h1>안녕하세요, <span class="username">{{ userName }}</span>님!</h1>
        <p>오늘의 추천 러닝 거리는 <span class="highlight">{{ recommendation }}km</span>예요</p>
        <button class="start-run-btn" @click="startRunning">
          <i class="ri-run-line"></i> 지금 달리기
        </button>
      </div>
      <div class="welcome-stats">
        <div class="stat-card">
          <span class="stat-label">총 거리</span>
          <span class="stat-value">{{ monthlyDistance }}</span>
        </div>
        <div class="stat-card highlight">
          <span class="stat-label">총 횟수</span>
          <span class="stat-value">{{ monthlyCount }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">챌린지</span>
          <span class="stat-value">{{ challengeCount }}</span>
        </div>
      </div>
    </div>

    <!-- 콩 성장 박스 -->
    <div class="bean-growth-box">
      <h3>콩이 자라는 중이에요~</h3>
      <div class="progress-container">
        <div class="progress-bar" :style="{ width: growthRate + '%' }">
          <span class="bean-icon"><i class="ri-seed-line"></i></span>
        </div>
        <span class="progress-text">{{ growthRate }}%</span>
      </div>
    </div>

    <!-- 메인 메뉴 -->
    <div class="main-menu">
      <router-link to="/challenge" class="menu-card">
        <i class="ri-trophy-line"></i>
        <span>챌린지</span>
      </router-link>
      <router-link to="/calendar" class="menu-card">
        <i class="ri-calendar-line"></i>
        <span>캘린더</span>
      </router-link>
      <router-link to="/routes" class="menu-card">
        <i class="ri-route-line"></i>
        <span>루트 찾기</span>
      </router-link>
      <router-link to="/board" class="menu-card">
        <i class="ri-discuss-line"></i>
        <span>게시판</span>
      </router-link>
    </div>

    <!-- 인기 게시글 / 팔로우 게시글 -->
    <div class="section">
      <div class="section-header">
        <h2>인기 게시글</h2>
        <button class="toggle-feed" @click="toggleFeedType">
          {{ isPopularFeed ? '팔로우 게시글 보기' : '인기 게시글 보기' }}
        </button>
      </div>
      
      <div class="post-container">
        <div v-for="post in currentFeed" :key="post.id" class="post-item">
          <div class="post-user">
            <img :src="post.authorAvatar" class="avatar" />
            <span class="username">{{ post.author }}</span>
          </div>
          <div class="post-content">
            <h3>{{ post.title }}</h3>
            <p>{{ post.content }}</p>
          </div>
          <div class="post-meta">
            <span><i class="ri-heart-line"></i> {{ post.likes }}</span>
            <span><i class="ri-chat-1-line"></i> {{ post.comments }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      userName: '김러너',
      recommendation: 5.2,
      monthlyDistance: 45.8,
      monthlyCount: 12,
      challengeCount: 3,
      growthRate: 65,
      isPopularFeed: true,
      popularPosts: [
        {
          id: 1,
          author: '러닝마스터',
          authorAvatar: 'https://via.placeholder.com/36',
          title: '초보 러너를 위한 효과적인 훈련법',
          content: '처음 러닝을 시작하시는 분들을 위한 팁을 공유합니다. 첫째, 천천히 시작하세요...',
          likes: 42,
          comments: 12,
          date: '2시간 전'
        },
        {
          id: 2,
          author: '마라톤조아',
          authorAvatar: 'https://via.placeholder.com/36',
          title: '서울 근교 러닝 코스 추천',
          content: '주말에 러닝하기 좋은 서울 근교 코스를 소개합니다. 한강공원은 물론...',
          likes: 35,
          comments: 8,
          date: '3시간 전'
        }
      ],
      followPosts: [
        {
          id: 3,
          author: '달려라하니',
          authorAvatar: 'https://via.placeholder.com/36',
          title: '오늘의 러닝 완료!',
          content: '오늘도 5km 러닝 완료했습니다. 날씨가 정말 좋았어요!',
          likes: 15,
          comments: 3,
          date: '1시간 전'
        },
        {
          id: 4,
          author: '조깅왕',
          authorAvatar: 'https://via.placeholder.com/36',
          title: '챌린지 참여했어요',
          content: '"30일 러닝 습관 만들기" 챌린지에 참여했습니다. 함께해요!',
          likes: 22,
          comments: 5,
          date: '3시간 전'
        }
      ]
    }
  },
  computed: {
    currentFeed() {
      return this.isPopularFeed ? this.popularPosts : this.followPosts;
    }
  },
  methods: {
    toggleFeedType() {
      this.isPopularFeed = !this.isPopularFeed;
    },
    startRunning() {
      // 달리기 페이지로 이동
      this.$router.push('/run');
    }
  }
}
</script>

<style scoped>
.home-container {
  padding: 20px 16px;
  max-width: 800px;
  margin: 0 auto;
}

/* 사용자 환영 카드 */
.welcome-card {
  background: linear-gradient(135deg, #7DC383, #549F69);
  border-radius: 16px;
  padding: 24px;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.welcome-info h1 {
  font-size: 24px;
  margin-bottom: 8px;
  font-weight: 700;
}

.welcome-info p {
  font-size: 16px;
  margin-bottom: 16px;
  opacity: 0.9;
}

.username {
  font-weight: 700;
}

.highlight {
  font-weight: 700;
  color: #ffffff;
}

.start-run-btn {
  background-color: white;
  color: #3A7D4B;
  border: none;
  border-radius: 50px;
  padding: 10px 20px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.start-run-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  background-color: #f0f0f0;
}

.welcome-stats {
  display: flex;
  margin-top: 20px;
  gap: 12px;
}

.stat-card {
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 12px;
  text-align: center;
  flex: 1;
}

.stat-card.highlight {
  background-color: rgba(255, 255, 255, 0.3);
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  display: block;
}

.stat-unit {
  font-size: 14px;
  opacity: 0.9;
}

.stat-label {
  font-size: 12px;
  opacity: 0.8;
  display: block;
  margin-top: 4px;
}

/* 콩 성장 박스 */
.bean-growth-box {
  background-color: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border-left: 4px solid #7DC383;
}

.bean-growth-box h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #3A7D4B;
}

.progress-container {
  position: relative;
  height: 24px;
  background-color: #E8F5E9;
  border-radius: 12px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #7DC383, #549F69);
  border-radius: 12px;
  position: relative;
  transition: width 0.5s ease;
}

.bean-icon {
  position: absolute;
  right: 5px;
  top: 50%;
  transform: translateY(-50%);
  color: white;
  font-size: 16px;
}

.progress-text {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #333;
  font-weight: 600;
  font-size: 14px;
}

/* 메인 메뉴 */
.main-menu {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.menu-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: white;
  min-width: 80px;
  padding: 16px 12px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border-bottom: 3px solid transparent;
}

.menu-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-bottom: 3px solid #7DC383;
}

.menu-card i {
  font-size: 24px;
  margin-bottom: 8px;
  color: #7DC383;
}

.menu-card span {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

/* 인기 게시글 섹션 */
.section {
  background-color: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  border-bottom: 1px solid #E8F5E9;
  padding-bottom: 10px;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #3A7D4B;
}

.toggle-feed {
  font-size: 14px;
  color: #7DC383;
  font-weight: 500;
  background: none;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.toggle-feed:hover {
  color: #3A7D4B;
  text-decoration: underline;
}

.post-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-item {
  padding-bottom: 16px;
  border-bottom: 1px solid #E8F5E9;
}

.post-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.post-user {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 12px;
  object-fit: cover;
  border: 2px solid #E8F5E9;
}

.post-user .username {
  font-weight: 500;
  color: #3A7D4B;
}

.post-content h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #333;
}

.post-content p {
  font-size: 14px;
  color: #555;
  margin-bottom: 12px;
  line-height: 1.5;
}

.post-meta {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #777;
}

.post-meta i {
  margin-right: 4px;
  color: #7DC383;
}

/* 반응형 스타일 */
@media (min-width: 768px) {
  .welcome-card {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
  
  .welcome-info {
    flex: 1;
  }
  
  .welcome-stats {
    margin-top: 0;
    min-width: 300px;
  }
  
  .main-menu {
    justify-content: space-between;
  }
  
  .menu-card {
    flex: 1;
  }
  
  .post-container {
    flex-direction: row;
  }
  
  .post-item {
    flex: 1;
    border-bottom: none;
    border-right: 1px solid #E8F5E9;
    padding-right: 16px;
    padding-bottom: 0;
  }
  
  .post-item:last-child {
    border-right: none;
    padding-right: 0;
  }
}
</style>