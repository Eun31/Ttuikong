// views/RunTimer.vue
<template>
  <div>
    <!-- 지도 위치용 hidden map -->
    <div id="map"></div>
    <!-- <div class="section">
      <div id="info">{{ infoText }}</div>
    </div> -->

<div class="timer-card">
  <h2>현재 러닝 시간</h2>
  <div class="time">{{ formattedTime }}</div>

  <div class="play-area">
  <div class="dog-zone">
    <img
      class="dog-image"
      :src="isRunning ? dogRun : dogSit"
      alt="강아지 상태"
    />

    <transition name="fade">
      <div v-if="isRunning" class="paw-prints">
        <img src="@/assets/paw.png" class="paw one" />
        <img src="@/assets/paw.png" class="paw two" />
        <img src="@/assets/paw.png" class="paw three" />
        <img src="@/assets/paw.png" class="paw four" />
      </div>
    </transition>
  </div>
  <button class="play-button" @click="toggleTimer">
    {{ isRunning ? '■' : '▶' }}
  </button>
</div>

</div>

    
    <div class="crew-list-section">
      <div class="crew-top">
        <h3>크루 목록</h3>
        <button class="create-crew-btn" @click="createCrew">+ 크루 생성</button>
      </div>
        <!--크루 검색-->
        <div
          v-for="crew in filteredCrews"
          :key="'search-' + crew.id"
          class="crew-card search-result"
        >
          <div class="crew-header">
            <h4>{{ crew.name }}</h4>
            <button class="join-btn" @click.stop="joinCrew(crew)">가입하기</button>
          </div>
          <p class="crew-meta">참여 인원: {{ crew.members.length }}명 · 목표: {{ crew.mission }}</p>
        </div>
        <div class="group-search">
          <input type="text" v-model="searchQuery" placeholder="크루 이름으로 검색..." class="search-input" />
          <button>검색</button>
        </div>

    <!-- 내 크루 목록 -->
      <h3>내가 속한 크루</h3>
      <div v-for="crew in crews" :key="crew.id" class="crew-card" @click="toggleCrew(crew.id)">
        <div class="crew-header">
          <h4>{{ crew.name }}</h4>
          <span>{{ crew.members.length }}명</span>
        </div>
        <transition name="fade">
          <div v-show="expandedCrews.includes(crew.id)" class="crew-detail">
            <p>📍 목표: <strong>{{ crew.mission }}</strong></p>
            <p>🏅 목표 달성률: {{ crew.participationRate }}%</p>
            <h3 class="sub-title">크루 멤버</h3>
            <div class="user-list">
              <div v-for="member in crewMembers" :key="member.id" class="user-card">
                <strong>{{ member.name }}</strong>
                <span>{{ member.status }}</span>
              </div>
            </div>
            <h3 class="sub-title">실시간 메신저</h3>
            <button class="talk-button" @click="goToChat">▶ Talk</button>
          </div>
        </transition>
      </div>
    </div>

    <!-- 거리 및 정보 -->
    <div class="section">
      <div id="info">{{ infoText }}</div>
    </div>
    <!-- 추가된 네비게이션 메뉴 -->
    <div class="run-nav">
      <button class="nav-btn active" @click="stayOnTimer">개인 러닝</button>
      <button class="nav-btn" @click="navigateToRank">랭킹</button>
    </div>
  </div>
</template>

