// views/RunTimer.vue
<template>
  <div>

    <div class="timer-card">
      <div class="play-area">
        <div class="dog-zone">
          <div id="map-wrapper">
            <div id="map"></div>
            <svg id="route-overlay">
              <polyline id="running-path" fill="none" stroke="red" stroke-width="4" />
            </svg>
            <!-- 타이머 -->
            <h2 class="time-head">현재 러닝 시간</h2>
            <div class="time">{{ formattedTime }}</div>
            <!-- 강아지 이미지 -->
            <img class="dog-image" :src="isRunning ? dogRunImg : dogSitImg" alt="강아지 상태" />
          </div>
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
        <button class="create-crew-btn" @click="toggleCrewForm">+ 크루 생성</button>
      </div>

      <!-- 크루 생성-->
      <div v-if="showCrewForm" class="form-box">
        <h2>크루 생성</h2>
        <form @submit.prevent="submitCrew">
          <div class="form-group">
            <label>크루명</label>
            <input v-model="newCrew.roomName" placeholder="크루명" required />
          </div>
          <div class="form-group">
            <label>소개</label>
            <textarea v-model="newCrew.roomDescription" placeholder="크루 소개" />
          </div>
          <div class="form-group">
            <label>목표 유형</label>
            <select v-model="newCrew.goalType">
              <option value="SUM">총 시간</option>
              <option value="AVERAGE">평균 시간</option>
            </select>
          </div>
          <div class="form-group">
            <label>목표 수치</label>
            <input v-model.number="newCrew.goalTime" type="number" placeholder="예: 30" required />
          </div>
          <div class="form-group">
            <label>시작일</label>
            <input v-model="newCrew.startDate" type="date" required />
          </div>
          <div class="form-group">
            <label>종료일</label>
            <input v-model="newCrew.endDate" type="date" required />
          </div>
          <button type="submit" class="submit-button">크루 생성하기</button>
        </form>
      </div>

      <!--크루 검색-->
      <div v-for="crew in filteredCrews" :key="'search-' + crew.id" class="crew-card search-result">
        <div class="crew-header">
          <h4>{{ crew.roomName }}</h4>
          <button class="join-btn" @click.stop="joinCrew(crew)">가입하기</button>
        </div>
        <p class="crew-meta"> 목표: {{ crew.goalType }} : {{ crew.goalTime }}</p>
        <p class="crew-meta"> 참여 인원: {{crewMembers.find(c => c.crewId === crew.id)?.members.length || 0}}명</p>
      </div>
      <div class="group-search">
        <input type="text" v-model="searchQuery" placeholder="크루 이름으로 검색..." class="search-input" />
        <button>검색</button>
      </div>

      <!-- 내 크루 목록 -->
      <h3>내가 속한 크루</h3>
      <div v-for="crew in myCrews" :key="crew.id" class="crew-card" @click="toggleCrew(crew.id)">
        <div class="crew-header">
          <h4>{{ crew.roomName }}</h4>
          <span>{{crewMembers.find(c => c.crewId === crew.id)?.members.length || 0}}명
            <button v-if="crew.creatorId != userId" class="quit-btn" @click.stop="quitCrew(crew)">탈퇴하기</button>
            <button v-else class="delete-btn" @click.stop="deleteCrew(crew)">삭제하기</button>
          </span>
        </div>
        <transition name="fade">
          <div v-show="expandedCrews.includes(crew.id)" class="crew-detail">
            <p>📍 목표: <strong>{{ crew.goalType }} : {{ crew.goalTime }}</strong></p>
            <!-- <p>🏅 목표 달성률: {{ crew.participationRate }}%</p> -->
            <h3 class="sub-title">크루 멤버</h3>
            <div class="user-list">
              <div v-for="member in crewMembers.find(c => c.crewId === crew.id)?.members || []" :key="member.id"
                class="user-card">
                <strong>{{ member.nickname }}</strong>
                <!-- <span>{{ member.status }}</span> -->
              </div>
            </div>
            <h3 class="sub-title">실시간 메신저</h3>
            <button class="talk-button" @click="goToChat(crew.id)">▶ Talk</button>
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
import { ref, computed, onMounted, onBeforeUnmount, defineEmits, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import dogRun from '@/assets/dog_run.gif';
import dogSit from '@/assets/dog_sit.gif';
import html2canvas from "html2canvas";
import { Canvg } from 'canvg';

const emit = defineEmits(['navigate']);
const router = useRouter();
const route = useRoute();

const searchQuery = ref('');
const seconds = ref(0);
const timer = ref(null);
const isRunning = ref(false);
const status = ref('ended');
const map = ref(null);
const kakaoMapLoaded = ref(false);
const infoText = ref('러닝을 시작하려면 ▶를 누르세요');
const positions = ref([]);
const distance = ref(0);
const expandedCrews = ref([]);
const startTime = ref('');
const endTime = ref(null);
const duration = ref(0);
const token = ref(localStorage.getItem("jwt"));
const userId = ref(Number(localStorage.getItem("userId")));
const showCrewForm = ref(false);
const crews = ref([]);
const crewMembers = ref([]);
const newCrew = ref({
  roomName: '',
  roomDescription: '',
  goalType: '거리',
  goalTime: 0,
  startDate: '',
  endDate: ''
});

/* 크루 생성 */
const toggleCrewForm = () => {
  showCrewForm.value = !showCrewForm.value;
};

const submitCrew = async () => {
  console.log("🚨 token for submitCrew:", token.value);

  const response = await fetch('http://localhost:8080/api/crew', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify(newCrew.value)
  });

  const message = await response.text();
  alert(message);
  if (response.ok) {
    showCrewForm.value = false;
    newCrew.value = {
      roomName: '',
      roomDescription: '',
      goalType: '거리',
      goalTime: 0,
      startDate: '',
      endDate: ''
    };
  }
};

