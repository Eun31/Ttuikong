<template>
  <div class="container">
    <!-- 헤더 -->
    <header class="profile-header">
      <h1 class="page-title">캘린더</h1>
    </header>

    <!-- 사용자 정보 카드 -->
    <div class="profile-card">
      <div class="profile-info">
        <div class="profile-avatar">
          <img :src="profile" alt="프로필 이미지">
        </div>
        <div class="profile-details">
          <h2 class="profile-name">{{ user.name }}</h2>
          <p class="profile-level">{{ user.level }}</p>
          <div class="profile-stats">
            <div class="stat">
              <span class="stat-value">{{ user.totalDistance }}</span>
              <span class="stat-label">총 거리</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ user.totalRuns }}</span>
              <span class="stat-label">총 러닝</span>
            </div>
            <div class="stat">
              <span class="stat-value">{{ user.achievements }}</span>
              <span class="stat-label">뱃지</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 러닝 통계 카드 -->
    <div class="stats-card">
      <div class="card-header">
        <h2 class="card-title">이번 달 러닝 통계</h2>
        <span class="card-subtitle">{{ currentMonth }}</span>
      </div>
      <div class="stats-grid">
        <div class="stat-item">
          <span class="stat-icon">🏃</span>
          <div class="stat-content">
            <span class="stat-value">{{ monthlyStats.totalRuns }}</span>
            <span class="stat-label">러닝 횟수</span>
          </div>
        </div>
        <div class="stat-item">
          <span class="stat-icon">📏</span>
          <div class="stat-content">
            <span class="stat-value">{{ monthlyStats.totalDistance }}</span>
            <span class="stat-label">총 거리</span>
          </div>
        </div>
        <div class="stat-item">
          <span class="stat-icon">⏱️</span>
          <div class="stat-content">
            <span class="stat-value">{{ monthlyStats.totalTime }}</span>
            <span class="stat-label">총 시간</span>
          </div>
        </div>
        <div class="stat-item">
          <span class="stat-icon">🔥</span>
          <div class="stat-content">
            <span class="stat-value">{{ monthlyStats.totalCalories }}</span>
            <span class="stat-label">소모 칼로리</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 캘린더 섹션 -->
    <div class="calendar-section">
      <div class="card-header">
        <h2 class="card-title">일별 러닝 기록</h2>
        <div class="month-selector">
          <button class="month-btn" @click="changeMonth(-1)">
            <span class="icon">←</span>
          </button>
          <span class="current-month">{{ currentMonthYear }}</span>
          <button class="month-btn" @click="changeMonth(1)">
            <span class="icon">→</span>
          </button>
        </div>
      </div>

      <!-- 요일 헤더 -->
      <div class="calendar-weekdays">
        <div class="weekday" v-for="day in weekdays" :key="day">{{ day }}</div>
      </div>

      <!-- 일자 그리드 -->
      <div class="calendar-days">
        <div
          v-for="(day, index) in calendarDays"
          :key="index"
          class="calendar-day"
          :class="{
            'empty': !day.date,
            'has-run': day.hasRun,
            'active': day.isActive,
            'today': day.isToday
          }"
          @click="day.date && selectDay(day)"
        >
          <span v-if="day.date" class="day-number">{{ day.date.getDate() }}</span>
          <span v-if="day.hasRun" class="run-indicator"></span>
        </div>
      </div>
    </div>

    <!-- 선택한 날짜의 러닝 기록 -->
    <div v-if="selectedDay && selectedDay.hasRun" class="run-details">
      <div class="run-date">
        <span class="icon">📅</span>
        {{ formatDate(selectedDay.date) }}
      </div>

      <!-- 러닝 정보 카드 -->
      <div class="run-card" v-for="(run, index) in selectedDayRuns" :key="index">
        <div class="run-header">
          <h3 class="run-title">{{ run.routeName }}</h3>
          <span class="run-time">{{ run.time }}</span>
        </div>
        
        <div class="run-stats">
          <div class="run-stat">
            <span class="stat-icon">⏱️</span>
            <span class="stat-label">시간:</span>
            <span class="stat-value">{{ run.duration }}</span>
          </div>
          <div class="run-stat">
            <span class="stat-icon">📏</span>
            <span class="stat-label">거리:</span>
            <span class="stat-value">{{ run.distance }}</span>
          </div>
          <div class="run-stat">
            <span class="stat-icon">🔥</span>
            <span class="stat-label">칼로리:</span>
            <span class="stat-value">{{ run.calories }}</span>
          </div>
        </div>
        
        <div class="run-map-preview">
          <img :src="run.routeImage" alt="러닝 루트" class="route-image">
          <button class="view-route-btn" @click="viewRoute(run.id)">
            <span class="icon">🗺️</span> 루트 보기
          </button>
        </div>
      </div>
    </div>

    <!-- 러닝 기록이 없는 경우 -->
    <div v-else-if="selectedDay && !selectedDay.hasRun" class="no-run-message">
      <span class="icon">🏃</span>
      <p>{{ formatDate(selectedDay.date) }}에는 러닝 기록이 없습니다.</p>
    </div>

    <!-- 루트 모음 버튼 -->
    <div class="route-collection-btn-container">
      <button class="route-collection-btn" @click="showRouteCollection = true">
        <span class="icon">🗺️</span> 모든 루트 보기
      </button>
    </div>

    <!-- 루트 모음 모달 -->
    <div v-if="showRouteCollection" class="modal-overlay" @click="showRouteCollection = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2 class="modal-title">내 러닝 루트 모음</h2>
          <button class="close-modal-btn" @click="showRouteCollection = false">
            <span class="icon">✕</span>
          </button>
        </div>
        
        <div class="routes-grid">
          <div v-for="(route, index) in routes" :key="index" class="route-item">
            <div class="route-image-container">
              <img :src="route.image" alt="러닝 루트" class="route-thumbnail">
            </div>
            <div class="route-info">
              <h3 class="route-name">{{ route.name }}</h3>
              <div class="route-meta">
                <span>{{ route.distance }}</span>
                <span class="route-separator">•</span>
                <span>{{ route.runCount }}회 달림</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import profileImg from '../assets/profile.png';

