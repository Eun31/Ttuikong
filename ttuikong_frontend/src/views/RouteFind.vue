<template>
  <div class="app-container">
    <div id="header-container"></div>
    <h1 class="title">My 루트 찾기</h1>
    <p class="subtitle">선택한 루트의 예상 시간, 거리, 소모 칼로리를 알려줄게!</p>

    <div class="stats-container">
      <div class="stat-card">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="12" r="9" stroke="white" stroke-width="2" />
            <path d="M12 7V12L15 15" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">Time</span>
          <span class="stat-value">{{ walkTime }}</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M8 19L5 19V5L8 5" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            <path d="M16 5L19 5V19L16 19" stroke="white" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round" />
            <path d="M16 12L18 12" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            <path d="M8 12L6 12" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            <rect x="8" y="5" width="8" height="14" rx="2" stroke="white" stroke-width="2" />
            <path d="M12 5V19" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </div>
        <div class="stat-info">
          <span class="stat-label">Calories Burned</span>
          <span class="stat-value">{{ caloriesBurned }}</span>
        </div>
      </div>
    </div>

    <div class="address-input-box">
      <label for="start-address">출발지:</label>
      <div class="input-with-button">
        <input type="text" id="start-address" v-model="startAddress" placeholder="예: 서울특별시 중구 명동길 14">
        <button @click="searchStartAddress" :disabled="!startAddress || isSearching">검색</button>
      </div>
    </div>
    <div class="address-input-box">
      <label for="end-address">도착지:</label>
      <div class="input-with-button">
        <input type="text" id="end-address" v-model="endAddress" placeholder="예: 서울특별시 종로구 종로 1">
        <button @click="searchEndAddress" :disabled="!endAddress || isSearching">검색</button>
      </div>
    </div>

    <div class="map-wrapper">
      <div id="map_div"></div>

      <div class="distance-box">
        <span class="distance-label">Distance</span>
        <span class="distance-value">{{ formattedDistance }}</span>
      </div>

      <!-- 위치 정보 메시지 추가 -->
      <div v-if="isGettingLocation" class="location-message">
        <span class="location-icon">📍</span>
        <span>현재 위치를 불러오는 중...</span>
      </div>

      <div v-if="selectionState !== 'complete'" class="map-overlay">
        <span class="map-instruction" v-if="selectionState === 'start'">
          출발 지점을 선택하세요
        </span>
        <span class="map-instruction" v-else-if="selectionState === 'end'">
          도착 지점을 선택하세요
        </span>
      </div>
    </div>

    <div class="buttons-container">
      <button class="reset-btn" @click="resetSelection">경로 다시 선택</button>
      <button class="save-btn" :disabled="!routeCalculated" @click="saveRoute">경로 저장하기</button>
    </div>

    <div v-if="error" class="error-message">{{ error }}</div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';

const map = ref(null);
const error = ref(null);
const routePolyline = ref(null);
const totalDistance = ref(0);
const walkTime = ref('0분');
const caloriesBurned = ref('0 kcal');
const routeCalculated = ref(false);
const walkSpeed = 1.4;
const calorieRate = 0.8;
const averageWeight = 65;
const startAddress = ref('');
const endAddress = ref('');
const isSearching = ref(false);
const apiKey = 'mqrjE2sC0W8sX1UeeDEpO4k9dLeON1p01iT6Ianw';
const selectionState = ref('start');
const isGettingLocation = ref(false);

const markers = reactive({ start: null, end: null });
const coords = reactive({ start: null, end: null });

const formattedDistance = computed(() => {
  return totalDistance.value < 1000
    ? `${totalDistance.value}m`
    : `${(totalDistance.value / 1000).toFixed(1)}km`;
});

onMounted(async () => {
  try {
    await loadTmapScript();
    initMap();
  } catch (err) {
    error.value = "TMAP API 로드 실패: " + err;
  }
});

function loadTmapScript() {
  return new Promise((resolve, reject) => {
    if (window.Tmapv2) return resolve();

    const script = document.createElement('script');
    script.id = 'tmap-sdk';
    script.src = `https://apis.openapi.sk.com/tmap/jsv2?version=1&appKey=${apiKey}`;
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('TMAP API 로드 실패'));
    document.head.appendChild(script);
  });
}

function initMap() {
  if (typeof Tmapv2 === 'undefined') {
    error.value = "TMAP API가 로드되지 않았습니다.";
    return;
  }

  map.value = new Tmapv2.Map("map_div", {
    center: new Tmapv2.LatLng(37.566, 126.978),
    width: "100%",
    height: "300px",
    zoom: 14
  });

  map.value.addListener("click", handleMapClick);
  getCurrentLocation();
}

