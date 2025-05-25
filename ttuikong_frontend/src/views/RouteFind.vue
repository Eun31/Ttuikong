<template>
    <div class="app-container">
      <div class="header-content">
        <div class="header-title-section">
          <h1 class="header-title">My 루트 찾기</h1>
          <p class="header-subtitle">코스 정보를 미리 확인하고 달려요!</p>
        </div>
    </div>

    <div v-if="showFeedback && feedbackMessage" class="feedback-card" :class="feedbackType">
      <div class="feedback-content">
        <div class="feedback-icon">{{ feedbackIcon }}</div>
        <div class="feedback-text">{{ feedbackMessage }}</div>
      </div>
    </div>

    <div class="stats-container">
      <div class="stat-card">
        <div class="stat-icon">
          🕒
        </div>
        <div class="stat-info">
          <span class="stat-label">Time</span>
          <span class="stat-value">{{ walkTime }}</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon">
          🔥
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
<div class="buttons-container">
  <button class="reset-btn" @click="getCurrentLocationAsStart">
    🏃‍♀️ 현재 위치에서 시작
  </button>
      <button class="reset-btn" @click="resetSelection">경로 다시 선택</button>
    </div>

    <div class="map-wrapper">
      <div id="map_div"></div>

      <div class="distance-box">
        <span class="distance-label">Distance</span>
        <span class="distance-value">{{ formattedDistance }}</span>
      </div>

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
    <div v-if="error" class="error-message">{{ error }}</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'

const map = ref(null)
const error = ref(null)
const markers = ref({
  start: null,
  end: null
})
const coords = ref({
  start: null,
  end: null
})
const routePolyline = ref(null)
const selectionState = ref('start') 
const totalDistance = ref(0)
const walkTime = ref('0분')
const caloriesBurned = ref('0 kcal')
const routeCalculated = ref(false)

const token = ref('')
const userId = ref(null)
const isLoadingRecommendation = ref(false)
const recommendationData = ref(null)

const showFeedback = ref(false)
const feedbackMessage = ref('')
const feedbackType = ref('')
const feedbackIcon = ref('')
const calorieRate = ref(1.2)
const averageWeight = ref(65)

const startAddress = ref('')
const endAddress = ref('')
const isSearching = ref(false)
const isGettingLocation = ref(false)
const apiKey = 'mqrjE2sC0W8sX1UeeDEpO4k9dLeON1p01iT6Ianw'

const formattedDistance = computed(() => {
  if (totalDistance.value < 1000) {
    return `${totalDistance.value}m`
  } else {
    return `${(totalDistance.value / 1000).toFixed(1)}km`
  }
})

const goBack = () => {
  window.history.back()
}

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

  } catch (err) {
    console.error('사용자 정보 요청 실패:', err);
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
    }
  } catch (error) {
    console.error('AI 추천 로드 실패:', error);
  } finally {
    isLoadingRecommendation.value = false;
  }
};

const checkDistanceAndProvideFeedback = () => {
  if (!routeCalculated.value || !recommendationData.value) return;

  const selectedDistanceKm = totalDistance.value / 1000;
  const recommendedDistanceKm = recommendationData.value.recommendedDistance;
  const difference = Math.abs(selectedDistanceKm - recommendedDistanceKm);
  const percentageDiff = (difference / recommendedDistanceKm) * 100;

  showFeedback.value = true;

  if (percentageDiff <= 10) {
    feedbackType.value = 'good';
    feedbackIcon.value = '🎯';
    feedbackMessage.value = `완벽해요! AI 추천과 거의 일치하는 거리입니다. (추천: ${recommendedDistanceKm}km, 선택: ${selectedDistanceKm.toFixed(1)}km)`;
  } else if (percentageDiff <= 25) {
    feedbackType.value = 'good';
    feedbackIcon.value = '👍';
    feedbackMessage.value = `좋은 선택이에요! AI 추천과 비슷한 거리네요. (추천: ${recommendedDistanceKm}km, 선택: ${selectedDistanceKm.toFixed(1)}km)`;
  } else if (selectedDistanceKm < recommendedDistanceKm) {
    feedbackType.value = 'info';
    feedbackIcon.value = '🚶‍♀️';
    feedbackMessage.value = `가벼운 산책이 되겠네요! 좀 더 도전해보는 건 어떨까요? (추천: ${recommendedDistanceKm}km, 선택: ${selectedDistanceKm.toFixed(1)}km)`;
  } else {
    feedbackType.value = 'warning';
    feedbackIcon.value = '💪';
    feedbackMessage.value = `도전적이네요! 무리하지 마시고 천천히 가세요. (추천: ${recommendedDistanceKm}km, 선택: ${selectedDistanceKm.toFixed(1)}km)`;
  }

  setTimeout(() => {
    showFeedback.value = false;
  }, 3000);
};