<script>
import dogRun from '@/assets/dog_run.gif'
import dogSit from '@/assets/dog_sit.gif'
export default {
  name: 'RunTimer',
  emits: ['navigate'],
  data() {
    return {
      dogSit,
      dogRun,
      seconds: 0,
      timer: null,
      isRunning: false,
      map: null,
      infoText: '러닝을 시작하려면 ▶를 누르세요',
      positions: [],
      distance: 0,
      crews: [
        {
          id: 1,
          name: '테일즈러너즈',
          mission: '5km 이상 달리기',
          avgDistance: 4.7,
          participationRate: 80,
          members: ['A', 'B', 'C']
        },
        {
          id: 2,
          name: '런닝걸스',
          mission: '30분 연속 달리기',
          avgDistance: 5.4,
          participationRate: 60,
          members: ['D', 'E']
        },
        {
          id: 3,
          name: '아침햇살조',
          mission: '아침 7시 달리기',
          participationRate: 40,
          members: ['F']
        }
      ],
      crewMembers: [
        { id: 1, name: '김민준', status: '달리는 중' },
        { id: 2, name: '이지우', status: '대기 중' },
        { id: 3, name: '박서연', status: '오프라인' },
        { id: 4, name: '최준호', status: '대기 중' },
        { id: 5, name: '정수빈', status: '오프라인' }
      ],
      expandedCrews: []
    
    }
  },

  computed: {
    formattedTime() {
      const minutes = Math.floor(this.seconds / 60);
      const remainingSeconds = this.seconds % 60;
      return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
    },
    filteredCrews() {
      const query = (this.searchQuery || '').trim().toLowerCase();
      if (!query) return this.crews;
      return this.crews.filter(crew =>
        crew.name.toLowerCase().includes(query)
      );
    }

  },
  mounted() {
    this.initMap();
  },
  beforeUnmount() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  },
  methods: {
    toggleTimer() {
      if (this.isRunning) {
        // 타이머 정지
        clearInterval(this.timer);
        this.saveRunningData();
      } else {
        // 타이머 시작
        this.timer = setInterval(() => {
          this.seconds++;
          this.updateLocation();
        }, 1000);
        this.infoText = '달리는 중...';
      }
      this.isRunning = !this.isRunning;
    },
    toggleCrew(id) {
      if (this.expandedCrews.includes(id)) {
        this.expandedCrews = this.expandedCrews.filter(cid => cid !== id);
      } else {
        this.expandedCrews.push(id);
      }
    },
    initMap() {
      if (window.kakao && window.kakao.maps) {
        const mapContainer = document.getElementById('map');
        const mapOption = {
          center: new kakao.maps.LatLng(37.566826, 126.9786567),
          level: 3
        };
        this.map = new kakao.maps.Map(mapContainer, mapOption);
        
        // 현재 위치 표시
        if (navigator.geolocation) {
          navigator.geolocation.getCurrentPosition(
            (position) => {
              const lat = position.coords.latitude;
              const lng = position.coords.longitude;
              const locPosition = new kakao.maps.LatLng(lat, lng);
              this.map.setCenter(locPosition);
            },
            (err) => {
              console.error('Geolocation error', err);
              this.infoText = '위치 정보를 가져올 수 없습니다.';
            }
          );
        } else {
          this.infoText = '이 브라우저에서는 위치 기능을 지원하지 않습니다.';
        }
      } else {
        console.error('Kakao maps SDK not loaded');
        this.infoText = '지도를 로드할 수 없습니다.';
      }
    },
    updateLocation() {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;
            const newPosition = new kakao.maps.LatLng(lat, lng);
            
            // 위치 추가 및 거리 계산
            if (this.positions.length > 0) {
              const lastPosition = this.positions[this.positions.length - 1];
              const linePath = [lastPosition, newPosition];
              const polyline = new kakao.maps.Polyline({
                path: linePath,
                strokeWeight: 5,
                strokeColor: '#db4040',
                strokeOpacity: 0.7,
                strokeStyle: 'solid'
              });
              
              // 거리 계산 (미터 단위)
              const newDistance = polyline.getLength();
              this.distance += newDistance;
              
              // 정보 업데이트
              this.infoText = `거리: ${(this.distance/1000).toFixed(2)}km`;
            }
            
            this.positions.push(newPosition);
          },
          (err) => {
            console.error('Geolocation update error', err);
          }
        );
      }
    },
    saveRunningData() {
      // 러닝 데이터 저장 로직
      const runData = {
        duration: this.seconds,
        distance: this.distance,
        date: new Date(),
        path: this.positions
      };
      
      // 로컬 스토리지에 저장 (나중에 서버로 전송하도록 변경 가능)
      const savedRuns = JSON.parse(localStorage.getItem('runningData') || '[]');
      savedRuns.push(runData);
      localStorage.setItem('runningData', JSON.stringify(savedRuns));
      
      this.infoText = `러닝 완료! ${(this.distance/1000).toFixed(2)}km를 ${this.formattedTime} 동안 달렸습니다.`;
    },
    // 추가된 네비게이션 메소드
    stayOnTimer() {
      // 현재 화면이므로 아무 작업 안함
    },    goToChat() {
      // 채팅 페이지로 이동
      this.$router.push('/chat');
    },
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