function getCurrentLocation() {
  if (!navigator.geolocation) return;
  isGettingLocation.value = true;

  navigator.geolocation.getCurrentPosition(
    (pos) => {
      isGettingLocation.value = false;
      const currentPos = new Tmapv2.LatLng(pos.coords.latitude, pos.coords.longitude);
      map.value.setCenter(currentPos);
      new Tmapv2.Marker({
        position: currentPos,
        icon: {
          url: '/assets/current-location.png',
          size: new Tmapv2.Size(24, 24),
          anchor: new Tmapv2.Point(12, 12)
        },
        map: map.value,
        title: '현재 위치'
      });
    },
    () => {
      isGettingLocation.value = false;
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
  );
}

function getAddressFromCoords(position, callback) {
  const apiUrl = `https://apis.openapi.sk.com/tmap/geo/reversegeocoding?version=1&format=json&appKey=${apiKey}&coordType=WGS84GEO&addressType=A10&lon=${position.lng()}&lat=${position.lat()}`;
  fetch(apiUrl)
    .then(res => res.json())
    .then(data => callback(data?.addressInfo?.fullAddress || ''))
    .catch(err => {
      console.error("주소 검색 오류:", err);
      callback(null);
    });
}

function setStartPosition(position) {
  if (markers.start) markers.start.setMap(null);
  markers.start = new Tmapv2.Marker({
    position,
    icon: { url: '/assets/location.png', size: new Tmapv2.Size(48, 48), anchor: new Tmapv2.Point(24, 48) },
    map: map.value,
    title: '출발지'
  });
  coords.start = position;
}

function setEndPosition(position) {
  if (markers.end) markers.end.setMap(null);
  markers.end = new Tmapv2.Marker({
    position,
    icon: { url: '/assets/destination.png', size: new Tmapv2.Size(48, 48), anchor: new Tmapv2.Point(24, 48) },
    map: map.value,
    title: '도착지'
  });
  coords.end = position;
}

function calculateRoute() {
  if (routePolyline.value) routePolyline.value.setMap(null);

  fetch("https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&format=json", {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      'appKey': apiKey
    },
    body: JSON.stringify({
      startX: coords.start.lng(), startY: coords.start.lat(),
      endX: coords.end.lng(), endY: coords.end.lat(),
      reqCoordType: "WGS84GEO",
      resCoordType: "WGS84GEO",
      startName: "출발지", endName: "도착지"
    })
  })
    .then(res => res.json())
    .then(data => {
      totalDistance.value = data.features[0].properties.totalDistance;
      const totalTime = data.features[0].properties.totalTime;
      walkTime.value = formatTime(totalTime);
      caloriesBurned.value = `${Math.round(totalDistance.value / 1000 * calorieRate * averageWeight)} kcal`;

      const lineArray = data.features.flatMap(f =>
        f.geometry.type === "LineString"
          ? f.geometry.coordinates.map(([lng, lat]) => new Tmapv2.LatLng(lat, lng))
          : []
      );

      routePolyline.value = new Tmapv2.Polyline({
        path: lineArray,
        strokeColor: "#FF7043",
        strokeWeight: 8,
        map: map.value
      });

      routeCalculated.value = true;

      const bounds = new Tmapv2.LatLngBounds();
      lineArray.forEach(pos => bounds.extend(pos));
      map.value.fitBounds(bounds);
    })
    .catch(err => {
      error.value = "경로 계산 API 오류: " + err.message;
    });
}

function formatTime(seconds) {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  return h ? `${h}시간 ${m}분` : `${m}분`;
}

function resetSelection() {
  selectionState.value = 'start';
  if (markers.start) markers.start.setMap(null);
  if (markers.end) markers.end.setMap(null);
  markers.start = markers.end = null;
  coords.start = coords.end = null;
  if (routePolyline.value) routePolyline.value.setMap(null);
  totalDistance.value = 0;
  walkTime.value = '0분';
  caloriesBurned.value = '0 kcal';
  routeCalculated.value = false;
  startAddress.value = '';
  endAddress.value = '';
  error.value = null;
  map.value?.setCenter(new Tmapv2.LatLng(37.566, 126.978));
  map.value?.setZoom(14);
}