watch(routeCalculated, (newValue) => {
  if (newValue) {
    setTimeout(() => {
      checkDistanceAndProvideFeedback();
    }, 1000); 
  }
});


const loadTmapScript = () => {
  return new Promise((resolve, reject) => {
    if (window.Tmapv2) {
      resolve()
      return
    }

    const script = document.createElement('script')
    script.src = `https://apis.openapi.sk.com/tmap/jsv2?version=1&appKey=${apiKey}`
    script.async = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('TMAP API 로드 실패'))
    document.head.appendChild(script)
  })
}

const initMap = () => {
  try {
    if (typeof Tmapv2 === 'undefined') {
      error.value = "TMAP API가 로드되지 않았습니다. API 키를 확인해주세요."
      return
    }

    map.value = new Tmapv2.Map("map_div", {
      center: new Tmapv2.LatLng(37.566, 126.978), // 기본 위치는 서울시청
      width: "100%",
      height: "300px",
      zoom: 14
    })

    map.value.addListener("click", handleMapClick)
    map.value.addListener("touchend", handleMapTouch)
    console.log("지도 클릭 리스너 등록됨")

    getCurrentLocation()

  } catch (e) {
    error.value = "지도 초기화 오류: " + e.message
    console.error("지도 초기화 오류:", e)
  }
}

const handleMapClick = (e) => {
  const clickedPosition = e.latLng
  console.log("지도 클릭 위치:", clickedPosition.toString())
  if (selectionState.value === 'start') {
    setStartPosition(clickedPosition)

    getAddressFromCoords(clickedPosition, (address) => {
      if (address) {
        startAddress.value = address
        console.log("출발지 주소 설정:", address)
      }
    })

    selectionState.value = 'end'
  } else if (selectionState.value === 'end') {
    setEndPosition(clickedPosition)

    getAddressFromCoords(clickedPosition, (address) => {
      if (address) {
        endAddress.value = address
        console.log("도착지 주소 설정:", address)
      }
    })

    selectionState.value = 'complete'
    calculateRoute()
  }
}

const handleMapTouch = (e) => {
  console.log("터치 이벤트 감지:", e)
  handleMapClick(e)
}

const getCurrentLocation = () => {
  isGettingLocation.value = true
  
  // 브라우저가 Geolocation API를 지원하는지 확인
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        isGettingLocation.value = false
        const currentPos = new Tmapv2.LatLng(
          position.coords.latitude,
          position.coords.longitude
        )
        console.log("현재 위치:", currentPos.toString())
        map.value.setCenter(currentPos)
        
        getAddressFromCoords(currentPos, (address) => {
          console.log("현재 위치 주소:", address)
        })
      },
      (error) => {
        isGettingLocation.value = false
        console.error("위치 정보를 가져오는데 실패했습니다:", error)
        let errorMessage = ""
        
        switch(error.code) {
          case error.PERMISSION_DENIED:
            errorMessage = "위치 정보 접근이 거부되었습니다. 위치 권한을 허용해주세요."
            break
          case error.POSITION_UNAVAILABLE:
            errorMessage = "위치 정보를 사용할 수 없습니다."
            break
          case error.TIMEOUT:
            errorMessage = "위치 정보 요청 시간이 초과되었습니다."
            break
          default:
            errorMessage = "알 수 없는 오류가 발생했습니다."
            break
        }
      
        console.warn(errorMessage)
      }
    )
  } else {
    isGettingLocation.value = false
    console.error("이 브라우저는 Geolocation을 지원하지 않습니다.")
  }
}

