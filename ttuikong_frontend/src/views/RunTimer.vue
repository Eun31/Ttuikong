<template>
  <div>
    <!-- 러닝 종료 시 mood 선택 -->
    <div v-if="showMoodModal" class="modal-overlay">
      <div class="modal-box">
        <h3>러닝은 어땠나요?</h3>
        <div class="mood-options">
          <button v-for="mood in moodOptions" :key="mood.label" class="mood-btn" @click="selectMood(mood.label)">
            <span class="emoji">{{ mood.emoji }}</span>
            <span class="label">{{ mood.label }}</span>
          </button>
        </div>
      </div>
    </div>

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
              <option value="SUM">총합 시간</option>
              <option value="AVERAGE">평균 시간</option>
            </select>
          </div>
          <div class="form-group">
            <label>목표 시간 (초)</label>
            <div style="display: flex; gap: 8px; align-items: center;">
              <input v-model.number="goalHours" type="number" min="0" max="23" placeholder="시" required />시
              <input v-model.number="goalMinutes" type="number" min="0" max="59" placeholder="분" required />분
              <input v-model.number="goalSeconds" type="number" min="0" max="59" placeholder="초" required />초
            </div>
          </div>
          <div class="form-group">
            <input v-model="newCrew.startDate" type="date" :min="today" required />
          </div>
          <div class="form-group">
            <label>종료일</label>
            <input v-model="newCrew.endDate" type="date" :min="newCrew.startDate" required />
          </div>
          <button type="submit" class="submit-button">크루 생성하기</button>
        </form>
      </div>

      <!--크루 검색-->
      <div class="group-search">
        <input type="text" v-model="searchQuery" placeholder="크루 이름으로 검색..." class="search-input" />
      </div>
      <div v-for="crew in paginatedCrews" :key="'search-' + crew.id" class="crew-card search-result">
        <div class="crew-header"  @click="openCrewId = openCrewId === crew.id ? null : crew.id">
          <h4>{{ crew.roomName }}</h4>
          <span>
            {{ crewMembers.find(c => c.crewId === crew.id)?.members.length || 0 }}명
            <button v-if="new Date(crew.startDate) > new Date() && (crewMembers.find(c => c.crewId === crew.id)?.members.length || 0) < 10" class="join-btn" @click.stop="joinCrew(crew)">가입하기</button>
          </span>
        </div>
        <p style="font-size: 1em; color: #f57c00; padding-top:5px; padding-bottom: 5px;">{{ crew.roomDescription }}</p>
        <p class="crew-meta">챌린지일: {{ crew.startDate }} ~ {{ crew.endDate }}</p>
        <p class="crew-meta">목표: {{ crew.goalType == 'SUM' ? '총합' : '평균' }} {{ formatDuration(crew.goalTime) }}</p>
      </div>
      <div class="pagination-controls">
        <button @click="prevPage" :disabled="page === 1">이전</button>
        <span>{{ page }} / {{ totalPages }}</span>
        <button @click="nextPage" :disabled="page === totalPages">다음</button>
      </div>

      <!-- 내 크루 목록 -->
      <h3>내가 속한 크루</h3>
      <div v-for="crew in mypaginatedCrews" :key="crew.id" class="crew-card" @click="toggleCrew(crew.id)">
        <div class="crew-header">
          <h4>{{ crew.roomName }}</h4>
          <span>{{crewMembers.find(c => c.crewId === crew.id)?.members.length || 0}}명
            <button v-if="crew.creatorId != userId" class="quit-btn" @click.stop="quitCrew(crew)">탈퇴하기</button>
            <button v-else class="delete-btn" @click.stop="deleteCrew(crew)">삭제하기</button>
          </span>
        </div>
        <p style="font-size: 1em; color: #f57c00; padding-top:5px; padding-bottom: 5px;">{{ crew.roomDescription }}</p>
        <p class="crew-meta">챌린지일: {{ crew.startDate }} ~ {{ crew.endDate }}</p>
        <p class="crew-meta">목표: {{ crew.goalType == 'SUM' ? '총합' : '평균' }} {{ formatDuration(crew.goalTime) }}</p>
        <transition name="fade">
          <div v-show="expandedCrews.includes(crew.id)" class="crew-detail">
            <p class="crew-meta">크루 생성일: {{ crew.createdAt.split("T")[0] }}</p>            
            <div class="goal-status-box">
              <h4 class="title">🏅 목표 달성률</h4>
              <!-- 게이지 바 -->
              <div v-if="(crew.goalType == 'SUM' ? Percentage(crew.goalTime, crewStatus.totalDuration) : Percentage(crew.goalTime, crewStatus.averageDuration)) < 100" class="progress-bar-container">
                <div class="progress-bar-bg">
                  <div class="progress-bar-fill" :style="{ width: crew.goalType == 'SUM' ? progressPercent(crew.goalTime, crewStatus.totalDuration) : progressPercent(crew.goalTime, crewStatus.averageDuration) }"></div>
                </div>
                <p class="progress-percent">{{ crew.goalType == 'SUM' ? Percentage(crew.goalTime, crewStatus.totalDuration) : Percentage(crew.goalTime, crewStatus.averageDuration) }}</p>
              </div>
              <div v-else class="goal-celebration">
                <p>🎉목표를 이미 달성했습니다🎉</p>
              </div>
              <!-- 수치 정보 -->
              <div class="goal-details">
                <p><strong>📍 목표:</strong> {{ crew.goalType == 'SUM' ? '총합' : '평균' }} {{ formatDuration(crew.goalTime) }}</p>
                <p>
                  <strong>📍 시간 현황:</strong>
                  {{ crew.goalType == 'SUM' ? formatDuration(crewStatus.totalDuration) : formatDuration(crewStatus.averageDuration) }}
                </p>
              </div>
          </div>
            <hr style="border: none; border-top: 2px dashed tan; margin: 24px 0;">
            <h3 class="sub-title">크루 멤버</h3>
            <div class="user-list">
              <div v-for="member in crewMembersMap[crew.id] || []" :key="crew.id + '-' + member.nickname"
                class="user-card">
                <strong>{{ member.nickname }} <span v-if="crew.creatorId == userId">🔸</span> </strong>
                <span>{{ formatDuration(member.duration) }}</span>
              </div>
            </div>
            <button class="talk-button" @click="goToChat(crew.id)">▶ 실시간 메신저</button>
          </div>
        </transition>
      </div>
      <div class="pagination-controls">
        <button @click="myprevPage" :disabled="mypage === 1">이전</button>
        <span>{{ mypage }} / {{ mytotalPages }}</span>
        <button @click="mynextPage" :disabled="mypage === mytotalPages">다음</button>
      </div>
    </div>

    <!-- 거리 및 정보 -->
    <!-- <div class="section">
      <div id="info">{{ infoText }}</div>
    </div> -->
    <!-- 추가된 네비게이션 메뉴 -->
    <div class="run-nav">
      <button class="nav-btn active" @click="stayOnTimer">개인 러닝</button>
      <button class="nav-btn" @click="navigateToRank">랭킹</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, defineEmits, nextTick, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import dogRun from '@/assets/dog_run.gif';
