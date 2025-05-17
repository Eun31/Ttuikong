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

  <script>
  export default {
    name: 'RouteFind',
    data() {
      return {
        map: null,
        error: null,
        markers: {
          start: null,
          end: null
        },
        coords: {
          start: null,
          end: null
        },
        routePolyline: null,
        selectionState: 'start', // 'start', 'end', 'complete'
        totalDistance: 0, // 미터 단위
        walkTime: '0분',
        caloriesBurned: '0 kcal',
        routeCalculated: false,
        // 걷기 속도 (초당 미터)
        walkSpeed: 1.4, // 평균 걷기 속도 (약 5km/h)
        // 칼로리 소모율 (kg당 km당 kcal)
        calorieRate: 0.8,
        // 평균 체중 (kg)
        averageWeight: 65,
        // 주소 입력 기능
        startAddress: '',
        endAddress: '',
        isSearching: false,
        // TMAP API 키
        apiKey: 'mqrjE2sC0W8sX1UeeDEpO4k9dLeON1p01iT6Ianw'
      };
    },
    computed: {
      formattedDistance() {
        if (this.totalDistance < 1000) {
          return `${this.totalDistance}m`;
        } else {
          return `${(this.totalDistance / 1000).toFixed(1)}km`;
        }
      }
    },
    mounted() {
      // TMAP API 스크립트 동적 로드
      this.loadTmapScript().then(() => {
        // API 로드 후 지도 초기화
        this.initMap();
      }).catch(error => {
        this.error = "TMAP API 로드 실패: " + error;
      });
    },
    methods: {
      loadTmapScript() {
        return new Promise((resolve, reject) => {
          // 이미 로드되었는지 확인
          if (window.Tmapv2) {
            resolve();
            return;
          }
  
          const script = document.createElement('script');
          script.src = `https://apis.openapi.sk.com/tmap/jsv2?version=1&appKey=${this.apiKey}`;
          script.async = true;
          script.onload = () => resolve();
          script.onerror = () => reject(new Error('TMAP API 로드 실패'));
          document.head.appendChild(script);
        });
      },
  
      initMap() {
        try {
          // API 로드 확인
          if (typeof Tmapv2 === 'undefined') {
            this.error = "TMAP API가 로드되지 않았습니다. API 키를 확인해주세요.";
            return;
          }
  
          // 지도 생성 (서울시청 위치)
          this.map = new Tmapv2.Map("map_div", {
            center: new Tmapv2.LatLng(37.566, 126.978),
            width: "100%",
            height: "300px",
            zoom: 14
          });
  
          // 사용자가 지도를 클릭하여 지점을 선택할 수 있도록 이벤트 리스너 추가
          this.map.addListener("click", this.handleMapClick);
          console.log("지도 클릭 리스너 등록됨");
  
        } catch (e) {
          this.error = "지도 초기화 오류: " + e.message;
          console.error("지도 초기화 오류:", e);
        }
      },
  
      handleMapClick(e) {
        console.log("지도 클릭됨", e.latLng.toString());
        const clickedPosition = e.latLng;
  
        // 클릭한 위치의 주소 검색 (역지오코딩)
        this.getAddressFromCoords(clickedPosition, (address) => {
          if (this.selectionState === 'start') {
            // 출발지 선택
            this.startAddress = address || '주소를 찾을 수 없습니다';
            this.setStartPosition(clickedPosition);
            this.selectionState = 'end';
            console.log("출발지 설정 완료, 도착지 선택 모드로 전환");
          } else if (this.selectionState === 'end') {
            // 도착지 선택
            this.endAddress = address || '주소를 찾을 수 없습니다';
            this.setEndPosition(clickedPosition);
            this.selectionState = 'complete';
            console.log("도착지 설정 완료, 경로 계산 시작");
            // 경로 계산 시작
            this.calculateRoute();
          }
        });
      },
  
      // 좌표로부터 주소 검색
      getAddressFromCoords(position, callback) {
        const apiUrl = "https://apis.openapi.sk.com/tmap/geo/reversegeocoding?version=1&format=json";
        
        const reqParams = {
          appKey: this.apiKey,
          coordType: "WGS84GEO",
          addressType: "A10",
          lon: position.lng(),
          lat: position.lat()
        };
        
        // 쿼리 문자열 생성
        const queryString = Object.keys(reqParams)
          .map(key => `${key}=${encodeURIComponent(reqParams[key])}`)
          .join('&');
        
        fetch(`${apiUrl}&${queryString}`)
          .then(response => response.json())
          .then(data => {
            let address = '';
            if (data && data.addressInfo) {
              // 전체 주소 구성
              const info = data.addressInfo;
              address = `${info.fullAddress}`;
            }
            callback(address);
          })
          .catch(e => {
            console.error("주소 검색 오류:", e);
            callback(null);
          });
      },
  
      setStartPosition(position) {
        // 기존 마커가 있으면 제거
        if (this.markers.start) {
          this.markers.start.setMap(null);
        }
  
        // 마커 생성
        this.markers.start = new Tmapv2.Marker({
          position: position,
          icon: {
            url: '/assets/location.png', // 경로는 실제 프로젝트에 맞게 조정 필요
            size: new Tmapv2.Size(48, 48),
            anchor: new Tmapv2.Point(24, 48)
          },
          map: this.map,
          title: '출발지'
        });
  
        // 좌표 저장
        this.coords.start = position;
        console.log("출발지 마커 생성", position.toString());
      },
  
      setEndPosition(position) {
        // 기존 마커가 있으면 제거
        if (this.markers.end) {
          this.markers.end.setMap(null);
        }
  
        // 마커 생성
        this.markers.end = new Tmapv2.Marker({
          position: position,
          icon: {
            url: '/assets/destination.png', // 실제 경로로 변경 필요
            size: new Tmapv2.Size(48, 48),
            anchor: new Tmapv2.Point(24, 48)
          },
          map: this.map,
          title: '도착지'
        });
  
        // 좌표 저장
        this.coords.end = position;
        console.log("도착지 마커 생성", position.toString());
      },
  
      calculateRoute() {
        try {
          // 기존 경로가 있으면 제거
          if (this.routePolyline) {
            this.routePolyline.setMap(null);
          }
  
          // 요청 URL 및 파라미터 설정
          const apiUrl = "https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1&format=json";
          
          // 요청 파라미터
          const params = {
            startX: this.coords.start.lng(),
            startY: this.coords.start.lat(),
            endX: this.coords.end.lng(),
            endY: this.coords.end.lat(),
            reqCoordType: "WGS84GEO",
            resCoordType: "WGS84GEO",
            startName: encodeURIComponent("출발지"),
            endName: encodeURIComponent("도착지")
          };
  
          // API 요청
          fetch(apiUrl, {
            method: 'POST',
            headers: {
              'Accept': 'application/json',
              'Content-Type': 'application/json',
              'appKey': this.apiKey
            },
            body: JSON.stringify(params)
          })
          .then(response => response.json())
          .then(data => {
            // 응답 데이터 처리
            if (data.features && data.features.length > 0) {
              // 총 거리 정보 업데이트 (미터 단위)
              this.totalDistance = data.features[0].properties.totalDistance;
              
              // 총 시간 정보 업데이트 (초 단위)
              const totalTimeSeconds = data.features[0].properties.totalTime;
              this.walkTime = this.formatTime(totalTimeSeconds);
              
              // 칼로리 소모량 계산
              const caloriesBurned = Math.round(this.totalDistance / 1000 * this.calorieRate * this.averageWeight);
              this.caloriesBurned = `${caloriesBurned} kcal`;
              
              // 경로 그리기
              let lineArray = [];
              
              // 응답의 features 배열에서 coordinates 값 가져오기
              for (let i in data.features) {
                const feature = data.features[i];
                
                if (feature.geometry.type === "LineString") {
                  for (let j in feature.geometry.coordinates) {
                    const coordinates = feature.geometry.coordinates[j];
                    // WGS84GEO 좌표를 사용하므로 변환이 필요 없음
                    const convertedCoords = new Tmapv2.LatLng(coordinates[1], coordinates[0]);
                    lineArray.push(convertedCoords);
                  }
                }
              }
              
              // 경로 그리기
              this.routePolyline = new Tmapv2.Polyline({
                path: lineArray,
                strokeColor: "#FF7043", // 홈 페이지 스타일에 맞춰 변경
                strokeWeight: 8,
                map: this.map
              });
              
              // 경로 계산 완료 표시
              this.routeCalculated = true;
              
              // 경로가 모두 보이도록 지도 경계 조정
              const bounds = new Tmapv2.LatLngBounds();
              for (let i in lineArray) {
                bounds.extend(lineArray[i]);
              }
              this.map.fitBounds(bounds);
            } else {
              this.error = "경로를 찾을 수 없습니다.";
            }
          })
          .catch(e => {
            this.error = "경로 계산 API 오류: " + e.message;
            console.error("경로 계산 API 오류:", e);
          });
        } catch (e) {
          this.error = "경로 계산 오류: " + e.message;
          console.error("경로 계산 오류:", e);
        }
      },
  
      formatTime(seconds) {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
  
        if (hours > 0) {
          return `${hours}시간 ${minutes}분`;
        } else {
          return `${minutes}분`;
        }
      },
  
      // 경로 다시 선택 기능
      resetSelection() {
        // 선택 상태 초기화
        this.selectionState = 'start';
        
        // 마커 제거
        if (this.markers.start) {
          this.markers.start.setMap(null);
          this.markers.start = null;
        }
        if (this.markers.end) {
          this.markers.end.setMap(null);
          this.markers.end = null;
        }
        
        // 좌표 초기화
        this.coords.start = null;
        this.coords.end = null;
        
        // 경로 제거
        if (this.routePolyline) {
          this.routePolyline.setMap(null);
          this.routePolyline = null;
        }
        
        // 거리, 시간, 칼로리 초기화
        this.totalDistance = 0;
        this.walkTime = '0분';
        this.caloriesBurned = '0 kcal';
        this.routeCalculated = false;
        
        // 주소 입력 초기화
        this.startAddress = '';
        this.endAddress = '';
        
        // 에러 메시지 초기화
        this.error = null;
        
        // 지도 중심 초기화
        if (this.map) {
          this.map.setCenter(new Tmapv2.LatLng(37.566, 126.978));
          this.map.setZoom(14);
        }
        
        console.log("경로 선택 초기화 완료");
      },
      
      // 주소로 검색하는 기능 - 출발지
      searchStartAddress() {
        if (!this.startAddress || this.isSearching) return;
        
        this.isSearching = true;
        this.error = null;
        
        const apiUrl = "https://apis.openapi.sk.com/tmap/pois?version=1&format=json&callback=result";
        
        fetch(`${apiUrl}&appKey=${this.apiKey}&searchKeyword=${encodeURIComponent(this.startAddress)}`)
          .then(response => response.json())
          .then(data => {
            this.isSearching = false;
            
            if (data.searchPoiInfo && data.searchPoiInfo.pois && data.searchPoiInfo.pois.poi && data.searchPoiInfo.pois.poi.length > 0) {
              // 첫 번째 검색 결과 사용
              const poi = data.searchPoiInfo.pois.poi[0];
              const position = new Tmapv2.LatLng(poi.frontLat, poi.frontLon);
              
              // 출발지 설정
              this.setStartPosition(position);
              
              // 지도 이동
              this.map.setCenter(position);
              this.map.setZoom(16);
              
              // 다음 단계로 진행
              if (this.selectionState === 'start') {
                this.selectionState = 'end';
                console.log("출발지 주소 설정 완료, 도착지 선택 모드로 전환");
              }
              
              // 출발지와 도착지가 모두 설정되면 경로 계산
              if (this.coords.start && this.coords.end) {
                this.calculateRoute();
              }
            } else {
              this.error = "검색 결과가 없습니다. 주소를 다시 확인해주세요.";
            }
          })
          .catch(e => {
            this.isSearching = false;
            this.error = "주소 검색 중 오류가 발생했습니다: " + e.message;
            console.error("주소 검색 오류:", e);
          });
      },
      
      // 주소로 검색하는 기능 - 도착지
      searchEndAddress() {
        if (!this.endAddress || this.isSearching) return;
        
        this.isSearching = true;
        this.error = null;
        
        const apiUrl = "https://apis.openapi.sk.com/tmap/pois?version=1&format=json&callback=result";
        
        fetch(`${apiUrl}&appKey=${this.apiKey}&searchKeyword=${encodeURIComponent(this.endAddress)}`)
          .then(response => response.json())
          .then(data => {
            this.isSearching = false;
            
            if (data.searchPoiInfo && data.searchPoiInfo.pois && data.searchPoiInfo.pois.poi && data.searchPoiInfo.pois.poi.length > 0) {
              // 첫 번째 검색 결과 사용
              const poi = data.searchPoiInfo.pois.poi[0];
              const position = new Tmapv2.LatLng(poi.frontLat, poi.frontLon);
              
              // 도착지 설정
              this.setEndPosition(position);
              
              // 지도 이동
              this.map.setCenter(position);
              this.map.setZoom(16);
              
              // 다음 단계로 진행
              if (this.selectionState === 'end') {
                this.selectionState = 'complete';
                console.log("도착지 주소 설정 완료");
              }
              
              // 출발지와 도착지가 모두 설정되면 경로 계산
              if (this.coords.start && this.coords.end) {
                this.calculateRoute();
              }
            } else {
              this.error = "검색 결과가 없습니다. 주소를 다시 확인해주세요.";
            }
          })
          .catch(e => {
            this.isSearching = false;
            this.error = "주소 검색 중 오류가 발생했습니다: " + e.message;
            console.error("주소 검색 오류:", e);
          });
      },
  
      // 경로 저장하기
      saveRoute() {
        // 여기에 경로 저장 로직 추가
        // 예: 서버에 저장하거나 로컬 스토리지에 저장
        alert('경로가 저장되었습니다!');
        // 실제 구현에서는 아래와 같이 이벤트를 발생시켜 상위 컴포넌트에 알림
        // this.$emit('route-saved', {
        //   start: this.startAddress,
        //   end: this.endAddress,
        //   distance: this.totalDistance,
        //   time: this.walkTime,
        //   calories: this.caloriesBurned
        // });
      }
    }
  };
  </script>
  
  <style scoped>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  font-family: 'Pretendard', 'Noto Sans KR', sans-serif;
}