function searchStartAddress() {
  if (!startAddress.value || isSearching.value) return;
  isSearching.value = true;
  error.value = null;

  const url = `https://apis.openapi.sk.com/tmap/pois?version=1&format=json&callback=result&appKey=${apiKey}&searchKeyword=${encodeURIComponent(startAddress.value)}`;
  fetch(url)
    .then(res => res.json())
    .then(data => {
      isSearching.value = false;
      const poi = data.searchPoiInfo?.pois?.poi?.[0];
      if (poi) {
        const pos = new Tmapv2.LatLng(poi.frontLat, poi.frontLon);
        setStartPosition(pos);
        map.value.setCenter(pos);
        map.value.setZoom(16);
        if (selectionState.value === 'start') selectionState.value = 'end';
        if (coords.start && coords.end) calculateRoute();
      } else {
        error.value = '검색 결과가 없습니다.';
      }
    })
    .catch(err => {
      isSearching.value = false;
      error.value = '주소 검색 오류: ' + err.message;
    });
}

function searchEndAddress() {
  if (!endAddress.value || isSearching.value) return;
  isSearching.value = true;
  error.value = null;

  const url = `https://apis.openapi.sk.com/tmap/pois?version=1&format=json&callback=result&appKey=${apiKey}&searchKeyword=${encodeURIComponent(endAddress.value)}`;
  fetch(url)
    .then(res => res.json())
    .then(data => {
      isSearching.value = false;
      const poi = data.searchPoiInfo?.pois?.poi?.[0];
      if (poi) {
        const pos = new Tmapv2.LatLng(poi.frontLat, poi.frontLon);
        setEndPosition(pos);
        map.value.setCenter(pos);
        map.value.setZoom(16);
        if (selectionState.value === 'end') selectionState.value = 'complete';
        if (coords.start && coords.end) calculateRoute();
      } else {
        error.value = '검색 결과가 없습니다.';
      }
    })
    .catch(err => {
      isSearching.value = false;
      error.value = '주소 검색 오류: ' + err.message;
    });
}

function saveRoute() {
  alert('경로가 저장되었습니다!');
}

function handleMapClick(evt) {
  const pos = evt.latLng;
  if (selectionState.value === 'start') {
    setStartPosition(pos);
    selectionState.value = 'end';
  } else if (selectionState.value === 'end') {
    setEndPosition(pos);
    selectionState.value = 'complete';
  }
  if (coords.start && coords.end) calculateRoute();
}
</script>


<style scoped>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  font-family: 'Noto Sans KR', sans-serif;
}

html,
body {
  background-color: #FFF8F2;
  /* 홈 화면과 동일한 배경색 */
  color: #333;
  margin: 0;
  padding: 0;
  overflow-x: hidden;
  /* 가로 스크롤 방지 */
  width: 100%;
  height: 100%;
  min-height: 100vh;
}

.app-container {
  width: 100%;
  /* 전체 너비로 확장 */
  margin: 0 auto;
  min-height: 100vh;
  background-color: #FFF8F2;
  /* 홈 화면과 동일한 배경색 */
  position: relative;
  padding-bottom: 30px;
  padding-top: 20px;
  /* 상단에 여백 추가 */
}

.header {
  background-color: #FFF3ED;
  /* 홈 화면과 동일한 색상 계열 */
  padding: 0.8rem 1.5rem;
  display: flex;
  justify-content: center;
  /* 중앙 정렬 */
  align-items: center;
  box-shadow: 0 2px 10px rgba(255, 112, 67, 0.09);
  border-bottom: 1.5px solid #FFE5D5;
  width: 100%;
  /* 전체 너비 */
}

