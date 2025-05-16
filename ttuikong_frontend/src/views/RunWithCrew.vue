// views/RunWithCrew.vue
<template>
  <div>
    <h3 id="crewName">{{ crewName }}</h3>

    <p>이곳은 선택한 크루의 상세 페이지입니다.</p>
    <div class="user-list" id="crewMemberList">
      <div v-for="member in crewMembers" :key="member.id" class="user-card">
        <strong>{{ member.name }}</strong>
        <span>{{ member.status }}</span>
      </div>
    </div>

    <h3>실시간 메신저</h3>
    <button class="talk-button" id="goToChat" @click="goToChat">▶ Talk</button>

    <br><br><br>
    <button id="cancelBtn" @click="goBack">돌아가기</button>
    
    <!-- 추가된 네비게이션 메뉴 -->
    <div class="run-nav">
      <button class="nav-btn" @click="navigateToTimer">개인 러닝</button>
      <button class="nav-btn active">크루 러닝</button>
      <button class="nav-btn" @click="navigateToRank">랭킹</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RunWithCrew',
  emits: ['navigate'],
  data() {
    return {
      crewName: '서울 8반 러닝 챌린지',
      crewMembers: [
        { id: 1, name: '김민준', status: '달리는 중' },
        { id: 2, name: '이지우', status: '대기 중' },
        { id: 3, name: '박서연', status: '오프라인' },
        { id: 4, name: '최준호', status: '대기 중' },
        { id: 5, name: '정수빈', status: '오프라인' }
      ]
    }
  },
  methods: {
    goToChat() {
      // 채팅 페이지로 이동
      this.$router.push('/chat');
    },
    goBack() {
      // 이전 페이지로 돌아가기
      this.$router.go(-1);
    },
    // 추가된 네비게이션 메소드
    navigateToTimer() {
      this.$emit('navigate', 'RunTimer');
    },
    navigateToRank() {
      this.$emit('navigate', 'RunWithRank');
    }
  }
}
</script>

<style scoped>
body {
  font-family: sans-serif;
  background-color: #f0f9f0;
  margin: 0;
  padding: 20px;
}

h3, p {
  font-size: 25px;
  margin: 15px 0 10px 15px;
  font-weight: 600;
  color: #333;
}

p {
  font-size: 16px;
  font-weight: normal;
  color: #555;
}

.user-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 0 15px;
  margin-bottom: 20px;
}

.user-card {
  background: white;
  padding: 15px 10px;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  text-align: center;
}

.user-card strong {
  margin-bottom: 5px;
  color: #333;
}

.user-card span {
  font-size: 14px;
  color: #777;
}

.progress {
  width: 97%;
  margin: 10px 15px;
  background: #ccc;
  border-radius: 20px;
  margin-top: 10px;
}

.progress > div {
  background: #69c14c;
  height: 15px;
  border-radius: 20px;
}

.talk-button {
  margin: 0px 0 10px 15px;
  font-weight: 600;
  background: #4CAF50;
  color: white;
  padding: 10px 20px;
  border-radius: 20px;
  border: none;
  font-size: 16px;
  cursor: pointer;
}

#cancelBtn {
  margin: 0px 0 10px 15px;
  font-weight: 600;
  background: #39693c;
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