export default {
  name: 'Profile',
  data() {
    return {
      profile: profileImg,
      user: {
        name: '러너홍길동',
        level: '열정적인 러너',
        totalDistance: '158.2km',
        totalRuns: 24,
        achievements: 8
      },
      currentDate: new Date(),
      selectedDate: new Date(),
      selectedDay: null,
      monthlyStats: {
        totalRuns: 12,
        totalDistance: '78.5km',
        totalTime: '8시간 45분',
        totalCalories: '4,320kcal'
      },
      weekdays: ['일', '월', '화', '수', '목', '금', '토'],
      runningData: [
        {
          date: new Date(2025, 4, 15), // 2025년 5월 15일
          runs: [
            {
              id: 1,
              routeName: '한강 공원 러닝',
              time: '오전 7:30',
              duration: '42분 30초',
              distance: '5.2km',
              calories: '320kcal',
              routeImage: 'https://via.placeholder.com/400x200/FFCCBC/FF5722?text=한강공원코스'
            }
          ]
        },
        {
          date: new Date(2025, 4, 13), // 2025년 5월 13일
          runs: [
            {
              id: 2,
              routeName: '동네 공원 산책',
              time: '오후 6:15',
              duration: '28분 10초',
              distance: '3.1km',
              calories: '180kcal',
              routeImage: 'https://via.placeholder.com/400x200/FFCCBC/FF5722?text=동네공원코스'
            }
          ]
        },
        {
          date: new Date(2025, 4, 10), // 2025년 5월 10일
          runs: [
            {
              id: 3,
              routeName: '아침 조깅',
              time: '오전 6:45',
              duration: '35분 20초',
              distance: '4.3km',
              calories: '250kcal',
              routeImage: 'https://via.placeholder.com/400x200/FFCCBC/FF5722?text=아침조깅코스'
            },
            {
              id: 4,
              routeName: '저녁 달리기',
              time: '오후 7:30',
              duration: '50분 15초',
              distance: '6.8km',
              calories: '420kcal',
              routeImage: 'https://via.placeholder.com/400x200/FFCCBC/FF5722?text=저녁달리기코스'
            }
          ]
        },
        {
          date: new Date(2025, 4, 8), // 2025년 5월 8일
          runs: [
            {
              id: 5,
              routeName: '오피스 점심 러닝',
              time: '오후 12:30',
              duration: '22분 40초',
              distance: '2.8km',
              calories: '170kcal',
              routeImage: 'https://via.placeholder.com/400x200/FFCCBC/FF5722?text=점심러닝코스'
            }
          ]
        },
        {
          date: new Date(2025, 4, 5), // 2025년 5월 5일
          runs: [
            {
              id: 6,
              routeName: '주말 장거리 달리기',
              time: '오전 8:15',
              duration: '1시간 25분',
              distance: '12.6km',
              calories: '850kcal',
              routeImage: 'https://via.placeholder.com/400x200/FFCCBC/FF5722?text=장거리코스'
            }
          ]
        },
        {
          date: new Date(2025, 4, 2), // 2025년 5월 2일
          runs: [
            {
              id: 7,
              routeName: '야간 조깅',
              time: '오후 9:00',
              duration: '30분 50초',
              distance: '3.5km',
              calories: '210kcal',
              routeImage: 'https://via.placeholder.com/400x200/FFCCBC/FF5722?text=야간코스'
            }
          ]
        }
      ],
      routes: [
        {
          id: 1,
          name: '한강 공원 코스',
          distance: '5.2km',
          runCount: 8,
          image: 'https://via.placeholder.com/400x200/FFCCBC/FF5722?text=한강공원코스'
        },
        {
          id: 2,
          name: '동네 공원 코스',
          distance: '3.1km',
          runCount: 12,
          image: 'https://via.placeholder.com/400x200/FFCCBC/FF5722?text=동네공원코스'
        },
        {
          id: 3,
          name: '아침 조깅 코스',
          distance: '4.3km',
          runCount: 5,
          image: 'https://via.placeholder.com/400x200/FFCCBC/FF5722?text=아침조깅코스'
        },
        {
          id: 4,
          name: '장거리 코스',
          distance: '12.6km',
          runCount: 3,
          image: 'https://via.placeholder.com/400x200/FFCCBC/FF5722?text=장거리코스'
        },
        {
          id: 5,
          name: '야간 코스',
          distance: '3.5km',
          runCount: 4,
          image: 'https://via.placeholder.com/400x200/FFCCBC/FF5722?text=야간코스'
        },
        {
          id: 6,
          name: '점심 러닝 코스',
          distance: '2.8km',
          runCount: 7,
          image: 'https://via.placeholder.com/400x200/FFCCBC/FF5722?text=점심러닝코스'
        }
      ],
      showRouteCollection: false,
      calendarDays: []
    };
  },
  computed: {
    currentMonth() {
      const months = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];
      return months[this.selectedDate.getMonth()];
    },
    currentMonthYear() {
      const months = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];
      return `${months[this.selectedDate.getMonth()]} ${this.selectedDate.getFullYear()}`;
    },
    selectedDayRuns() {
      if (!this.selectedDay || !this.selectedDay.hasRun) return [];
      
      const runData = this.runningData.find(data => 
        this.isSameDay(data.date, this.selectedDay.date)
      );
      
      return runData ? runData.runs : [];
    }
  },
  methods: {
    generateCalendar() {
      const year = this.selectedDate.getFullYear();
      const month = this.selectedDate.getMonth();
      
      // 선택된 달의 첫날과 마지막 날
      const firstDay = new Date(year, month, 1);
      const lastDay = new Date(year, month + 1, 0);
      
      // 첫날의 요일 (0: 일요일, 6: 토요일)
      const firstDayOfWeek = firstDay.getDay();
      
      // 캘린더 배열 초기화
      this.calendarDays = [];
      
      // 첫 주의 빈 칸 채우기
      for (let i = 0; i < firstDayOfWeek; i++) {
        this.calendarDays.push({ date: null });
      }
      
      // 날짜 채우기
      for (let i = 1; i <= lastDay.getDate(); i++) {
        const currentDate = new Date(year, month, i);
        
        // 러닝 기록 확인
        const hasRun = this.runningData.some(data => 
          this.isSameDay(data.date, currentDate)
        );
        
        this.calendarDays.push({
          date: currentDate,
          hasRun: hasRun,
          isToday: this.isSameDay(currentDate, this.currentDate),
          isActive: this.selectedDay ? this.isSameDay(currentDate, this.selectedDay.date) : false
        });
      }
      
      // 마지막 주의 빈 칸 채우기 (7의 배수로 맞추기)
      const remaining = 7 - (this.calendarDays.length % 7);
      if (remaining < 7) {
        for (let i = 0; i < remaining; i++) {
          this.calendarDays.push({ date: null });
        }
      }
    },
    changeMonth(delta) {
      const newDate = new Date(this.selectedDate);
      newDate.setMonth(newDate.getMonth() + delta);
      this.selectedDate = newDate;
      this.selectedDay = null;
      this.generateCalendar();
    },
    selectDay(day) {
      this.selectedDay = day;
      
      // 활성화 상태 업데이트
      this.calendarDays = this.calendarDays.map(d => ({
        ...d,
        isActive: d.date && day.date ? this.isSameDay(d.date, day.date) : false
      }));
    },
    isSameDay(date1, date2) {
      return date1.getFullYear() === date2.getFullYear() &&
             date1.getMonth() === date2.getMonth() &&
             date1.getDate() === date2.getDate();
    },
    formatDate(date) {
      const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
      return date.toLocaleDateString('ko-KR', options);
    },
    viewRoute(routeId) {
      // 루트 상세보기로 이동 (실제 구현에서는 라우터 사용)
      alert(`루트 ID: ${routeId} 상세보기로 이동합니다.`);
    }
  },
  mounted() {
    this.generateCalendar();
    
    // 오늘 날짜에 러닝 기록이 있는지 확인하고 선택
    const today = this.calendarDays.find(day => 
      day.date && this.isSameDay(day.date, this.currentDate)
    );
    
    if (today && today.hasRun) {
      this.selectDay(today);
    }
  }
};
</script>

