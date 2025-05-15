// views/RunTimer.vue
<template>
  <div>
    <div id="map"></div>

    <div class="timer-card">
      <h2>현재 러닝 시간</h2>
      <div class="time" id="duration">{{ formattedTime }}</div>
      <button class="play-button" id="toggleTracking" @click="toggleTimer">{{ isRunning ? '■' : '▶' }}</button>
    </div>

    <div class="section">
      <div id="info">{{ infoText }}</div>
    </div>
    
    <!-- 추가된 네비게이션 메뉴 -->
    <div class="run-nav">
      <button class="nav-btn active" @click="stayOnTimer">개인 러닝</button>
      <button class="nav-btn" @click="navigateToCrew">크루 러닝</button>
      <button class="nav-btn" @click="navigateToRank">랭킹</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RunTimer',
  emits: ['navigate'],
  data() {
    return {
      seconds: 0,
      timer: null,
      isRunning: false,
      map: null,
      infoText: '러닝을 시작하려면 ▶를 누르세요',
      positions: [],
      distance: 0
    }
  },
  computed: {
    formattedTime() {
      const minutes = Math.floor(this.seconds / 60);
      const remainingSeconds = this.seconds % 60;
      return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
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
    },
    navigateToCrew() {
      this.$emit('navigate', 'RunWithCrew');
    },
    navigateToRank() {
      this.$emit('navigate', 'RunWithRank');
    }
  }
}
</script>

<style scoped>
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
  background: linear-gradient(rgba(0,0,0,0.5), rgba(0,0,0,0.5)), 
              url('../assets/run_img.jpg') center/cover no-repeat;
  color: white;
  padding: 30px 20px;
  border-radius: 15px;
  position: relative;
  text-align: left;
  margin: 15px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.timer-card h2 {
  font-size: 25px;
  margin: 0;
  font-weight: normal;
}

.timer-card .time {
  font-size: 45px;
  margin-top: 10px;
  font-weight: bold;
}

.play-button {
  position: absolute;
  right: 40px;
  top: 50%;
  transform: translateY(-50%);
  background: #e7b147;
  border: none;
  border-radius: 50%;
  width: 70px;
  height: 70px;
  color: white;
  font-size: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.section {
  margin-top: 230px;
  padding: 0 15px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #333;
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