const joinCrew = async (crew) => {
  const currentToken = localStorage.getItem("jwt");
  const currentuserId = localStorage.getItem("userId");

  if (!currentuserId) {
    alert("로그인이 필요합니다.");
    return;
  }

  try {
    const res = await fetch(`http://localhost:8080/api/crew/${crew.id}/join?userId=${currentuserId}`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${currentToken}`
      }
    });

    const message = await res.text();
    alert(message);
  } catch (err) {
    console.error("크루 가입 중 오류:", err);
    alert("크루 가입 실패: 서버 오류");
  }
};

/* 크루 삭제, 탈퇴 */
const deleteCrew = async (crew) => {
  try {
    const res = await fetch(`http://localhost:8080/api/crew/${crew.id}?creatorId=${userId.value}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token.value}`
      }
    });

    const message = await res.text();
    alert(message);

    if (res.ok) {
      // 삭제 후 목록 갱신
      await fetchCrewsAndMembers();
    }
  } catch (err) {
    console.error("크루 삭제 중 오류:", err);
    alert("크루 삭제 실패: 서버 오류");
  }
};

const quitCrew = async (crew) => {
  try {
    const res = await fetch(`http://localhost:8080/api/crew/${crew.id}/leave?userId=${userId.value}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token.value}`
      }
    });

    const message = await res.text();
    alert(message);

    if (res.ok) {
      // 탈퇴 후 목록 갱신
      await fetchCrewsAndMembers();
    }
  } catch (err) {
    console.error("크루 탈퇴 중 오류:", err);
    alert("크루 탈퇴 실패: 서버 오류");
  }
};