@import url('https://fonts.googleapis.com/css2?family=Pretendard&display=swap');
.run-container {
  padding: 16px;
  background-color: #FFF8F2;
  font-family: 'Pretendard', sans-serif;
}

.run-container {
  padding: 16px;
  background-color: #FFF8F2;
}

#map, .timer-card, .section > #info {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
}

body {
  font-family: sans-serif;
  background-color: #f0f9f0;
  margin: 0;
  padding: 20px;
  max-width: 390px; /* 가로 지정 */
}

#map {
  width: 100%;
  height: 400px;
  margin-bottom: 10px;
  visibility: hidden;
  position: absolute;
  top: -9999px;
}
.timer-card {
  background: linear-gradient(135deg, #FFCE9E, #FFA172);
  border-radius: 20px;
  padding: 24px;
  color: #fff;
  text-align: center;
  box-shadow: 0 4px 12px rgba(255, 112, 67, 0.25);
  font-family: 'Pretendard', sans-serif;
  position: relative;
  overflow: hidden;
}

.timer-card h2 {
  font-size: 16px;
  margin-bottom: 8px;
}

.timer-card .time {
  font-size: 42px;
  font-weight: 800;
  margin-bottom: 16px;
}

.play-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  background: linear-gradient(135deg, #FFF5E1 60%, #FFD1A1 100%);
  border-radius: 32px;
  padding: 32px 24px 24px 24px;
  box-shadow: 0 6px 24px rgba(255, 112, 67, 0.15);
  position: relative;
  min-width: 300px;

  background-image: url('@/assets/dog_bg.png');
  background-size: cover;       
  background-position: center;   
  background-repeat: no-repeat;  
}

.dog-zone {
  position: relative;
  width: 100%;
  height: 150px; 
}

.dog-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  animation: float 2s ease-in-out infinite;
  filter: drop-shadow(0 4px 16px rgba(255, 160, 67, 0.15));
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-12px); }
  100% { transform: translateY(0px); }
}

/* 발자국 애니메이션 */
.paw-prints {
  position: absolute;
  width: 100%;
  top: 5%;
  left: -150px;
  pointer-events: none;
}

.paw {
  position: absolute;
  width: 50px;
  opacity: 0.25;
  filter: drop-shadow(0 2px 4px rgba(255, 160, 67, 0.18));
  animation: paw-step 1.5s linear infinite;
}

.paw.one   { top: 20px;  left: 10px;   animation-delay: 0s;   transform: rotate(-10deg);}
.paw.two   { top: 60px;  left: 80px;   animation-delay: 0.3s; transform: rotate(12deg);}
.paw.three { top: 100px; left: 40px;   animation-delay: 0.6s; transform: rotate(-5deg);}
.paw.four  { top: 30px;  left: 110px;  animation-delay: 0.8s; transform: rotate(18deg);}
.paw.five  { top: 120px; left: 100px;  animation-delay: 1.1s; transform: rotate(-15deg);}

@keyframes paw-step {
  0%   { opacity: 0.25; transform: scale(1) translateY(0);}
  100% { opacity: 0;    transform: scale(1.3) translateY(-30px);}
}