import dogSit from '@/assets/dog_sit.gif';
import html2canvas from "html2canvas";

const emit = defineEmits(['navigate']);
const router = useRouter();

// 토큰 관리 상수 및 유틸리티 함수
const TOKEN_KEY = "jwt";
const USER_ID_KEY = "userId";

const getStoredToken = () => localStorage.getItem(TOKEN_KEY);
const getStoredUserId = () => localStorage.getItem(USER_ID_KEY);
const removeStoredToken = () => localStorage.removeItem(TOKEN_KEY);
const removeStoredUserId = () => localStorage.removeItem(USER_ID_KEY);

// 상태 변수들
const searchQuery = ref('');
const seconds = ref(0);
const timer = ref(null);
const isRunning = ref(false);
const STORAGE_KEY = 'run-timer-start'
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
const token = ref(getStoredToken());
const userId = ref(Number(getStoredUserId()));
const showCrewForm = ref(false);
const crews = ref([]);
const crewMembers = ref([]);
const newCrew = ref({
  roomName: '',
  roomDescription: '',
  goalType: 'SUM',
  goalTime: 0,
  startDate: '',
  endDate: ''
});
const moodOptions = [
  { label: '기쁨', emoji: '😊' },
  { label: '뿌듯함', emoji: '💪' },
  { label: '아쉬움', emoji: '😕' },
  { label: '화남', emoji: '😠' },
  { label: '슬픔', emoji: '😢' },
  { label: '쏘쏘', emoji: '😐' }
];
const calories = ref(0);
const selectedMood = ref('');
const showMoodModal = ref(false);
const crewMembersMap = ref({});
const crewStatus = ref({});
const goalHours = ref(0);
const goalMinutes = ref(0);
const goalSeconds = ref(0);
const today = new Date().toISOString().split('T')[0];
const page = ref(1);
const perPage = 4;
const mypage = ref(1);
const myperPage = 4;
const openCrewId = ref(null);