<style scoped>
.container {
  max-width: 100%;
  margin: 0 auto;
  background-color: var(--background-color, #F5F5F5);
  min-height: 100vh;
  padding: 16px;
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (min-width: 768px) {
  .container {
    max-width: 700px;
    margin: 0 auto;
  }
}

/* 헤더 */
.profile-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--dark-text, #333);
}

/* 프로필 카드 */
.profile-card {
  background-color: var(--card-color, white);
  border-radius: var(--border-radius, 16px);
  box-shadow: var(--shadow-md, 0 2px 8px rgba(0, 0, 0, 0.08));
  padding: 20px;
  margin-bottom: 20px;
}

.profile-info {
  display: flex;
  align-items: center;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 20px;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-details {
  flex: 1;
}

.profile-name {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
  color: var(--dark-text, #333);
}

.profile-level {
  font-size: 14px;
  color: var(--primary-color, #FF5722);
  margin-bottom: 12px;
}

.profile-stats {
  display: flex;
  gap: 16px;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-weight: 600;
  color: var(--dark-text, #333);
}

.stat-label {
  font-size: 12px;
  color: var(--medium-text, #757575);
}

/* 러닝 통계 카드 */
.stats-card {
  background-color: var(--card-color, white);
  border-radius: var(--border-radius, 16px);
  box-shadow: var(--shadow-md, 0 2px 8px rgba(0, 0, 0, 0.08));
  padding: 20px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--dark-text, #333);
}

.card-subtitle {
  font-size: 14px;
  color: var(--primary-color, #FF5722);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  background-color: rgba(255, 87, 34, 0.05);
  padding: 16px;
  border-radius: var(--border-radius, 12px);
}

.stat-icon {
  font-size: 24px;
  margin-right: 12px;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

/* 캘린더 섹션 */
.calendar-section {
  background-color: var(--card-color, white);
  border-radius: var(--border-radius, 16px);
  box-shadow: var(--shadow-md, 0 2px 8px rgba(0, 0, 0, 0.08));
  padding: 20px;
  margin-bottom: 20px;
}

.month-selector {
  display: flex;
  align-items: center;
}

.month-btn {
  background: none;
  border: none;
  color: var(--primary-color, #FF5722);
  cursor: pointer;
  font-size: 20px;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.month-btn:hover {
  background-color: rgba(255, 87, 34, 0.1);
}

.current-month {
  font-weight: 500;
  margin: 0 12px;
  min-width: 100px;
  text-align: center;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 8px;
  border-bottom: 1px solid var(--border-color, #EEE);
  padding-bottom: 8px;
}

.weekday {
  text-align: center;
  font-weight: 500;
  font-size: 14px;
  color: var(--medium-text, #757575);
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}

.calendar-day {
  aspect-ratio: 1/1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  border-radius: 50%;
  cursor: pointer;
  transition: var(--transition, all 0.3s ease);
}

.calendar-day:not(.empty):hover {
  background-color: rgba(255, 87, 34, 0.1);
}

.calendar-day.today {
  border: 2px solid var(--primary-color, #FF5722);
}

.calendar-day.active {
  background-color: var(--primary-color, #FF5722);
  color: white;
}

.calendar-day.active .run-indicator {
  background-color: white;
}

.calendar-day.empty {
  cursor: default;
}

.day-number {
  font-weight: 500;
  font-size: 14px;
}

.run-indicator {
  width: 6px;
  height: 6px;
  background-color: var(--primary-color, #FF5722);
  border-radius: 50%;
  position: absolute;
  bottom: 4px;
}

/* 러닝 기록 상세 */
.run-details {
  margin-bottom: 24px;
}

.run-date {
  display: flex;
  align-items: center;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--primary-color, #FF5722);
}

.run-date .icon {
  margin-right: 8px;
}

.run-card {
  background-color: var(--card-color, white);
  border-radius: var(--border-radius, 16px);
  box-shadow: var(--shadow-md, 0 2px 8px rgba(0, 0, 0, 0.08));
  padding: 16px;
  margin-bottom: 16px;
}

.run-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.run-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--dark-text, #333);
}

.run-time {
  font-size: 14px;
  color: var(--medium-text, #757575);
}

.run-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.run-stat {
  display: flex;
  align-items: center;
  background-color: rgba(255, 87, 34, 0.05);
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 14px;
}

.run-stat .stat-icon {
  margin-right: 6px;
  font-size: 16px;
}

.run-stat .stat-label {
  margin-right: 4px;
  color: var(--medium-text, #757575);
}

.run-stat .stat-value {
  font-weight: 500;
}

.run-map-preview {
  position: relative;
}

.route-image {
  width: 100%;
  border-radius: var(--border-radius, 12px);
  height: 140px;
  object-fit: cover;
}

.view-route-btn {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background-color: var(--primary-color, #FF5722);
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

.view-route-btn:hover {
  background-color: var(--primary-dark, #E64A19);
}

/* 러닝 기록 없음 메시지 */
.no-run-message {
  background-color: var(--card-color, white);
  border-radius: var(--border-radius, 16px);
  box-shadow: var(--shadow-md, 0 2px 8px rgba(0, 0, 0, 0.08));
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--medium-text, #757575);
  gap: 12px;
  text-align: center;
}

.no-run-message .icon {
  font-size: 32px;
}

/* 루트 모음 버튼 */
.route-collection-btn-container {
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
}

.route-collection-btn {
  background-color: var(--primary-color, #FF5722);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 30px;
  font-size: 15px;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  box-shadow: var(--shadow-md, 0 2px 8px rgba(0, 0, 0, 0.08));
  transition: var(--transition, all 0.3s ease);
}

.route-collection-btn:hover {
  background-color: var(--primary-dark, #E64A19);
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg, 0 4px 12px rgba(0, 0, 0, 0.15));
}

/* 모달 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background-color: var(--card-color, white);
  border-radius: var(--border-radius, 16px);
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  padding: 20px;
  box-shadow: var(--shadow-lg, 0 10px 25px rgba(0, 0, 0, 0.15));
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color, #EEE);
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--dark-text, #333);
}

.close-modal-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: var(--medium-text, #757575);
  cursor: pointer;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: var(--transition, all 0.3s ease);
}

.close-modal-btn:hover {
  background-color: rgba(0, 0, 0, 0.05);
  color: var(--primary-color, #FF5722);
}

/* 루트 그리드 */
.routes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.route-item {
  background-color: var(--card-color, white);
  border-radius: var(--border-radius, 12px);
  overflow: hidden;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0, 0, 0, 0.1));
  transition: var(--transition, all 0.3s ease);
  cursor: pointer;
}

.route-item:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md, 0 4px 12px rgba(0, 0, 0, 0.1));
}

.route-image-container {
  width: 100%;
  height: 140px;
  overflow: hidden;
}

.route-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.route-item:hover .route-thumbnail {
  transform: scale(1.05);
}

.route-info {
  padding: 12px;
}

.route-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--dark-text, #333);
  margin-bottom: 6px;
}

.route-meta {
  font-size: 13px;
  color: var(--medium-text, #757575);
  display: flex;
  align-items: center;
}

.route-separator {
  margin: 0 6px;
}

/* 반응형 조정 */
@media (max-width: 768px) {
  .routes-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .routes-grid {
    grid-template-columns: 1fr;
  }
  
  .modal-content {
    width: 95%;
    padding: 16px;
  }
}
</style>