html, body {
  background-color: #FFF8F2; /* 홈 화면과 동일한 배경색 */
  color: #333;
  margin: 0;
  padding: 0;
  overflow-x: hidden; /* 가로 스크롤 방지 */
  width: 100%;
  height: 100%;
  min-height: 100vh;
}

.app-container {
  width: 100%; /* 전체 너비로 확장 */
  margin: 0 auto;
  min-height: 100vh;
  background-color: #FFF8F2; /* 홈 화면과 동일한 배경색 */
  position: relative;
  padding-bottom: 30px;
}

.header {
  background-color: #FFF3ED; /* 홈 화면과 동일한 색상 계열 */
  padding: 0.8rem 1.5rem;
  display: flex;
  justify-content: center; /* 중앙 정렬 */
  align-items: center;
  box-shadow: 0 2px 10px rgba(255, 112, 67, 0.09);
  border-bottom: 1.5px solid #FFE5D5;
  width: 100%; /* 전체 너비 */
}

/* 헤더 내부 콘텐츠 추가 */
.header-content {
  width: 100%;
  max-width: 440px; /* 모바일 너비로 제한 */
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 메인 콘텐츠 컨테이너 */
.content-container {
  max-width: 440px; /* 모바일 너비로 제한 */
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
  background-color: #FF7043; /* 홈 화면과 동일한 포인트 색상 */
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
  margin: 32px 16px 8px 16px; /* 상단 여백 16px에서 40px로 증가 */
  color: #333;
  text-align: center;
  position: relative;
  padding-bottom: 12px;
}

.subtitle {
  font-size: 16px;
  color: #666;
  margin: 0 16px 16px 16px;
  text-align: center; /* 홈 화면과 동일하게 중앙 정렬 */
}

.stats-container {
  display: flex;
  margin: 0 16px 16px 16px;
  gap: 12px;
}

.stat-card {
  background-color: #FFF3ED; /* 홈 화면과 동일한 배경색 */
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  flex: 1;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.stat-icon {
  width: 40px;
  height: 40px;
  background-color: rgba(255, 112, 67, 0.2); /* 홈 화면 테마에 맞게 변경 */
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.stat-info {
  display: flex;
  flex-direction: column;
  color: #333; /* 홈 화면과 일치하게 변경 */
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
  width: 40px; /* 45px에서 40px로 줄임 */
  font-size: 13px; /* 14px에서 13px로 줄임 */
  color: #333;
  font-weight: 500;
  margin-right: 8px;
  white-space: nowrap; /* 줄바꿈 방지 */
}

/* 입력 필드와 버튼 정렬 */
.input-with-button {
  display: flex;
  gap: 8px;
  width: 100%;
  height: 48px; /* 더 높은 입력 필드 */
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
  border-color: #FF7043; /* 홈 화면과 동일한 포인트 색상 */
}

/* 버튼 스타일 */
.input-with-button button {
  min-width: 60px;
  padding: 0 16px;
  background-color: #FF7043; /* 홈 화면과 동일한 포인트 색상 */
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
  background-color: #FFF3ED; /* 홈 화면과 동일한 배경색 */
  padding: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
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
  background-color: #FF7043; /* 홈 화면과 동일한 포인트 색상 */
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
  box-shadow: 0 3px 6px rgba(0,0,0,0.1);
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

@media (max-width: 400px) {
  .input-with-button input {
    padding: 12px 8px; /* 좌우 패딩 줄임 */
  }
  
  .input-with-button button {
    min-width: 48px; /* 버튼 너비 줄임 */
    padding: 0 8px; /* 패딩 줄임 */
    font-size: 13px; /* 폰트 크기 줄임 */
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