/* 크루 목록 페이징 */
const paginatedCrews = computed(() => {
  const start = (page.value - 1) * perPage;
  return filteredCrews.value.slice(start, start + perPage);
});

const totalPages = computed(() =>
  Math.ceil(filteredCrews.value.length / perPage)
);

const nextPage = () => {
  if (page.value < totalPages.value) page.value++;
};
const prevPage = () => {
  if (page.value > 1) page.value--;
};

/* 내 크루 목록 페이징 */
const mypaginatedCrews = computed(() => {
  const start = (mypage.value - 1) * myperPage;
  return myCrews.value.slice(start, start + myperPage);
});

const mytotalPages = computed(() =>
  Math.ceil(myCrews.value.length / myperPage)
);

const mynextPage = () => {
  if (mypage.value < mytotalPages.value) mypage.value++;
};
const myprevPage = () => {
  if (mypage.value > 1) mypage.value--;
};

/* 크루 생성 */
const goalTime = computed(() => {
  return goalHours.value * 3600 + goalMinutes.value * 60 + goalSeconds.value
});

watch(goalTime, (newVal) => {
  newCrew.value.goalTime = newVal
})

const toggleCrewForm = () => {
  showCrewForm.value = !showCrewForm.value;
};

const submitCrew = async () => {
  if (!token.value) {
    alert("로그인이 필요합니다.");
    router.push('/login');
    return;
  }

  try {
    const response = await fetch('http://localhost:8080/api/crew', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token.value}`
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
        goalType: 'SUM',
        goalTime: goalTime.value,
        startDate: '',
        endDate: ''
      };
      // 크루 목록 새로고침
      await fetchCrewsAndMembers();
    }
  } catch (error) {
    console.error("크루 생성 중 오류:", error);
    alert("크루 생성 실패: 서버 오류");
  }
};

const joinCrew = async (crew) => {
  if (!token.value || !userId.value) {
    alert("로그인이 필요합니다.");
    router.push('/login');
    return;
  }

  try {
    const res = await fetch(`http://localhost:8080/api/crew/${crew.id}/join?userId=${userId.value}`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${token.value}`
      }
    });

    const message = await res.text();
    alert(message);

    if (res.ok) {
      // 크루 목록 새로고침
      await fetchCrewsAndMembers();
    }
  } catch (err) {
    console.error("크루 가입 중 오류:", err);
    alert("크루 가입 실패: 서버 오류");
  }
};

/* 크루 삭제, 탈퇴 */
const deleteCrew = async (crew) => {
  if (!token.value || !userId.value) {
    alert("로그인이 필요합니다.");
    router.push('/login');
    return;
  }

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

const silentDeleteCrew = async (crew) => {
  try {
    const res = await fetch(`http://localhost:8080/api/crew/${crew.id}?creatorId=${userId.value}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token.value}` }
    });

    if (res.ok) {
      console.log(`종료일 지난 크루 자동 삭제: ${crew.roomName}`);
    }
  } catch (err) {
    console.error("자동 크루 삭제 중 오류:", err);
  }
};


const quitCrew = async (crew) => {
  if (!token.value || !userId.value) {
    alert("로그인이 필요합니다.");
    router.push('/login');
    return;
  }

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
  if (!token.value) {
    console.warn("토큰이 없어 크루 목록을 가져올 수 없습니다.");
    return;
  }

  try {
    const res = await fetch("http://localhost:8080/api/crew", {
      headers: { Authorization: `Bearer ${token.value}` }
    });

    if (!res.ok) {
      if (res.status === 401) {
        handleTokenExpired();
        return;
      }
      throw new Error("크루 목록 불러오기 실패");
    }

    const data = await res.json();
    crews.value = data;

    const memberPromises = data.map(async crew => {
      try {
        const res = await fetch(`http://localhost:8080/api/crew/${crew.id}/members`, {
          headers: { Authorization: `Bearer ${token.value}` }
        });

        if (!res.ok) {
          console.warn(`멤버 불러오기 실패: crewId=${crew.id}`);
          return { crewId: crew.id, members: [] };
        }

        const members = await res.json();

        return { crewId: crew.id, members: members || [] };
      } catch (error) {
        console.error(`크루 ${crew.id} 멤버 불러오기 오류:`, error);
        return { crewId: crew.id, members: [] };
      }
    });

    const memberResults = await Promise.all(memberPromises);
    crewMembers.value = memberResults;

  } catch (error) {
    console.error("크루 또는 멤버 불러오는 중 오류:", error);
  }
};