/* 크루 데이터베이스에서 불러오기*/
const fetchCrewsAndMembers = async () => {
  const currentToken = localStorage.getItem("jwt");
  try {
    const res = await fetch("http://localhost:8080/api/crew", {
      headers: { Authorization: `Bearer ${currentToken}` }
    });
    if (!res.ok) throw new Error("크루 목록 불러오기 실패");

    const data = await res.json();
    console.log("crew list:", data);
    crews.value = data;

    const memberPromises = data.map(async crew => {
      const res = await fetch(`http://localhost:8080/api/crew/${crew.id}/members`, {
        headers: { Authorization: `Bearer ${currentToken}` }
      });
      if (!res.ok) throw new Error(`멤버 불러오기 실패: crewId=${crew.id}`);

      const members = await res.json();
      console.log(`members for crew ${crew.id}:`, members);

      return { crewId: crew.id, members: members || [] };
    });

    const memberResults = await Promise.all(memberPromises);
    crewMembers.value = memberResults;

  } catch (error) {
    console.error("크루 또는 멤버 불러오는 중 오류:", error);
  }
};

const toggleCrew = (id) => {
  if (expandedCrews.value.includes(id)) {
    expandedCrews.value = expandedCrews.value.filter(cid => cid !== id);
  } else {
    expandedCrews.value.push(id);
  }
};

const myCrews = computed(() => {
  return crews.value.filter(crew => {
    return (
      crew &&
      (crew.creatorId == userId.value ||
        crewMembers.value.find(c => c.crewId === crew.id)?.members?.some(m => m.id == userId.value))
    );
  });
});

/* 시간 format */
const formattedTime = computed(() => {
  const min = Math.floor(seconds.value / 60);
  const sec = seconds.value % 60;
  return `${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`;
});

/* 크루 필터링 */
const filteredCrews = computed(() => {
  const query = searchQuery.value.trim().toLowerCase();
  if (!query) return crews.value;
  return crews.value.filter(c => c.roomName.toLowerCase().includes(query));
});

/* 카카오 API */
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

/* 지도 표시 */
function updateLocation() {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(position => {
      const lat = position.coords.latitude;
      const lng = position.coords.longitude;
      console.log(`현재 위치: ${lat}, ${lng}`);
    }, error => {
      console.error("위치 추적 실패:", error);
    });
  } else {
    console.error("이 브라우저는 geolocation을 지원하지 않습니다.");
  }
}

const drawPolylineOnSVG = () => {
  if (!map.value || positions.value.length === 0) return;

  const svg = document.getElementById("route-overlay");
  const polyline = document.getElementById("running-path");
  const projection = map.value.getProjection();

  const path = positions.value.map(latlng => {
    const point = projection.containerPointFromCoords(latlng);
    return `${point.x},${point.y}`;
  }).join(" ");

  polyline.setAttribute("points", path);
};


const uploadMapImage = async () => {
  try {
    drawPolylineOnSVG();

    const mapContainer = document.getElementById("map-wrapper");
    const svgElement = document.getElementById("route-overlay");

    if (!mapContainer || !svgElement) {
      console.error("필요한 요소를 찾을 수 없습니다.");
      return;
    }

    await nextTick();
    await new Promise(resolve => setTimeout(resolve, 500));

    const canvas = await html2canvas(mapContainer, {
      backgroundColor: null,
      useCORS: false,
      allowTaint: false,
      scale: 1,
      logging: false,
      width: mapContainer.offsetWidth,
      height: mapContainer.offsetHeight,
      foreignObjectRendering: false,
      ignoreElements: function (element) {
        return (
          element.tagName === 'IFRAME' ||
          element.classList.contains('dog-image') ||
          element.classList.contains('time') ||
          element.classList.contains('time-head')
        );
      }
    });

    const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/png', 0.8));

    if (!blob) {
      console.error("canvas.toBlob 실패");
      return;
    }

    const formData = new FormData();
    formData.append("image", blob);
    formData.append("startTime", startTime.value);
    formData.append("endTime", new Date().toISOString());

    const res = await fetch("http://localhost:8080/api/runs/upload-map-image", {
      method: "POST",
      headers: { Authorization: `Bearer ${token.value}` },
      body: formData
    });

    if (!res.ok) {
      const err = await res.text();
      console.error("업로드 실패:", err);
    } else {
      console.log("러닝 경로 이미지 업로드 완료");
    }

  } catch (error) {
    console.error("맵 이미지 업로드 중 오류:", error);
    try {
      const alternativeBlob = await convertSvgToBlob();
      if (alternativeBlob) {
        const formData = new FormData();
        formData.append("image", alternativeBlob);
        formData.append("startTime", startTime.value);
        formData.append("endTime", new Date().toISOString());

        const res = await fetch("http://localhost:8080/api/runs/upload-map-image", {
          method: "POST",
          headers: { Authorization: `Bearer ${token.value}` },
          body: formData
        });

        if (res.ok) {
          console.log("대체 방법으로 업로드 완료");
        }
      }
    } catch (altError) {
      console.error("대체 방법도 실패:", altError);
    }
  }
};

