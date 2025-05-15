// views/RunWithRank.vue
<template>
  <div>
    <h3>러닝 랭킹</h3>

    <div class="rank-container">
      <div v-for="(user, index) in rankings" :key="user.id" class="rank-item">
        <div class="rank">{{ index + 1 }}</div>
        <div class="user-info">
          <strong>{{ user.name }}</strong>
          <span>{{ user.distance }}km</span>
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
          <span>{{ myDistance }}km</span>
        </div>
      </div>
    </div>

    <button class="back-button" @click="goBack">돌아가기</button>
    
    <!-- 추가된 네비게이션 메뉴 -->
    <div class="run-nav">
      <button class="nav-btn" @click="navigateToTimer">개인 러닝</button>
      <button class="nav-btn" @click="navigateToCrew">크루 러닝</button>
      <button class="nav-btn active">랭킹</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RunWithRank',
  data() {
    return {
      userName: '김러너',
      myDistance: 45.8,
      myRanking: 8,
      rankings: [
        { id: 1, name: '마라토너', distance: 120.5 },
        { id: 2, name: '달리기왕', distance: 98.3 },
        { id: 3, name: '러닝퀸', distance: 87.2 },
        { id: 4, name: '김달림', distance: 76.1 },
        { id: 5, name: '박조깅', distance: 68.5 },
        { id: 6, name: '정스프린트', distance: 58.9 },
        { id: 7, name: '최건강', distance: 52.6 },
        { id: 8, name: '김러너', distance: 45.8 },
        { id: 9, name: '이운동', distance: 41.2 },
        { id: 10, name: '장마라톤', distance: 38.7 }
      ]
    }
  },
  methods: {
    getMedalClass(index) {
      if (index === 0) return 'gold';
      if (index === 1) return 'silver';
      if (index === 2) return 'bronze';
      return '';
    },
    goBack() {
      this.$router.push('/');
    },
    // 추가된 네비게이션 메소드
    navigateToTimer() {
      this.$root.$emit('navigate', 'RunTimer');
    },
    navigateToCrew() {
      this.$root.$emit('navigate', 'RunWithCrew');
    }
  }
}
</script>

<style scoped>
h3, h4 {
  font-size: 25px;
  margin: 15px 0 10px 15px;
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
}

.rank-item.highlight {
  background: linear-gradient(to right, #E8F5E9, white);
  border-left: 4px solid #7DC383;
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
  color: #3A7D4B;
}

.back-button {
  margin: 15px;
  font-weight: 600;
  background: #4CAF50;
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
  background: #4CAF50;
  color: white;
}

.nav-btn:hover {
  background: #e0e0e0;
}

.nav-btn.active:hover {
  background: #43A047;
}
</style>