const toggleCrew = async (id) => {
  if (expandedCrews.value.includes(id)) {
    expandedCrews.value = expandedCrews.value.filter(cid => cid !== id);
  } else {
    await getCrewRun(id);
    await getCrewGoal(id);
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

/* 토큰 만료 처리 */
const handleTokenExpired = () => {
  removeStoredToken();
  removeStoredUserId();
  token.value = null;
  userId.value = null;
  alert("로그인이 만료되었습니다. 다시 로그인해주세요.");
  router.push('/login');
};

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
  if (!token.value) {
    console.warn("토큰이 없어 이미지 업로드를 할 수 없습니다.");
    return;
  }

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
      if (res.status === 401) {
        handleTokenExpired();
        return;
      }
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
        } else if (res.status === 401) {
          handleTokenExpired();
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
  if (!token.value) {
    console.warn("토큰이 없어 러닝 데이터를 저장할 수 없습니다.");
    return;
  }

  try {
    const res = await fetch("http://localhost:8080/api/runs/track-location", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token.value}`
      },
      body: JSON.stringify({
        startTime: startTime.value,
        endTime: endTime.value,
        distance: (distance.value / 1000).toFixed(2)
      })
    });

    if (res.status === 401) {
      handleTokenExpired();
      return;
    }

    infoText.value = `러닝 완료! ${(distance.value / 1000).toFixed(2)}km를 ${formattedTime.value} 동안 달렸습니다.`;
  } catch (error) {
    console.error("러닝 데이터 저장 중 오류:", error);
  }
};

/* 타이머 기능 */
const toggleTimer = async () => {
  if (!token.value) {
    alert("로그인이 필요합니다.");
    return;
  }

  if (isRunning.value) {
    // 종료 분기
    try {
      endTime.value = new Date().toISOString();
      duration.value = seconds.value;

      if (!startTime.value) {
        startTime.value = localStorage.getItem(STORAGE_KEY);
      }

      const endJsonData = JSON.stringify({
        startTime: startTime.value,
        endTime: endTime.value,
        duration: duration.value,
        distance: (distance.value / 1000).toFixed(2),
        status: "ended"
      });

      const res = await fetch("http://localhost:8080/api/runs/running-status", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token.value}`
        },
        body: endJsonData
      });

      if (res.status === 401) {
        handleTokenExpired();
        return;
      }

      await saveRunningData();
      await uploadMapImage();
      infoText.value = "러닝이 종료되었습니다.";
      showMoodModal.value = true;
    } catch (error) {
      console.error("러닝 종료 요청 중 에러 발생:", error);
    } finally {
      clearInterval(timer.value);
      isRunning.value = false;
      localStorage.removeItem(STORAGE_KEY);
      startTime.value = null;
      seconds.value = 0;
    }
  } else {
    // 시작 분기
    try {
      startTime.value = new Date().toISOString();
      localStorage.setItem(STORAGE_KEY, startTime.value);

      const startJsonData = JSON.stringify({
        startTime: startTime.value,
        status: "running"
      });

      const res = await fetch("http://localhost:8080/api/runs/running-status", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token.value}`
        },
        body: startJsonData
      });

      if (res.status === 401) {
        handleTokenExpired();
        return;
      }

      timer.value = setInterval(() => {
        seconds.value++;
        if (kakaoMapLoaded.value) updateLocation();
      }, 1000);

      isRunning.value = true;
      infoText.value = "달리는 중...";
    } catch (error) {
      console.error("러닝 시작 요청 중 에러 발생:", error);
    }
  }
};


/* 유저 불러오기 */
const getCurrentUser = async () => {
  const currentToken = getStoredToken();
  const currentUserId = getStoredUserId();

  if (!currentToken) {
    alert("로그인이 필요합니다.");
    router.push('/login');
    return;
  }

  token.value = currentToken;
  userId.value = Number(currentUserId);

  try {
    const res = await fetch("http://localhost:8080/api/users/me", {
      headers: {
        Authorization: `Bearer ${currentToken}`
      }
    });

    if (!res.ok) {
      if (res.status === 401) {
        handleTokenExpired();
        return;
      }
      throw new Error("유저 정보 불러오기 실패");
    }

    const data = await res.json();
    userId.value = data.user.id;

  } catch (err) {
    console.error("사용자 정보 요청 실패:", err);
    alert("로그인이 필요합니다.");
    router.push('/login');
  }
};

/* daily record에 업데이트 */
const selectMood = async (mood) => {
  selectedMood.value = mood;
  showMoodModal.value = false;

  const routeId = await getRouteId();
  await fetchLatestRoute(routeId);
  await calDailyRun(routeId);
};

const fetchLatestRoute = async (routeId) => {
  try {
    const res = await fetch(`http://localhost:8080/api/my/route/${routeId}`, {
      headers: {
        Authorization: `Bearer ${token.value}`
      }
    });

    const data = await res.json();
    distance.value = data.distance;
    if (data.calories == null)
      data.calories = 0;
    calories.value = data.calories;

    // console.log("가장 최근 러닝 기록 불러오기 성공:", data);
  } catch (err) {
    console.error("최신 러닝 정보 조회 실패:", err);
  }
};