const getAddressFromCoords = (position, callback) => {
  const apiUrl = "https://apis.openapi.sk.com/tmap/geo/reversegeocoding?version=1&format=json"
  
  const reqParams = {
    appKey: apiKey,
    coordType: "WGS84GEO",
    addressType: "A10",
    lon: position.lng(),
    lat: position.lat()
  }
  
  const queryString = Object.keys(reqParams)
    .map(key => `${key}=${encodeURIComponent(reqParams[key])}`)
    .join('&')
  
  fetch(`${apiUrl}&${queryString}`)
    .then(response => response.json())
    .then(data => {
      let address = ''
      if (data && data.addressInfo) {
        const info = data.addressInfo
        address = `${info.fullAddress}`
      }
      callback(address)
    })
    .catch(e => {
      console.error("주소 검색 오류:", e)
      callback(null)
    })
}

const setStartPosition = (position) => {
  coords.value.start = position
}

const setEndPosition = (position) => {
  coords.value.end = position
}

const calculateRoute = () => {
  try {
    if (routePolyline.value) {
      routePolyline.value.setMap(null)
    }
    const apiUrl = "https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&format=json"
    const params = {
      startX: coords.value.start.lng(),
      startY: coords.value.start.lat(),
      endX: coords.value.end.lng(),
      endY: coords.value.end.lat(),
      reqCoordType: "WGS84GEO",
      resCoordType: "WGS84GEO",
      startName: encodeURIComponent("출발지"),
      endName: encodeURIComponent("도착지")
    }

    fetch(apiUrl, {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
        'appKey': apiKey
      },
      body: JSON.stringify(params)
    })
    .then(response => response.json())
    .then(data => {
      if (data.features && data.features.length > 0) {
        totalDistance.value = data.features[0].properties.totalDistance
        const totalTimeSeconds = data.features[0].properties.totalTime
        walkTime.value = formatTimeFromSeconds(totalTimeSeconds)
        const calories = Math.round(totalDistance.value / 1000 * calorieRate.value * averageWeight.value)
        caloriesBurned.value = `${calories} kcal`
        let lineArray = []

        for (let i in data.features) {
          const feature = data.features[i]
          
          if (feature.geometry.type === "LineString") {
            for (let j in feature.geometry.coordinates) {
              const coordinates = feature.geometry.coordinates[j]
              const convertedCoords = new Tmapv2.LatLng(coordinates[1], coordinates[0])
              lineArray.push(convertedCoords)
            }
          }
        }
        
        // 경로 그리기
        routePolyline.value = new Tmapv2.Polyline({
          path: lineArray,
          strokeColor: "#FF7043",
          strokeWeight: 8,
          map: map.value
        })
        
        routeCalculated.value = true
        const bounds = new Tmapv2.LatLngBounds()
        for (let i in lineArray) {
          bounds.extend(lineArray[i])
        }
        map.value.fitBounds(bounds)
      } else {
        error.value = "경로를 찾을 수 없습니다."
      }
    })
    .catch(e => {
      error.value = "경로 계산 API 오류: " + e.message
      console.error("경로 계산 API 오류:", e)
    })
  } catch (e) {
    error.value = "경로 계산 오류: " + e.message
    console.error("경로 계산 오류:", e)
  }
}

const formatTimeFromSeconds = (seconds) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)

  if (hours > 0) {
    return `${hours}시간 ${minutes}분`
  } else {
    return `${minutes}분`
  }
}

// 경로 다시 선택 기능
const resetSelection = () => {
  // 선택 상태 초기화
  selectionState.value = 'start'

  // 좌표 초기화
  coords.value.start = null
  coords.value.end = null
  
  // 경로 제거
  if (routePolyline.value) {
    routePolyline.value.setMap(null)
    routePolyline.value = null
  }
  
  // 거리, 시간, 칼로리 초기화
  totalDistance.value = 0
  walkTime.value = '0분'
  caloriesBurned.value = '0 kcal'
  routeCalculated.value = false
  
  // 주소 입력 초기화
  startAddress.value = ''
  endAddress.value = ''
  
  // 에러 메시지 초기화
  error.value = null
  
  // 피드백 숨기기
  showFeedback.value = false
}

