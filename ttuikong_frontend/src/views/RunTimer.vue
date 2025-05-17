// views/RunTimer.vue
<template>
  <div>
    <!-- 지도 위치용 hidden map -->
    <div id="map"></div>
    <!-- <div class="section">
      <div id="info">{{ infoText }}</div>
    </div> -->

  <div class="timer-card">
    <div class="play-area">
      <div class="dog-zone">
        <!-- 타이머 -->
        <h2>현재 러닝 시간</h2>
        <div class="time">{{ formattedTime }}</div>
        <!-- 강아지 이미지 -->
        <img
          class="dog-image"
          :src="isRunning ? dogRun : dogSit"
          alt="강아지 상태"
        />
      </div>
    </div>
    <!-- 플레이 버튼 -->
      <button class="play-button" @click="toggleTimer">
        {{ isRunning ? '■' : '▶' }}
      </button>
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
      searchQuery: '', 
      dogSit,
      dogRun,
      seconds: 0,
      timer: null,
      isRunning: false,
      map: null,
      kakaoMapLoaded: false,
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
    this.loadKakaoMapScript();
  },
  beforeUnmount() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  },
  methods: {
    loadKakaoMapScript() {
      // 이미 로드된 스크립트가 있는지 확인
      const existingScript = document.getElementById('kakao-map-sdk');
      if (existingScript) {
        this.waitForKakao();
        return;
      }

      // API 키 가져오기
      fetch("http://localhost:8080/api/config/kakao-map-key")
        .then(res => res.text())
        .then(apiKey => {
          const script = document.createElement("script");
          script.id = "kakao-map-sdk";
          script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${apiKey}&autoload=false`;
          script.async = true;
          script.onload = () => {
            // 스크립트 로드 완료 후 카카오맵 초기화
            if (window.kakao && window.kakao.maps) {
              window.kakao.maps.load(() => {
                this.kakaoMapLoaded = true;
                this.initMap();
              });
            }
          };
          document.head.appendChild(script);
        })
        .catch(error => {
          console.error("Kakao map key fetch error", error);
          this.infoText = 'API 키를 가져오는 중 오류가 발생했습니다.';
        });
    },
    
    waitForKakao() {
      // 카카오 맵 객체가 로드될 때까지 대기
      if (window.kakao && window.kakao.maps) {
        window.kakao.maps.load(() => {
          this.kakaoMapLoaded = true;
          this.initMap();
        });
      } else {
        setTimeout(() => this.waitForKakao(), 100);
      }
    },

    async toggleTimer() {
    if (this.isRunning) {
      clearInterval(this.timer);
      await this.saveRunningData(); // 종료시 저장
    } else {
      // 러닝 시작 백엔드 알림
      const startTime = new Date().toISOString();
      this.startTime = startTime;

      await fetch("http://localhost:8080/api/runs/running-status", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          startTime: startTime,
          status: "start"
        })
      });

      this.timer = setInterval(() => {
        this.seconds++;
        if (this.kakaoMapLoaded) this.updateLocation();
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
      try {
        const mapContainer = document.getElementById('map');
        if (!mapContainer) {
          console.error('Map container not found');
          return;
        }
        
        const mapOption = {
          center: new window.kakao.maps.LatLng(37.566826, 126.9786567),
          level: 3
        };
        
        this.map = new window.kakao.maps.Map(mapContainer, mapOption);

        if (navigator.geolocation) {
          navigator.geolocation.getCurrentPosition(
            (position) => {
              const lat = position.coords.latitude;
              const lng = position.coords.longitude;
              const locPosition = new window.kakao.maps.LatLng(lat, lng);
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
      } catch (error) {
        console.error('Error initializing map:', error);
        this.infoText = '지도를 초기화하는 중 오류가 발생했습니다.';
      }
    },

    updateLocation() {
      if (!this.kakaoMapLoaded || !window.kakao || !window.kakao.maps) {
        console.warn('Kakao maps not loaded yet');
        return;
      }
      
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;
            const newPosition = new window.kakao.maps.LatLng(lat, lng);
            
            // 위치 추가 및 거리 계산
            if (this.positions.length > 0) {
              const lastPosition = this.positions[this.positions.length - 1];
              const linePath = [lastPosition, newPosition];
              const polyline = new window.kakao.maps.Polyline({
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
    
    async saveRunningData() {
      const endTime = new Date().toISOString();

      await fetch("http://localhost:8080/api/runs/track-location", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          startTime: this.startTime,
          endTime: endTime,
          distance: this.distance
        })
      });

      this.infoText = `러닝 완료! ${(this.distance / 1000).toFixed(2)}km를 ${this.formattedTime} 동안 달렸습니다.`;
    },

    async uploadMapImage(file) {
      const formData = new FormData();
      formData.append("image", file); // 이미지 파일
      formData.append("startTime", this.startTime);
      formData.append("endTime", new Date().toISOString());

      await fetch("http://localhost:8080/api/runs/upload-map-image", {
        method: "POST",
        body: formData
      });
    },

    // 추가된 네비게이션 메소드
    stayOnTimer() {
      // 현재 화면이므로 아무 작업 안함
    },
    
    goToChat() {
      // 채팅 페이지로 이동
      this.$router.push('/chat');
    },
    
    navigateToTimer() {
      this.$emit('navigate', 'RunTimer');
    },
    
    navigateToRank() {
      this.$emit('navigate', 'RunWithRank');
    },
    
    createCrew() {
      // 크루 생성 로직 구현 필요
      console.log('크루 생성 기능 개발 필요');
    },
    
    joinCrew(crew) {
      // 크루 가입 로직 구현 필요
      console.log('크루 가입 기능 개발 필요:', crew.name);
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
  color: orange;
  background-color: #FFE3D6;
  text-align: center;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 4px 12px rgba(255, 112, 67, 0.25);
  font-family: 'Pretendard', sans-serif;
  position: relative;
  overflow: hidden;
}

.timer-card h2 {
  font-size: 16px;
}

.timer-card .time {
  font-size: 42px;
  font-weight: 800;
}

.play-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  background: linear-gradient(135deg, #FFF5E1 60%, #FFD1A1 100%);
  padding: 10px;
  box-shadow: 0 6px 20px rgba(255, 112, 67, 0.15);
  position: relative;
  min-width: 300px;
  background-image: url('@/assets/dog_bg.png');
  background-size: cover;       
  background-position: center;   
  background-repeat: no-repeat;  
}

.dog-zone {
  position: relative;
  width: 70%;
}

.dog-image {
  width: 50%;
  object-fit: contain;
  margin-top: -26px;
  margin-left: 25px;
  filter: drop-shadow(0 4px 10px rgba(255, 160, 67, 0.15));
}

.play-button {
  background: linear-gradient(135deg, #FFB172, #FF7043);
  color: #fff;
  border: none;
  width: 100%;
  height: 5%;
  font-size: 30px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(255, 112, 67, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
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