const getRouteId = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/my/route/latest-route-id`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token.value}`,
        'Content-Type': 'application/json'
      }
    });
    const data = await res.json();
    // console.log("route id 불러오기", data);
    return data.routeId;
  } catch (err) {
    console.error("route id 불러오기 실패:", err);
    return null;
  }
};

const calDailyRun = async (routeId) => {
  if (!token.value || !userId.value) {
    alert("로그인이 필요합니다.");
    router.push('/login');
    return;
  }

  try {
    const res = await fetch(`http://localhost:8080/api/runs/user/${userId.value}/day-time`, {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${token.value}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        routeId,
        distance: distance.value,
        calories: calories.value,
        mood: selectedMood.value
      })
    });

    const data = await res.json();
    // console.log("하루 러닝 시간 업데이트", data);
  } catch (err) {
    console.error("하루 러닝 시간 계산 업데이트 중 오류:", err);
  }
};

/* 목표 관련 */
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

const Percentage = (goal, now) => {
  if (!(goal || now)) return "0%";
  if (goal <= now)
    return `목표를 이미 달성했습니다.`;
  else {
    const per = (goal - now) / goal * 100;
    return `${per.toFixed(2)}%`;
  }
};

const getCrewRun = async (crewId) => {
  try {
    const res = await fetch(`http://localhost:8080/api/runs/crew/${crewId}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token.value}`
      }
    });

    if (!res.ok) throw new Error("서버 응답 오류");

    const data = await res.json();
    // console.log("크루 멤버 러닝 시간:", data);

    data.forEach(member => {
      if (member.duration == null) member.duration = 0;
    });

    // 여기서 객체에 직접 세팅
    crewMembersMap.value[crewId] = data;

  } catch (err) {
    console.error("getCrewRun 오류:", err);
  }
};