// 현재 위치를 출발지로 설정하는 새 함수 추가
const getCurrentLocationAsStart = () => {
  isGettingLocation.value = true
  
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        isGettingLocation.value = false
        const currentPos = new Tmapv2.LatLng(
          position.coords.latitude,
          position.coords.longitude
        )
        
        console.log("현재 위치를 출발지로 설정:", currentPos.toString())
        
        // 현재 위치를 출발지로 설정
        setStartPosition(currentPos)
        
        // 지도 중심을 현재 위치로 이동
        if (map.value) {
          map.value.setCenter(currentPos)
          map.value.setZoom(16)
        }
        
        // 현재 위치 주소를 출발지 주소로 설정
        getAddressFromCoords(currentPos, (address) => {
          if (address) {
            startAddress.value = address
            console.log("출발지로 설정된 현재 위치:", address)
          }
        })
        
        // 출발지가 설정되었으므로 도착지 선택 모드로 전환
        selectionState.value = 'end'
        
      },
      (error) => {
        isGettingLocation.value = false
        console.error("위치 정보를 가져오는데 실패했습니다:", error)
        
        let errorMessage = ""
        switch(error.code) {
          case error.PERMISSION_DENIED:
            errorMessage = "위치 정보 접근이 거부되었습니다. 위치 권한을 허용해주세요."
            break
          case error.POSITION_UNAVAILABLE:
            errorMessage = "위치 정보를 사용할 수 없습니다."
            break
          case error.TIMEOUT:
            errorMessage = "위치 정보 요청 시간이 초과되었습니다."
            break
          default:
            errorMessage = "알 수 없는 오류가 발생했습니다."
            break
        }
        
        console.warn(errorMessage)
        error.value = errorMessage
        
        // 위치 정보를 가져올 수 없는 경우 기본 위치(서울시청)로 설정
        if (map.value) {
          map.value.setCenter(new Tmapv2.LatLng(37.566, 126.978))
          map.value.setZoom(14)
        }
      },
      {
        enableHighAccuracy: true, // 높은 정확도 요청
        timeout: 10000,          // 10초 타임아웃
        maximumAge: 60000        // 1분간 캐시된 위치 정보 사용
      }
    )
  } else {
    isGettingLocation.value = false
    console.error("이 브라우저는 Geolocation을 지원하지 않습니다.")
    error.value = "이 브라우저는 위치 서비스를 지원하지 않습니다."
    
    // Geolocation을 지원하지 않는 경우 기본 위치로 설정
    if (map.value) {
      map.value.setCenter(new Tmapv2.LatLng(37.566, 126.978))
      map.value.setZoom(14)
    }
  }
}

// 주소로 검색하는 기능 - 출발지
const searchStartAddress = () => {
  if (!startAddress.value || isSearching.value) return
  
  isSearching.value = true
  error.value = null
  
  const apiUrl = "https://apis.openapi.sk.com/tmap/pois?version=1&format=json&callback=result"
  
  fetch(`${apiUrl}&appKey=${apiKey}&searchKeyword=${encodeURIComponent(startAddress.value)}`)
    .then(response => response.json())
    .then(data => {
      isSearching.value = false
      
      if (data.searchPoiInfo && data.searchPoiInfo.pois && data.searchPoiInfo.pois.poi && data.searchPoiInfo.pois.poi.length > 0) {
        // 첫 번째 검색 결과 사용
        const poi = data.searchPoiInfo.pois.poi[0]
        const position = new Tmapv2.LatLng(poi.frontLat, poi.frontLon)
        
        // 출발지 설정
        setStartPosition(position)
        
        // 지도 이동
        map.value.setCenter(position)
        map.value.setZoom(16)
        
        // 다음 단계로 진행
        if (selectionState.value === 'start') {
          selectionState.value = 'end'
          console.log("출발지 주소 설정 완료, 도착지 선택 모드로 전환")
        }
        
        // 출발지와 도착지가 모두 설정되면 경로 계산
        if (coords.value.start && coords.value.end) {
          calculateRoute()
        }
      } else {
        error.value = "검색 결과가 없습니다. 주소를 다시 확인해주세요."
      }
    })
    .catch(e => {
      isSearching.value = false
      error.value = "주소 검색 중 오류가 발생했습니다: " + e.message
      console.error("주소 검색 오류:", e)
    })
}

