<template>
  <div class="home-container">

    <!-- 상단 헤더 영역 -->
    <div class="welcome-header">
      <h1 class="greeting">안녕하세요,</h1>
      <h1 class="username">{{ userName }}님!</h1>
    </div>

    <!-- 오늘의 추천 러닝 카드 -->
    <div class="today-card">
      <div class="card-icon">
        <i class="ri-run-line"></i>
      </div>
      <div class="card-content">
        <h3 class="card-title">오늘의 추천 러닝</h3>
        <p class="recommend-text">{{ recommendation }}km 달려보세요!</p>
      </div>
      <button class="start-run-btn" @click="startRunning">GO!</button>
    </div>

    <!-- 러닝 통계 -->
    <div class="stats-section">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="icon">{{ stat.icon }}</div>
        <div class="value">{{ stat.value }}</div>
        <div class="label">{{ stat.label }}</div>
      </div>
    </div>

    <!-- 콩 성장률 -->
    <div class="bean-section">
      <h3>🌱 콩이 자라고 있어요!</h3>
      <p>오늘의 미션: <strong>{{ recommendation }}km 달리기</strong></p>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: growthRate + '%' }"></div>
      </div>
      <p class="progress-percent">{{ growthRate }}%</p>
    </div>

    <!-- 메인 메뉴 -->
    <div class="main-menu">
      <router-link v-for="menu in menus" :key="menu.label" :to="menu.path" class="menu-item">
        <div class="menu-icon">{{ menu.icon }}</div>
        <div class="menu-label">{{ menu.label }}</div>
      </router-link>
    </div>

    <!-- 인기 게시글 -->
    <div class="section">
      <div class="section-header">
        <h3>🔥 게시판</h3>
        <button class="toggle-btn" @click="toggleFeedType">
          {{ isPopularFeed ? '팔로우 게시글 보기' : '인기 게시글 보기' }}
        </button>
      </div>
      <div class="post-list">
        <div class="post-card" v-for="post in currentFeed" :key="post.id">
          <div class="post-header">
            <img :src="post.authorAvatar" class="avatar" />
            <span class="author">{{ post.author }}</span>
          </div>
          <h4 class="title">{{ post.title }}</h4>
          <p class="content">{{ post.content }}</p>
          <div class="post-meta">
            <span>❤️ {{ post.likes }}</span>
            <span>💬 {{ post.comments }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const token = ref('');
const userId = ref(null);
const userName = ref('');
const recommendation = ref(5);
const growthRate = ref(100);
const stats = ref([
  { label: '총 거리', value: '45.8km', icon: '🏁' },
  { label: '총 횟수', value: '12', icon: '👟' },
  { label: '챌린지 보상', value: '3', icon: '🎁' }
]);
const menus = ref([
  { label: '랭킹', icon: '👟', path: '/run/rank' },
  { label: '캘린더', icon: '📝', path: '/calendar' },
  { label: '루트 찾기', icon: '👥', path: '/routes' },
  { label: '게시판', icon: '🖐️', path: '/board' }
]);
const isPopularFeed = ref(true);
const popularPosts = ref([
  {
    id: 1,
    author: '러닝마스터',
    authorAvatar: 'https://placehold.co/600x400',
    title: '초보 러너를 위한 효과적인 훈련법',
    content: '처음 러닝을 시작하시는 분들을 위한 팁을 공유합니다...',
    likes: 42,
    comments: 12
  },
  {
    id: 2,
    author: '마라톤조아',
    authorAvatar: 'https://placehold.co/600x400',
    title: '서울 근교 러닝 코스 추천',
    content: '주말에 러닝하기 좋은 서울 근교 코스를 소개합니다...',
    likes: 35,
    comments: 8
  }
]);
const followPosts = ref([
  {
    id: 3,
    author: '달려라하니',
    authorAvatar: 'https://placehold.co/600x400',
    title: '오늘의 러닝 완료!',
    content: '오늘도 5km 러닝 완료했습니다. 날씨가 정말 좋았어요!',
    likes: 15,
    comments: 3,
    date: '1시간 전'
  },
  {
    id: 4,
    author: '조깅왕',
    authorAvatar: 'https://placehold.co/600x400',
    title: '챌린지 참여했어요',
    content: '"30일 러닝 습관 만들기" 챌린지에 참여했습니다. 함께해요!',
    likes: 22,
    comments: 5,
    date: '3시간 전'
  }
]);

// computed 속성
const currentFeed = computed(() => {
  return isPopularFeed.value ? popularPosts.value : followPosts.value;
});

// 메서드
function toggleFeedType() {
  isPopularFeed.value = !isPopularFeed.value;
}

function startRunning() {
  router.push('/run');
}

/* user 불러오기 */
const getCurrentUser = async () => {
  const currentToken = localStorage.getItem('jwt');

  try {
    const res = await fetch(`http://localhost:8080/api/users/me`, {
      headers: {
        Authorization: `Bearer ${currentToken}`
      }
    });

    const data = await res.json();
    const user = data.user;

    token.value = currentToken;
    userId.value = user.id;
    userName.value = user.nickname;

    localStorage.setItem('userId', user.id);
    localStorage.setItem('nickname', user.nickname);

  } catch (err) {
    console.error('사용자 정보 요청 실패:', err);
    alert('로그인이 필요합니다.');
  }
};

/* 오늘 뛴 시간 */
const formatDuration = (min) => {
  if (!min) return "0분";
  const hr = Math.floor(min / 60);
  const m = min % 60;
  return `${hr}시간 ${m.toFixed(0)}분`;
};


onMounted(async () => {
  await getCurrentUser();
});
</script>

<style scoped>
.home-container {
  padding: 24px;
  background-color: #FFF8F2;
  font-family: 'Noto Sans KR', sans-serif;
}

/* 상단 환영 헤더 */
.welcome-header {
  margin-bottom: 24px;
}

.greeting {
  font-size: 20px;
  font-weight: 400;
  color: #666;
  margin: 0;
}

.username {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 4px 0 0;
}

.logo {
  height: 48px;
  margin-bottom: 16px;
}

.intro-section {
  text-align: center;
  margin-bottom: 32px;
}

.intro-section h2 {
  font-size: 20px;
  margin-bottom: 12px;
}

.cta-button {
  background-color: #FF7043;
  color: white;
  border: none;
  border-radius: 999px;
  padding: 12px 24px;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
}

.stats-section {
  display: flex;
  justify-content: space-between;
  margin: 24px 0;
  gap: 12px;
}

.stat-card {
  background: #FFF3ED;
  border-radius: 12px;
  flex: 1;
  text-align: center;
  padding: 12px;
}

.stat-card .icon {
  font-size: 24px;
}

.stat-card .value {
  font-weight: bold;
  font-size: 18px;
  margin: 6px 0;
}

.stat-card .label {
  font-size: 14px;
  color: #555;
}

.bean-section {
  background: #FFF3ED;
  padding: 16px;
  border-radius: 12px;
  text-align: center;
  margin-bottom: 24px;
}

.progress-bar {
  background: #FFE5D5;
  height: 16px;
  border-radius: 999px;
  overflow: hidden;
  margin-top: 8px;

}

.progress-fill {
  height: 100%;
  background: #FF7043;
  transition: width 0.3s ease;
}

.progress-percent {
  font-size: 13px;
  color: #333;
  margin-top: 6px;
}

.main-menu {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}

.menu-item {
  width: 72px;
  height: 88px;
  background: white;
  border-radius: 16px;
  text-align: center;
  font-weight: 500;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
  padding: 10px 4px;
  font-size: 14px;
  color: #333;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-decoration: none;
}

.menu-icon {
  font-size: 22px;
  margin-bottom: 8px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-label {
  font-size: 14px;
  text-align: center;
  line-height: 1.2;
}

.section {
  background: white;
  padding: 16px;
  border-radius: 12px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-header h3 {
  font-size: 16px;
  font-weight: 600;
}

.toggle-btn {
  border: none;
  background: none;
  color: #FF7043;
  font-size: 13px;
  cursor: pointer;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-card {
  padding: 12px;
  background: #FFF8F2;
  border-radius: 10px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
}

.author {
  font-weight: 500;
  color: #333;
}

.title {
  font-size: 15px;
  font-weight: bold;
  margin: 4px 0;
}

.content {
  font-size: 14px;
  color: #666;
  margin-bottom: 6px;
}

.post-meta {
  font-size: 13px;
  color: #888;
  display: flex;
  gap: 12px;
}

.today-card {
  background-color: #FF7E47;
  border-radius: 20px;
  padding: 20px;
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  box-shadow: 0 4px 15px rgba(255, 126, 71, 0.3);
  color: white;
  position: relative;
  overflow: hidden;
}

.card-icon {
  width: 48px;
  height: 48px;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
}

.card-content {
  flex-grow: 1;
}

.card-title {
  font-size: 18px;
  margin: 0 0 4px;
  font-weight: 500;
}

.recommend-text {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.start-run-btn {
  background-color: white;
  color: #FF7E47;
  border: none;
  border-radius: 50px;
  padding: 10px 20px;
  font-weight: 700;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.start-run-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
}

/* 반응형 스타일 */
@media (max-width: 600px) {
  .main-menu {
    justify-content: space-between;
  }

  .menu-item {
    width: calc(25% - 10px);
    height: 80px;
    padding: 8px 4px;
  }

  .menu-icon {
    font-size: 20px;
    margin-bottom: 6px;
    /* 고정된 높이 유지 */
    height: 24px;
  }

  .menu-label {
    font-size: 12px;
    /* 고정된 높이 유지 */
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .stat-card .icon {
    font-size: 20px;
  }

  .stat-bubble {
    padding: 12px 8px;
  }

  .stat-value {
    font-size: 18px;
  }

  .stat-card .value {
    font-size: 16px;
  }

  .stat-card .label {
    font-size: 12px;
  }

  .recommend-text {
    font-size: 20px;
  }
}

/* 애니메이션 효과 */
@keyframes pulse {
  0% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.05);
  }

  100% {
    transform: scale(1);
  }
}

.today-card {
  animation: pulse 2s infinite;
}
</style>
