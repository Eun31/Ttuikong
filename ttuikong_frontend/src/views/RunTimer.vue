// views/RunTimer.vue
<template>
  <div>
    <!-- 지도 위치용 hidden map -->
    <div id="map"></div>

    <div class="timer-card">
      <div class="play-area">
        <div class="dog-zone">
          <!-- 타이머 -->
          <h2>현재 러닝 시간</h2>
          <div class="time">{{ formattedTime }}</div>
          <!-- 강아지 이미지 -->
          <img class="dog-image" :src="isRunning ? dogRunImg : dogSitImg" alt="강아지 상태" />
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
      <div v-for="crew in filteredCrews" :key="'search-' + crew.id" class="crew-card search-result">
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

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, defineEmits } from 'vue';
import { useRouter } from 'vue-router';
import dogRun from '@/assets/dog_run.gif';
import dogSit from '@/assets/dog_sit.gif';

const emit = defineEmits(['navigate']);
const router = useRouter();

const searchQuery = ref('');
const seconds = ref(0);
const timer = ref(null);
const isRunning = ref(false);
const map = ref(null);
const kakaoMapLoaded = ref(false);
const infoText = ref('러닝을 시작하려면 ▶를 누르세요');
const positions = ref([]);
const distance = ref(0);
const expandedCrews = ref([]);
const startTime = ref('');

const crews = ref([
  { id: 1, name: '테일즈러너즈', mission: '5km 이상 달리기', avgDistance: 4.7, participationRate: 80, members: ['A', 'B', 'C'] },
  { id: 2, name: '런닝걸스', mission: '30분 연속 달리기', avgDistance: 5.4, participationRate: 60, members: ['D', 'E'] },
  { id: 3, name: '아침햇살조', mission: '아침 7시 달리기', participationRate: 40, members: ['F'] }
]);

const crewMembers = ref([
  { id: 1, name: '김민준', status: '달리는 중' },
  { id: 2, name: '이지우', status: '대기 중' },
  { id: 3, name: '박서연', status: '오프라인' },
  { id: 4, name: '최준호', status: '대기 중' },
  { id: 5, name: '정수빈', status: '오프라인' }
]);

const formattedTime = computed(() => {
  const min = Math.floor(seconds.value / 60);
  const sec = seconds.value % 60;
  return `${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`;
});

const filteredCrews = computed(() => {
  const query = searchQuery.value.trim().toLowerCase();
  if (!query) return crews.value;
  return crews.value.filter(c => c.name.toLowerCase().includes(query));
});