// 주소로 검색하는 기능 - 도착지
const searchEndAddress = () => {
  if (!endAddress.value || isSearching.value) return
  
  isSearching.value = true
  error.value = null
  
  const apiUrl = "https://apis.openapi.sk.com/tmap/pois?version=1&format=json&callback=result"
  
  fetch(`${apiUrl}&appKey=${apiKey}&searchKeyword=${encodeURIComponent(endAddress.value)}`)
    .then(response => response.json())
    .then(data => {
      isSearching.value = false
      
      if (data.searchPoiInfo && data.searchPoiInfo.pois && data.searchPoiInfo.pois.poi && data.searchPoiInfo.pois.poi.length > 0) {
        // 첫 번째 검색 결과 사용
        const poi = data.searchPoiInfo.pois.poi[0]
        const position = new Tmapv2.LatLng(poi.frontLat, poi.frontLon)
        
        // 도착지 설정
        setEndPosition(position)
        
        // 지도 이동
        map.value.setCenter(position)
        map.value.setZoom(16)
        
        // 다음 단계로 진행
        if (selectionState.value === 'end') {
          selectionState.value = 'complete'
          console.log("도착지 주소 설정 완료")
        }
        
        // 출발지와 도착지가 모두 설정되면 경로 계산
        if (coords.value.start && coords.value.end) {
          calculateRoute()
        }
      } else {
        error.value = "검색 결과가 없습니다. 주소를 다시 확인해주세요."
      }
    })
    .catch(e => {
      isSearching.value = false
      error.value = "주소 검색 중 오류가 발생했습니다: " + e.message
      console.error("주소 검색 오류:", e)
    })
}

onMounted(async () => {
  await getCurrentUser();
  await getAIRecommendation();
  loadTmapScript().then(() => {
    initMap()
  }).catch(err => {
    error.value = "TMAP API 로드 실패: " + err
  })
})
</script>

<style scoped>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  font-family: 'Pretendard', 'Noto Sans KR', sans-serif;
}

html, body {
  background-color: #FFF8F2;
  color: #333;
  margin: 0;
  padding: 0;
  overflow-x: hidden;
  width: 100%;
  height: 100%;
  min-height: 100vh;
}

.app-container {
  width: 100%;
  margin: 0 auto;
  min-height: 100vh;
  background-color: #FFF8F2;
  position: relative;
  padding-bottom: 30px;
  padding-top: 20px;
}

.header-content {
  width: 100%;
  max-width: 440px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  padding: 16px;
  position: relative;
  margin-bottom: 20px;
}

.header-title-section {
  margin-top: 5px;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  width: calc(100% - 120px); /* 양쪽 버튼 공간 확보 */
}