.play-button {
  background: linear-gradient(135deg, #FFB172, #FF7043);
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 72px;
  height: 72px;
  font-size: 32px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(255, 112, 67, 0.22);
  transition: 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 10px;
}

.play-button:hover {
  background: #FFD1A1;
  color: #FF7043;
  transform: scale(1.08) rotate(-5deg);
  box-shadow: 0 8px 28px rgba(255, 112, 67, 0.25);
}

.section {
  margin-top: 230px;
  padding: 0 15px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}


.crew-list-section {
  background: #FAF3ED;
  border-radius: 16px;
  padding: 20px;
}

.crew-list-section h3 { font-size: 18px; margin: 16px 0 10px; }

.crew-card {
  background: #FFF8F2;
  border: 2px solid #FFE3D6;
  border-radius: 16px;
  padding: 14px 18px;
  margin-bottom: 14px;
  box-shadow: 0 4px 8px rgba(255, 126, 71, 0.1);
  transition: transform 0.2s ease;
}
.crew-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 12px rgba(255, 126, 71, 0.2);
}

.crew-header {
  display: flex;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
  color: #FF7043;
}

.crew-detail {
  background: #FFF3ED;
  margin-top: 12px;
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  color: #444;
}

.sub-title { margin-top: 12px; font-size: 16px; font-weight: bold; color: #333; }
.user-list {
  display: grid; grid-template-columns: repeat(3, 1fr);
  gap: 12px; padding-top: 10px;
}
.user-card {
  background: white; border-radius: 12px; padding: 10px; text-align: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.talk-button {
  margin-top: 10px;
  background: #FF7043; color: white; border: none;
  padding: 10px 16px; border-radius: 999px; font-weight: 600;
  cursor: pointer;
}

.join-btn {
  background: linear-gradient(135deg, #FF9F69, #FF7043);
  color: white;
  padding: 8px 20px;
  border: none;
  border-radius: 999px;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 3px 5px rgba(255, 112, 67, 0.2);
  transition: all 0.3s ease;
}
.join-btn:hover {
  background: #FF8A65;
  transform: scale(1.05);
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



/* 그룹 검색 */
.group-search {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 12px 0 24px;
}

.group-search input {
  flex: 1;
  height: 44px; /* 높이 명시 */
  padding: 0 20px 0 42px;
  font-size: 14px;
  border: 1px solid #ffd9c1;
  border-radius: 999px;
  background-color: #fffdfb;
  background-image: url('https://cdn-icons-png.flaticon.com/512/622/622669.png');
  background-size: 18px;
  background-repeat: no-repeat;
  background-position: 14px center;
  outline: none;
  box-sizing: border-box;
}

.group-search button {
  height: 44px; /* 동일한 높이 */
  padding: 0 20px;
  font-size: 14px;
  font-weight: 600;
  background: linear-gradient(135deg, #FF9F69, #FF7043);
  color: white;
  border: none;
  border-radius: 999px;
  cursor: pointer;
  box-shadow: 0 3px 6px rgba(255, 112, 67, 0.2);
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.group-search button:hover {
  background: #FF7E47;
  transform: scale(1.03);
}


.create-crew-btn {
  background: #FFD5BD;
  color: #FF5722;
  border: none;
  padding: 8px 16px;
  font-weight: 600;
  border-radius: 999px;
  font-size: 14px;
  margin-left: auto;
  cursor: pointer;
  transition: 0.3s;
}
.create-crew-btn:hover {
  background: #FFBFA2;
}
.crew-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.search-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ffd9c1;
  border-radius: 10px;
  font-size: 14px;
  outline: none;
}

.search-result {
  background-color: #fffdfb;
  border: 1px solid #ffe3d6;
}

.crew-meta {
  font-size: 13px;
  color: #666;
  margin-top: 6px;
}

</style>