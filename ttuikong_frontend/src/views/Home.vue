<template>
  <div class="home-container">

    <!-- 상단 헤더 영역 -->
    <div class="welcome-header">
      <h1 class="greeting">안녕하세요,</h1>
      <h1 class="username">{{ userName }}님!</h1>
    </div>

    <div class="today-card">
      <div class="card-content">
        <h3 class="card-title">오늘의 러닝 추천</h3>
        <div v-if="isLoadingRecommendation" class="loading-state">
          <p class="recommend-text">AI가 분석 중...</p>
        </div>
        <div v-else-if="recommendationData" class="recommendation-info">
          <p class="recommend-text">{{ recommendationData.recommendedDistance }}km 달려보세요!</p>
          <div class="sub-info">
            <span>{{ formattedTime }}</span>
            <span>{{ recommendationData.estimatedCalories }}kcal</span>
          </div>
        </div>
        <div v-else class="fallback-state">
          <p class="recommend-text">{{ recommendation }}km 달려보세요!</p>
        </div>
      </div>
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
      <h3>        
        <span v-if="growthRate >= 100">🌳</span>
        <span v-else-if="growthRate >= 60">🌿</span>
        <span v-else-if="growthRate >= 30">🌱</span>
        <span v-else>🫘</span>
        콩이 자라고 있어요!
      </h3>
      <!-- <p>오늘의 미션: <strong>{{ recommendation }}km 달리기</strong></p> -->
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: growthRate + '%' }"></div>
      </div>
      <p class="progress-percent">{{ growthRate.toFixed(1) }}%</p>
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
const isLoadingRecommendation = ref(false);
const recommendation = ref(5);
const recommendationData = ref(null);
const formattedTime = ref(null);
const beanCount = ref(null);
const growthRate = ref(0);
const activityLevel = ref('');
const runningData = ref([]);

const stats = computed(() => {
  const totalDistance = runningData.value.reduce((sum, run) => sum + (run.distance || 0), 0);
  const totalDuration = runningData.value.reduce((sum, run) => sum + (run.duration || 0), 0);
  const runCount = runningData.value.length;

  return [
    {
      label: '총 거리',
      value: `${totalDistance.toFixed(1)} km`,
      icon: '📏'
    },
    {
      label: '총 시간',
      value: formatDuration(totalDuration),
      icon: '⏱️'
    },
    {
      label: '러닝 횟수',
      value: `${runCount}회`,
      icon: '🏃‍♂️'
    }
  ];
});
const menus = ref([
  { label: '챌린지', icon: '👟', path: '/run' },
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

function formatTime(minutes) {
  if (minutes >= 60) {
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;

    if (remainingMinutes === 0) {
      return `${hours}시간`;
    } else {
      return `${hours}시간 ${remainingMinutes}분`;
    }
  } else {
    return `${minutes}분`;
  }
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

    localStorage.setItem('nickname', user.nickname);

  } catch (err) {
    console.error('사용자 정보 요청 실패:', err);
    alert('로그인이 필요합니다.');
    router.push('login');
  }
};

const getAIRecommendation = async () => {
  if (!token.value) return;

  try {
    isLoadingRecommendation.value = true;
    const response = await fetch('http://localhost:8080/api/my/running/recommendation', {
      headers: {
        'Authorization': `Bearer ${token.value}`
      }
    });

    if (response.ok) {
      const data = await response.json();
      recommendationData.value = data;
      formattedTime.value = formatTime(recommendationData.value.estimatedTime);
    }
  } catch (error) {
    console.error('AI 추천 로드 실패:', error);
    // 실패시 기본값 유지
  } finally {
    isLoadingRecommendation.value = false;
  }
};
/* 오늘 뛴 시간 */
const formatDuration = (seconds) => {
  if (!seconds || seconds <= 0) return "0초";

  const totalMinutes = Math.floor(seconds / 60);
  const sec = Math.floor(seconds % 60);
  const days = Math.floor(totalMinutes / 1440);
  const hours = Math.floor((totalMinutes % 1440) / 60);
  const minutes = totalMinutes % 60;

  if (days > 0) {
    return `${days}일 ${hours}시간 ${minutes}분 ${sec}초`;
  } else if (hours > 0) {
    return `${hours}시간 ${minutes}분 ${sec}초`;
  } else if (minutes > 0) {
    return `${minutes}분 ${sec}초`;
  } else {
    return `${sec}초`;
  }
};

/* 유저의 하루 러닝 기록 */
const getDayRoutes = async () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth() + 1;

  try {
    const res = await fetch(
      `http://localhost:8080/api/users/${userId.value}/records?year=${year}&month=${month}`,
      {
        headers: {
          Authorization: `Bearer ${token.value}`
        }
      }
    );

    const data = await res.json();
    runningData.value = data;
    console.log(runningData.value)

  } catch (err) {
    console.error("하루 러닝 정보 조회 실패:", err);
  }
};