.header-title {
  font-size: 24px;
  font-weight: 700;
  color: #FF7043;
  margin: 0 0 4px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-subtitle {
  font-size: 14px;
  color: #726f6f;
  margin: 0;
  font-weight: 500;
}

/* 피드백 카드 스타일 */
.feedback-card {
  margin: 0 16px 20px 16px;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
  animation: slideDown 0.5s ease-out;
}

.feedback-card.good {
  background: linear-gradient(135deg, #FF7043, #FF5722);
  color: white;
}

.feedback-card.warning {
  background: linear-gradient(135deg, #FFB74D, #FF9800);
  color: white;
}

.feedback-card.info {
  background: linear-gradient(135deg, #FFE0B2, #FFCC80);
  color: #5D4037;
}

.feedback-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.feedback-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.feedback-text {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header {
  background-color: #FFF3ED;
  padding: 0.8rem 1.5rem;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 10px rgba(255, 112, 67, 0.09);
  border-bottom: 1.5px solid #FFE5D5;
  width: 100%;
}

.content-container {
  max-width: 440px;
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
  margin: 20px 0 8px 0;
  color: #FF7043;
  text-align: center;
  position: relative;
  padding: 0 16px;
  width: 100%;
}

.subtitle {
  font-size: 16px;
  color: #FF5722;
  margin: 0 0 16px 0;
  text-align: center;
  padding: 0 16px;
  width: 100%;
}

.stats-container {
  display: flex;
  margin: 0 16px 16px 16px;
  gap: 12px;
}

.stat-card {
  background-color: #FFF3ED;
  border-radius: 12px;
  padding: 16px;
  margin-top: 0px;
  display: flex;
  align-items: center;
  flex: 1;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.stat-icon {
  width: 40px;
  height: 40px;
  background-color: rgba(255, 112, 67, 0.2);
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
}

.stat-label {
  font-size: 14px;
  color: #555;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
}

.address-input-container {
  margin: 0 16px 16px 16px;
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  width: calc(100% - 32px);
}

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

.address-input-box label {
  width: 40px;
  font-size: 13px;
  color: #333;
  font-weight: 500;
  margin-right: 8px;
  white-space: nowrap;
}

.input-with-button {
  display: flex;
  gap: 8px;
  width: 100%;
  height: 48px;
}

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
}

.input-with-button button {
  min-width: 60px;
  padding: 0 16px;
  background-color: #FF7043;
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
  padding: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

#map_div {
  width: 100%;
  height: 300px;
  border-radius: 12px;
  background-color: #e7e7e7;
  position: relative;
  cursor: pointer;
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
  gap: 12px;
  margin: 24px 16px 16px 16px;
}

.save-btn {
  display: block;
  width: 100%;
  background-color: #FF7043;
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
  box-shadow: 0 3px 6px rgba(0,0,0,0.1);
}

.save-btn:disabled {
  background-color: #FFB8A3;
  cursor: not-allowed;
  opacity: 0.7;
}

.reset-btn {
  display: block;
  background-color: #F0F0F0;
  color: #333;
  border: none;
  border-radius: 12px;
  padding: 16px 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
  white-space: nowrap;
  flex: 1;
}

.reset-btn:hover {
  background-color: #E0E0E0;
  transform: translateY(-2px);
  box-shadow: 0 3px 6px rgba(0,0,0,0.1);
}

.reset-btn:first-child {
  background-color: #FF7043;
  color: white;
}

.reset-btn:first-child:hover {
  background-color: #FF5722;
}

.reset-btn.secondary {
  background-color: #F5F5F5;
  color: #666;
  border: 1px solid #E0E0E0;
}

.reset-btn.secondary:hover {
  background-color: #EEEEEE;
  color: #555;
}

.map-overlay {
  position: absolute;
  top: 16px;
  left: 16px;
  background-color: white;
  padding: 12px 20px;
  border-radius: 50px;
  z-index: 10;
  box-shadow: 0 3px 10px rgba(0,0,0,0.2);
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

.pin-icon {
  width: 24px;
  height: 36px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -100%);
  z-index: 5;
  display: none;
  pointer-events: none;
}

.pin-icon.visible {
  display: block;
  animation: pinDrop 0.5s ease-out;
}

@keyframes pinDrop {
  0% { transform: translate(-50%, -150%); opacity: 0; }
  100% { transform: translate(-50%, -100%); opacity: 1; }
}

@keyframes pulse {
  0% { opacity: 0.6; }
  50% { opacity: 1; }
  100% { opacity: 0.6; }
}

/* 모바일 반응형 디자인 */
@media (max-width: 400px) {
  .header-content {
    padding: 12px;
  }

  .header-title {
    font-size: 20px;
  }
  
  .header-subtitle {
    font-size: 13px;
  }
  
  .header-title-section {
    width: calc(100% - 104px); /* 작은 화면에서 버튼 공간 조정 */
  }
  
  .input-with-button input {
    padding: 12px 8px;
  }
  
  .input-with-button button {
    min-width: 48px;
    padding: 0 8px;
    font-size: 13px;
  }
  
  .address-input-box {
    margin-left: 12px;
    margin-right: 12px;
  }

  .reset-btn {
    padding: 14px 8px;
    font-size: 13px;
  }
  
  .buttons-container {
    gap: 8px;
    margin: 20px 12px 12px 12px;
  }

  .feedback-card {
    margin: 12px;
    padding: 12px;
  }

  .feedback-icon {
    font-size: 20px;
  }

  .feedback-text {
    font-size: 13px;
  }
}

/* 더 작은 화면을 위한 스타일 */
@media (max-width: 340px) {
  .header-title {
    font-size: 18px;
  }
  
  .header-subtitle {
    font-size: 12px;
  }
  
  .address-input-box label {
    font-size: 12px;
    width: 36px;
  }
  
  .input-with-button button {
    min-width: 40px;
    font-size: 12px;
  }

  .header-title-section {
    width: calc(100% - 96px);
  }
}
</style>