/* 헤더 내부 콘텐츠 추가 */
.header-content {
  width: 100%;
  max-width: 440px;
  /* 모바일 너비로 제한 */
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 메인 콘텐츠 컨테이너 */
.content-container {
  max-width: 440px;
  /* 모바일 너비로 제한 */
  margin: 0 auto;
  width: 100%;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo {
  width: 32px;
  height: 32px;
}

.app-name {
  font-size: 14px;
  color: #4a4a4a;
}

.my-page-btn {
  background-color: #FF7043;
  /* 홈 화면과 동일한 포인트 색상 */
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
}

.title {
  font-size: 28px;
  font-weight: 700;
  margin: 20px 16px 8px 16px;
  /* 상단 마진 조정 */
  color: #333;
  text-align: center;
  position: relative;
  padding-bottom: 12px;
}

.subtitle {
  font-size: 16px;
  color: #666;
  margin: 0 16px 16px 16px;
  text-align: center;
  /* 홈 화면과 동일하게 중앙 정렬 */
}

.stats-container {
  display: flex;
  margin: 0 16px 16px 16px;
  gap: 12px;
}

.stat-card {
  background-color: #FFF3ED;
  /* 홈 화면과 동일한 배경색 */
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  flex: 1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 40px;
  height: 40px;
  background-color: rgba(255, 112, 67, 0.2);
  /* 홈 화면 테마에 맞게 변경 */
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.stat-info {
  display: flex;
  flex-direction: column;
  color: #333;
  /* 홈 화면과 일치하게 변경 */
}

.stat-label {
  font-size: 14px;
  color: #555;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
}

/* 주소 입력 컨테이너 스타일 */
.address-input-container {
  margin: 0 16px 16px 16px;
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  width: calc(100% - 32px);
}

/* 주소 입력 박스 스타일 */
.address-input-box {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  justify-content: flex-start;
  margin-left: 16px;
  margin-right: 16px;
}

.address-input-box:last-child {
  margin-bottom: 0;
}

/* 라벨 스타일 */
.address-input-box label {
  width: 40px;
  /* 45px에서 40px로 줄임 */
  font-size: 13px;
  /* 14px에서 13px로 줄임 */
  color: #333;
  font-weight: 500;
  margin-right: 8px;
  white-space: nowrap;
  /* 줄바꿈 방지 */
}

/* 입력 필드와 버튼 정렬 */
.input-with-button {
  display: flex;
  gap: 8px;
  width: 100%;
  height: 48px;
  /* 더 높은 입력 필드 */
}

/* 입력 필드 스타일 */
.input-with-button input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
}

.input-with-button input:focus {
  border-color: #FF7043;
  /* 홈 화면과 동일한 포인트 색상 */
}

/* 버튼 스타일 */
.input-with-button button {
  min-width: 60px;
  padding: 0 16px;
  background-color: #FF7043;
  /* 홈 화면과 동일한 포인트 색상 */
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  font-size: 14px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.input-with-button button:hover {
  background-color: #FF5722;
}

.input-with-button button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.map-wrapper {
  margin: 0 16px;
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  background-color: #FFF3ED;
  /* 홈 화면과 동일한 배경색 */
  padding: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

#map_div {
  width: 100%;
  height: 300px;
  border-radius: 12px;
  background-color: #e7e7e7;
  position: relative;
}

.distance-box {
  position: absolute;
  top: 16px;
  right: 16px;
  background-color: white;
  border-radius: 8px;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.distance-label {
  font-size: 12px;
  color: #888;
}

.distance-value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.buttons-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 24px 16px 16px 16px;
}

.save-btn {
  display: block;
  width: 100%;
  background-color: #FF7043;
  /* 홈 화면과 동일한 포인트 색상 */
  color: white;
  border: none;
  border-radius: 12px;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.save-btn:hover:not(:disabled) {
  background-color: #FF5722;
  transform: translateY(-2px);
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
}

.save-btn:disabled {
  background-color: #FFB8A3;
  cursor: not-allowed;
  opacity: 0.7;
}

.reset-btn {
  display: block;
  width: 100%;
  background-color: #F0F0F0;
  color: #333;
  border: none;
  border-radius: 12px;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.reset-btn:hover {
  background-color: #E0E0E0;
  transform: translateY(-2px);
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
}

.map-overlay {
  position: absolute;
  top: 16px;
  left: 16px;
  background-color: white;
  padding: 12px 20px;
  border-radius: 50px;
  z-index: 10;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
}

.map-instruction {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.error-message {
  margin: 10px 16px;
  padding: 16px;
  background-color: #FFEBEE;
  color: #D32F2F;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
  box-shadow: 0 2px 6px rgba(211, 47, 47, 0.1);
}

/* 위치 정보 메시지 스타일 */
.location-message {
  position: absolute;
  top: 16px;
  left: 16px;
  background-color: rgba(255, 255, 255, 0.9);
  padding: 8px 16px;
  border-radius: 20px;
  z-index: 10;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  font-size: 14px;
  color: #333;
  display: flex;
  align-items: center;
  max-width: 80%;
}

.location-icon {
  color: #FF7043;
  margin-right: 8px;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    opacity: 0.6;
  }

  50% {
    opacity: 1;
  }

  100% {
    opacity: 0.6;
  }
}

@media (max-width: 400px) {
  .input-with-button input {
    padding: 12px 8px;
    /* 좌우 패딩 줄임 */
  }

  .input-with-button button {
    min-width: 48px;
    /* 버튼 너비 줄임 */
    padding: 0 8px;
    /* 패딩 줄임 */
    font-size: 13px;
    /* 폰트 크기 줄임 */
  }

  /* 주소 입력 박스 여백 줄임 */
  .address-input-box {
    margin-left: 12px;
    margin-right: 12px;
  }
}

/* 더 작은 화면을 위한 스타일 */
@media (max-width: 340px) {
  .address-input-box label {
    font-size: 12px;
    width: 36px;
  }

  .input-with-button button {
    min-width: 40px;
    font-size: 12px;
  }
}
</style>