const loadKakaoMapScript = () => {
  const existingScript = document.getElementById('kakao-map-sdk');
  if (existingScript) {
    waitForKakao();
    return;
  }

  fetch("http://localhost:8080/api/config/kakao-map-key")
    .then(res => res.text())
    .then(apiKey => {
      const script = document.createElement("script");
      script.id = "kakao-map-sdk";
      script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${apiKey}&autoload=false`;
      script.async = true;
      script.onload = () => {
        if (window.kakao && window.kakao.maps) {
          window.kakao.maps.load(() => {
            kakaoMapLoaded.value = true;
            initMap();
          });
        }
      };
      document.head.appendChild(script);
    })
    .catch(error => {
      console.error("Kakao map key fetch error", error);
      infoText.value = 'API 키를 가져오는 중 오류가 발생했습니다.';
    });
};

const waitForKakao = () => {
  if (window.kakao && window.kakao.maps) {
    window.kakao.maps.load(() => {
      kakaoMapLoaded.value = true;
      initMap();
    });
  } else {
    setTimeout(() => waitForKakao(), 100);
  }
};

const toggleTimer = async () => {
  if (isRunning.value) {
    clearInterval(timer.value);
    await saveRunningData();
  } else {
    startTime.value = new Date().toISOString();
    const token = localStorage.getItem("jwt");
    await fetch("http://localhost:8080/api/runs/running-status", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify({
        startTime: startTime.value,
        status: "start"
      })
    });

    timer.value = setInterval(() => {
      seconds.value++;
      if (kakaoMapLoaded.value) updateLocation();
    }, 1000);
    infoText.value = '달리는 중...';
  }
  isRunning.value = !isRunning.value;
};

const toggleCrew = (id) => {
  if (expandedCrews.value.includes(id)) {
    expandedCrews.value = expandedCrews.value.filter(cid => cid !== id);
  } else {
    expandedCrews.value.push(id);
  }
};

const initMap = () => {
  const mapContainer = document.getElementById('map');
  if (!mapContainer) return;

  const mapOption = {
    center: new window.kakao.maps.LatLng(37.566826, 126.9786567),
    level: 3
  };

  map.value = new window.kakao.maps.Map(mapContainer, mapOption);

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const lat = pos.coords.latitude;
        const lng = pos.coords.longitude;
        map.value.setCenter(new window.kakao.maps.LatLng(lat, lng));
      },
      (err) => {
        console.error('Geolocation error', err);
        infoText.value = '위치 정보를 가져올 수 없습니다.';
      }
    );
  } else {
    infoText.value = '이 브라우저에서는 위치 기능을 지원하지 않습니다.';
  }
};

const updateLocation = () => {
  if (!kakaoMapLoaded.value) return;

  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition((pos) => {
      const lat = pos.coords.latitude;
      const lng = pos.coords.longitude;
      const newPos = new window.kakao.maps.LatLng(lat, lng);

      if (positions.value.length > 0) {
        const lastPos = positions.value[positions.value.length - 1];
        const polyline = new window.kakao.maps.Polyline({
          path: [lastPos, newPos],
          strokeWeight: 5,
          strokeColor: '#db4040',
          strokeOpacity: 0.7,
          strokeStyle: 'solid'
        });

        distance.value += polyline.getLength();
        infoText.value = `거리: ${(distance.value / 1000).toFixed(2)}km`;
      }

      positions.value.push(newPos);
    });
  }
};

const saveRunningData = async () => {
  const endTime = new Date().toISOString();
  const token = localStorage.getItem("jwt");

  await fetch("http://localhost:8080/api/runs/track-location", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`
    },
    body: JSON.stringify({
      startTime: startTime.value,
      endTime,
      distance: distance.value
    })
  });

  infoText.value = `러닝 완료! ${(distance.value / 1000).toFixed(2)}km를 ${formattedTime.value} 동안 달렸습니다.`;
};

const uploadMapImage = async (file) => {
  const formData = new FormData();
  formData.append("image", file);
  formData.append("startTime", startTime.value);
  formData.append("endTime", new Date().toISOString());

  await fetch("http://localhost:8080/api/runs/upload-map-image", {
    method: "POST",
    body: formData
  });
};

const stayOnTimer = () => { };
const goToChat = () => router.push('/chat');
const navigateToTimer = () => emit('navigate', 'RunTimer');
const navigateToRank = () => emit('navigate', 'RunWithRank');
const createCrew = () => console.log('크루 생성 기능 개발 필요');
const joinCrew = (crew) => console.log('크루 가입 기능 개발 필요:', crew.name);

onMounted(loadKakaoMapScript);
onBeforeUnmount(() => {
  if (timer.value) clearInterval(timer.value);
});

const dogRunImg = dogRun;
const dogSitImg = dogSit;
</script>

<style scoped>
.run-container {
  padding: 16px;
  background-color: #FFF8F2;
  font-family: sans-serif;
}

.run-container {
  padding: 16px;
  background-color: #FFF8F2;
}

#map,
.timer-card,
.section>#info {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
}

body {
  font-family: sans-serif;
  background-color: #f0f9f0;
  margin: 0;
  max-width: 390px;
  /* 가로 지정 */
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
  font-family: sans-serif;
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

.crew-list-section h3 {
  font-size: 18px;
  margin: 16px 0 10px;
}

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

.sub-title {
  margin-top: 12px;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.user-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding-top: 10px;
}

.user-card {
  background: white;
  border-radius: 12px;
  padding: 10px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.talk-button {
  margin-top: 10px;
  background: #FF7043;
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 999px;
  font-weight: 600;
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
  height: 44px;
  /* 높이 명시 */
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
  height: 44px;
  /* 동일한 높이 */
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