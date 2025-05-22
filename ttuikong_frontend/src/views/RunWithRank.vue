// views/RunWithRank.vue
<template>
  <link href="https://cdn.jsdelivr.net/npm/remixicon@2.5.0/fonts/remixicon.css" rel="stylesheet" />

  <div class="container">
    <h3>🔥 러닝 랭킹</h3>

    <div class="rank-container">
      <div v-for="(user, index) in rankings" :key="user.id" class="rank-item">
        <div class="rank">{{ index + 1 }}</div>
        <div class="user-info">
          <strong>{{ user.nickname }}</strong>
          <span>{{ formatDuration(user.duration) }}</span>
        </div>
        <div class="medal" v-if="index < 3">
          <i :class="['ri-medal-line', getMedalClass(index)]"></i>
        </div>
      </div>
    </div>

    <div class="user-rank">
      <h4>나의 순위</h4>
      <div class="rank-item highlight">
        <div class="rank">{{ myRanking }}</div>
        <div class="user-info">
          <strong>{{ userName }}</strong>
          <span>{{ myDistance }}분</span>
        </div>
      </div>
    </div>
    <br>
    <br>

    <!-- 네비게이션 메뉴 -->
    <div class="run-nav">
      <button class="nav-btn" @click="navigateToTimer">개인 러닝</button>
      <button class="nav-btn active">랭킹</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
const emit = defineEmits(['navigate']);

const BASE_URL = 'http://localhost:8080/api/runs';

const token = ref('');
const userId = ref(null);
const userName = ref('');

const rankings = ref([]);
const myRanking = ref('-');
const myDistance = ref(0);

const formatDuration = (min) => {
  if (!min) return "0분";
  const hr = Math.floor(min / 60);
  const m = min % 60;
  return `${hr}시간 ${m.toFixed(0)}분`;
};

const getMedalClass = (index) => {
  if (index === 0) return 'gold';
  if (index === 1) return 'silver';
  if (index === 2) return 'bronze';
  return '';
};

const navigateToTimer = () => {
  emit('navigate', 'RunTimer');
};

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

const getTop10UsersByDuration = async () => {
  const jwt = localStorage.getItem('jwt');

  const res = await fetch(`${BASE_URL}/rank`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${jwt}`,
      'Content-Type': 'application/json'
    }
  });

  const raw = await res.json();

  return Array.isArray(raw) ? raw : raw.data || [];
};

const getMyRanking = async () => {
  const jwt = localStorage.getItem('jwt');
  const userId = localStorage.getItem('userId');

  try {
    const res = await fetch(`${BASE_URL}/rank/me?userId=${userId}`, {
      headers: {
        'Authorization': `Bearer ${jwt}`,
        'Content-Type': 'application/json'
      }
    });

    const contentLength = res.headers.get("content-length");
    if (!contentLength || parseInt(contentLength) === 0) {
      console.warn("📭 응답 본문 없음 (content-length: 0)");
      return;
    }

    const data = await res.json();

    myRanking.value = data.ranking;
    myDistance.value = data.total_duration;
  } catch (err) {
    console.error('내 랭킹 조회 실패:', err);
    myRanking.value = '-';
    myDistance.value = 0;
  }
};


onMounted(async () => {
  await getCurrentUser();
  await getMyRanking();

  try {
    const data = await getTop10UsersByDuration();
    rankings.value = data;

    const me = data.find(user => user.nickname === userName.value);
  } catch (error) {
    console.error('랭킹 데이터 불러오기 실패:', error);
  }
});
</script>


<style scoped>
.container {
  background-color: #ffe3d6;
}

h3 {
  padding: 10px;
}

h3,
h4 {
  font-size: 25px;
  margin: 10px 0 10px 15px;
  font-weight: 600;
  color: #333;
}

h4 {
  font-size: 20px;
}

.rank-container {
  padding: 0 15px;
  margin-bottom: 30px;
}

.rank-item {
  background: white;
  padding: 15px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  border: 1px solid #ffe3d6;
}

.rank-item.highlight {
  background: linear-gradient(to right, #ffe3d6, white);
  border-left: 4px solid #FF7E47;
}

.rank {
  font-size: 24px;
  font-weight: 700;
  margin-right: 15px;
  min-width: 30px;
  text-align: center;
  color: #555;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.user-info strong {
  font-size: 16px;
  color: #333;
}

.user-info span {
  font-size: 14px;
  color: #777;
  margin-top: 4px;
}

.medal {
  font-size: 22px;
  margin-left: 10px;
}

.gold {
  color: gold;
}

.silver {
  color: silver;
}

.bronze {
  color: #cd7f32;
}

.user-rank {
  background: white;
  padding: 15px;
  border-radius: 16px;
  margin: 0 15px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.user-rank h4 {
  margin: 0 0 10px 0;
  color: #FF7E47;
}

.back-button {
  margin: 15px;
  font-weight: 600;
  background: #FF7E47;
  color: white;
  padding: 10px 20px;
  border-radius: 20px;
  border: none;
  font-size: 16px;
  cursor: pointer;
}

/* 추가된 네비게이션 스타일 */
.run-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  display: flex;
  padding: 15px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
}

.nav-btn {
  flex: 1;
  padding: 12px;
  margin: 0 5px;
  border: none;
  border-radius: 10px;
  background: #f0f0f0;
  font-weight: 600;
  font-size: 14px;
  color: #555;
  cursor: pointer;
  transition: all 0.3s ease;
}

.nav-btn.active {
  background: #FF7E47;
  color: white;
}

.nav-btn:hover {
  background: #e0e0e0;
}

.nav-btn.active:hover {
  background: #FF7E47;
}
</style>