/* 콩 */
const fetchBeanStatus = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/bean/${userId.value}`, {
      headers: {
        Authorization: `Bearer ${token.value}`
      }
    })

    if (!res.ok) throw new Error('콩 상태 조회 실패')

    const data = await res.json()
    return data

  } catch (err) {
    console.error('콩 상태 조회 실패:', err)
    return null
  }
};

const loadBeanStatus = async () => {
  const data = await fetchBeanStatus()
  if (data) {
    beanCount.value = data.beanCount
    growthRate.value = Math.min((data.beanCount / 100) * 100, 100)
    activityLevel.value = data.activityLevel
  }
}

onMounted(async () => {
  await getCurrentUser();
  await getAIRecommendation();
  await getDayRoutes();
  await loadBeanStatus();
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
  position: relative;
}

.progress-fill {
  position: absolute;
  top: 0;
  left: 0;
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
  background: #FF7E47;
  border-radius: 16px;
  padding: 24px;
  margin: 16px 0;
  box-shadow: 0 4px 16px rgba(255, 126, 71, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
}

.card-content {
  text-align: center;
  width: 100%;
  text-align: center;
  width: 100%;
}

.card-title {
  color: white;
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px 0;
}

.loading-state,
.recommendation-info,
.fallback-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
  width: 100%;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  backdrop-filter: blur(10px);
}

@keyframes loading {

  0%,
  100% {
    opacity: 0.7;
  }

  50% {
    opacity: 1;
  }
}

.recommend-text {
  color: white;
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.loading-state .recommend-text {
  opacity: 0.9;
  animation: loading 1.5s ease-in-out infinite;
}

.sub-info {
  display: flex;
  gap: 24px;
  margin-top: 8px;
}

.sub-info span {
  color: rgba(255, 255, 255, 0.95);
  font-size: 14px;
  font-weight: 500;
  text-align: center;
}

.sub-info span:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.sub-info span::before {
  font-size: 1.2rem;
  margin-bottom: 4px;
  display: block;
}

.sub-info span:first-child::before {
  content: '⏱️예상 소요 시간';
  font-size: 0.75rem;
  opacity: 0.8;
  margin-top: 2px;
  font-weight: 400;
}

.sub-info span:last-child::before {
  content: '🔥 예상 소모 칼로리';
  font-size: 0.75rem;
  opacity: 0.8;
  margin-top: 2px;
  font-weight: 400;
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



@media (max-width: 480px) {
  .today-card {
    padding: 16px;
    margin: 12px 0;
    gap: 12px;
    min-height: 70px;
  }

  .card-title {
    font-size: 14px;
    margin-bottom: 6px;
  }

  .recommend-text {
    font-size: 18px;
  }

  .start-run-btn {
    padding: 10px 16px;
    font-size: 14px;
    min-width: 50px;
  }

  .sub-info span {
    font-size: 11px;
    padding: 2px 6px;
  }
}

@media (max-width: 360px) {
  .today-card {
    padding: 14px;
    gap: 10px;
  }

  .card-title {
    font-size: 13px;
  }

  .recommend-text {
    font-size: 16px;
  }

  .start-run-btn {
    padding: 8px 14px;
    font-size: 13px;
  }

  .sub-info {
    gap: 6px;
  }
}

/* 콩 */
.bean-section {
  margin: 16px 0;
  padding: 12px;
  background-color: #f3faff;
  border-radius: 12px;
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.05);
}

.progress-bar {
  width: 100%;
  height: 20px;
  background-color: #eee;
  border-radius: 10px;
  overflow: hidden;
  margin: 8px 0;
}

.progress-fill {
  height: 100%;
  background-color: #81c784;
  transition: width 0.4s ease-in-out;
}

.progress-percent {
  text-align: right;
  font-weight: bold;
  color: #388e3c;
}

</style>