/* 목표 관련 2 */
const getCrewGoal = async (crewId) => {
  try {
    const res = await fetch(`http://localhost:8080/api/runs/crew/${crewId}/time`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token.value}`
      }
    });

    if (!res.ok) throw new Error("서버 응답 오류");

    const data = await res.json();
    // console.log("크루 시간 현황:", data);

    crewStatus.value = data;

  } catch (err) {
    console.error("크루 시간 현황:", err);
  }
};


const stayOnTimer = () => { };

function goToChat(crewId) {
  router.push(`/chat/${crewId}`);
}

const navigateToRank = () => emit('navigate', 'RunWithRank');

onMounted(async () => {
  getCurrentUser();
  loadKakaoMapScript();
  fetchCrewsAndMembers();

  // 종료일이 지난 크루 자동 삭제
  const now = new Date();
  for (const crew of crews.value) {
    const end = new Date(crew.endDate);
    end.setDate(end.getDate() + 1);

    if (end <= now && crew.creatorId == userId.value) {
      await silentDeleteCrew(crew);
    }
  }

  // 새로고침 후 타이머 복원
  const savedStart = localStorage.getItem(STORAGE_KEY);
  if (savedStart) {
    startTime.value = savedStart;
    seconds.value = Math.floor((new Date() - new Date(savedStart)) / 1000);
    isRunning.value = true;

    timer.value = setInterval(() => {
      seconds.value++;
      if (kakaoMapLoaded.value) updateLocation();
    }, 1000);
  }
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

.join-btn {
  background: linear-gradient(135deg, #FF9F69, #FF7043);
  color: white;
  padding: 6px 14px;
  margin-left: 4px;
  border: none;
  border-radius: 999px;
  font-weight: 600;
  font-size: 15px;
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

/* mood */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-box {
  background: white;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.mood-options {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
  margin-top: 16px;
}

.mood-btn {
  background: #f9f9f9;
  border: 2px solid #eee;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 1.1rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.mood-btn:hover {
  background: #ffe4b5;
}

.emoji {
  font-size: 1.5rem;
  display: block;
}

/* 내 크루 목록 CSS */
.crew-card {
  background: #fff7ee;
  border-radius: 18px;
  box-shadow: 0 2px 12px rgba(255, 181, 91, 0.08);
  margin-bottom: 20px;
  padding: 20px 18px 10px 18px;
  transition: box-shadow 0.2s;
  cursor: pointer;
  border: 1.5px solid #ffe0b2;
}

.crew-card:hover {
  box-shadow: 0 4px 24px rgba(255, 181, 91, 0.17);
}

.crew-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.crew-header h4 {
  font-size: 1.13em;
  color: #f57c00;
  margin: 0;
  font-weight: bold;
  letter-spacing: -1px;
}

.crew-header span {
  font-size: 0.95em;
  color: #b26a00;
  font-weight: 500;
}

.quit-btn,
.delete-btn {
  background: #ffe0b2;
  color: #f57c00;
  border: none;
  border-radius: 12px;
  padding: 4px 12px;
  margin-left: 8px;
  font-size: 0.93em;
  cursor: pointer;
  transition: background 0.15s;
}

.quit-btn:hover,
.delete-btn:hover {
  background: #ffd180;
}

.crew-detail {
  margin-top: 12px;
  font-size: 1em;
  color: #5d4037;
  background: #fffbe7;
  border-radius: 12px;
  padding: 16px 12px 8px 12px;
  border: 1px dashed #ffd180;
}

.crew-detail p {
  margin: 3px 0 7px 0;
  font-size: 1em;
}

.crew-detail strong {
  color: #ff9800;
}

.sub-title {
  font-size: 1.03em;
  font-weight: bold;
  color: #f57c00;
  margin: 16px 0 8px 0;
  letter-spacing: -0.5px;
}

.user-list {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-bottom: 10px;
}

.user-card {
  flex: 0 0 auto;
  background: #fff;
  border-radius: 9px;
  border: 1px solid #ffe0b2;
  padding: 7px 13px 5px 13px;
  font-size: 0.98em;
  color: #a05a00;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 74px;
  box-shadow: 0 1px 4px rgba(255, 181, 91, 0.08);
}

.user-card strong {
  font-size: 1em;
  color: #f57c00;
}

.user-card span {
  font-size: 0.96em;
  color: #6d4c1c;
  margin-top: 2px;
}

.talk-button {
  background: linear-gradient(90deg, #ffb74d 0%, #ff9800 100%);
  color: white;
  font-weight: bold;
  border: none;
  border-radius: 16px;
  padding: 9px 28px;
  font-size: 1.06em;
  margin-top: 10px;
  box-shadow: 0 2px 8px rgba(255, 181, 91, 0.13);
  cursor: pointer;
  transition: background 0.18s;
}

.talk-button:hover {
  background: linear-gradient(90deg, #ff9800 0%, #ffb74d 100%);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.more-btn-wrapper, .pagination-controls {
  text-align: center;
  margin-top: 12px;
}

.pagination-controls button {
  padding: 4px 8px;
  margin: 0px 8px;
  background-color: #f58220;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.progress-bar-container {
  margin-top: 8px;
  height: 20px;
  background: #f5f5f5;
  border-radius: 10px;
  overflow: hidden;
}

.progress-bar-bg {
  width: 100%;
  height: 100%;
  background: #eee;
  border-radius: 10px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background: linear-gradient(to right, #ffa500, #ff6347);
  transition: width 0.4s ease;
}

.progress-percent {
  margin-top: 4px;
  font-weight: bold;
  text-align: right;
  font-size: 14px;
}

.goal-details {
  margin-top: 10px;
  font-size: 14px;
}

.goal-celebration {
  background-color: #fff3cd;
  color: #d48806;
  border: 2px solid #ffe58f;
  border-radius: 8px;
  padding: 12px 20px;
  margin: 12px 0;
  font-weight: bold;
  font-size: 1.2em;
  text-align: center;
  animation: pop 0.5s ease-in-out;
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.5);
}

@keyframes pop {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

</style>