const convertSvgToBlob = async () => {
  try {
    const svgElement = document.getElementById("route-overlay");
    if (!svgElement) return null;

    const svgData = new XMLSerializer().serializeToString(svgElement);
    const svgBlob = new Blob([svgData], { type: 'image/svg+xml;charset=utf-8' });

    const url = URL.createObjectURL(svgBlob);
    const img = new Image();

    return new Promise((resolve) => {
      img.onload = () => {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');

        canvas.width = 400;
        canvas.height = 400;

        ctx.drawImage(img, 0, 0);

        canvas.toBlob((blob) => {
          URL.revokeObjectURL(url);
          resolve(blob);
        }, 'image/png');
      };

      img.onerror = () => {
        URL.revokeObjectURL(url);
        resolve(null);
      };

      img.src = url;
    });
  } catch (error) {
    console.error("SVG 변환 실패:", error);
    return null;
  }
};

const saveRunningData = async () => {
  const currentToken = localStorage.getItem("jwt");

  await fetch("http://localhost:8080/api/runs/track-location", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${currentToken}`
    },
    body: JSON.stringify({
      startTime: startTime.value,
      endTime: endTime.value,
      distance: (distance.value / 1000).toFixed(2)
    })
  });

  infoText.value = `러닝 완료! ${(distance.value / 1000).toFixed(2)}km를 ${formattedTime.value} 동안 달렸습니다.`;
};

/* 타이머 기능 */
const toggleTimer = async () => {
  const currentToken = localStorage.getItem("jwt");

  if (isRunning.value) {
    // 러닝 종료
    clearInterval(timer.value);

    endTime.value = new Date().toISOString();
    duration.value = seconds.value;

    // 1. 러닝 종료 데이터 전송
    const endJsonData = JSON.stringify({
      startTime: startTime.value,
      endTime: endTime.value,
      duration: duration.value,
      distance: (distance.value / 1000).toFixed(2),
      status: "ended"
    });

    console.log("종료 시 전송 데이터:", endJsonData);

    try {
      await fetch("http://localhost:8080/api/runs/running-status", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${currentToken}`
        },
        body: endJsonData
      });

      // 2. 저장 및 지도 업로드 실행
      await saveRunningData();
      await uploadMapImage();

      infoText.value = "러닝이 종료되었습니다.";
    } catch (error) {
      console.error("러닝 종료 요청 중 에러 발생:", error);
    }
  } else {
    // 러닝 시작
    startTime.value = new Date().toISOString();

    const startJsonData = JSON.stringify({
      startTime: startTime.value,
      status: "running"
    });

    console.log("시작 시 전송 데이터:", startJsonData);

    try {
      await fetch("http://localhost:8080/api/runs/running-status", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${currentToken}`
        },
        body: startJsonData
      });

      timer.value = setInterval(() => {
        seconds.value++;
        if (kakaoMapLoaded.value) updateLocation();
      }, 1000);

      infoText.value = "달리는 중...";
    } catch (error) {
      console.error("러닝 시작 요청 중 에러 발생:", error);
    }
  }

  // 상태 전환
  isRunning.value = !isRunning.value;
};



/* 유저 불러오기 */
const getCurrentUser = async () => {
  const currentToken = localStorage.getItem("jwt");
  const currentuserId = localStorage.getItem("userId");

  token.value = currentToken;
  userId.value = currentuserId;

  try {
    const res = await fetch("http://localhost:8080/api/users/me", {
      headers: {
        Authorization: `Bearer ${currentToken}`
      }
    });

    if (!res.ok) throw new Error("유저 정보 불러오기 실패");

    const data = await res.json();
    const userId = data.user.id;

    localStorage.setItem("userId", currentuserId);
    console.log("로그인된 사용자 ID:", currentuserId);

    localStorage.setItem("token", currentToken);
    console.log("사용자 token:", currentToken);

  } catch (err) {
    console.error("사용자 정보 요청 실패:", err);
    alert("로그인이 필요합니다.");
  }
};

const stayOnTimer = () => { };
function goToChat(crewId) {
  router.push(`/chat/${crewId}`);
}
const navigateToTimer = () => emit('navigate', 'RunTimer');
const navigateToRank = () => emit('navigate', 'RunWithRank');

onMounted(() => {
  getCurrentUser();
  loadKakaoMapScript();
  fetchCrewsAndMembers();
});

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
  margin: 0 auto;
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
  justify-content: flex-end;
  background: linear-gradient(135deg, #FFF5E1 60%, #FFD1A1 100%);
  box-shadow: 0 6px 20px rgba(255, 112, 67, 0.15);
  position: relative;
  width: 100%;
  padding: 0;
  gap: 0;
  align-items: stretch;
}


#map-wrapper {
  position: relative;
  width: 100%;
  height: auto;
  border-radius: 16px;
  overflow: hidden;
  background-color: #ddd;
  min-height: 450px;
}

@media screen and (max-width: 420px) {
  #map-wrapper {
    min-height: 200px;
  }
}


#map,
#route-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.dog-zone {
  width: 100%;
}

.time {
  position: absolute;
  top: 15%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3;
}

.time-head {
  position: absolute;
  top: 10%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3;
}


.dog-image {
  position: absolute;
  top: calc(15% + 40px);
  left: 53%;
  transform: translateX(-50%);
  width: 20%;
  z-index: 3;
}


@media screen and (max-width: 768px) {
  .dog-image {
    position: absolute;
    top: calc(15% + 40px);
    left: 53%;
    transform: translateX(-50%);
    width: 30%;
    z-index: 3;
  }
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
  font-size: 17px;
  box-shadow: 0 3px 5px rgba(255, 112, 67, 0.2);
  transition: all 0.3s ease;
}

.join-btn:hover {
  background: #FF8A65;
  transform: scale(1.05);
}

.quit-btn,
.delete-btn {
  color: #FF7043;
  padding: 8px 20px;
  border: none;
  border-radius: 999px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
}

.quit-btn:hover,
.delete-btn:hover {
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

/* 크루 생성 css */
.form-box {
  background-color: #FFF3EC;
  border: 2px solid #FFD5BD;
  border-radius: 20px;
  padding: 24px;
  margin: 20px 0;
  box-shadow: 0 4px 12px rgba(255, 112, 67, 0.15);
  font-family: 'Pretendard', sans-serif;
  max-width: 100%;
}

.form-box h2 {
  color: #FF7043;
  margin-bottom: 20px;
  font-size: 20px;
  text-align: center;
}

.form-group {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 600;
  margin-bottom: 6px;
  color: #444;
}

.form-group input,
.form-group textarea,
.form-group select {
  padding: 12px;
  border: 1px solid #ffd1b6;
  border-radius: 10px;
  font-size: 14px;
  background-color: #fffaf7;
  box-shadow: inset 0 2px 4px rgba(255, 160, 120, 0.05);
  transition: border 0.2s;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: #ff8a65;
  background-color: #fff6f0;
}

textarea {
  resize: none;
  min-height: 80px;
}

.submit-button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #FF9F69, #FF7043);
  color: white;
  font-weight: bold;
  font-size: 16px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  box-shadow: 0 4px 8px rgba(255, 112, 67, 0.2);
  transition: all 0.3s ease;
}

.submit-button:hover {
  background: #FF7E47;
  transform: scale(1.02);
}
</style>