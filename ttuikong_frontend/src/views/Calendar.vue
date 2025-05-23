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
          <h2 class="profile-name">{{ userName }}</h2>
          <p class="profile-level">{{ userInfo.activityLevel }}</p>
          <div class="profile-stats">
            <div class="stat">
              <span class="stat-label">총 거리</span>
              <span class="stat-value">{{ userInfo.totalDistance }}</span>
            </div>
            <!-- <div class="stat">
              <span class="stat-label">총 시간</span>
              <span class="stat-value">{{ userInfo.totalDuration }}</span>
            </div> -->
          </div>
        </div>
      </div>
    </div>

    <!-- 러닝 통계 카드 -->
    <div class="stats-card">
      <div class="card-header">
        <h2 class="card-title">월 통계</h2>
        <span class="card-subtitle">{{ currentMonth }}</span>
      </div>
      <div class="stats-grid">
        <div class="stat-item">
          <span class="stat-icon">📏</span>
          <div class="stat-content">
            <span class="stat-value">{{ monthlyStats.monthdistance }}</span>
            <span class="stat-label">총 거리</span>
          </div>
        </div>
        <div class="stat-item">
          <span class="stat-icon">⏱️</span>
          <div class="stat-content">
            <span class="stat-value">{{ monthlyStats.monthduration }}</span>
            <span class="stat-label">총 시간</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 캘린더 섹션 -->
    <div class="calendar-section">
      <div class="card-header">
        <h2 class="card-title">기록</h2>
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
        <div v-for="(day, index) in calendarDays" :key="index" class="calendar-day" :class="{
          'empty': !day.date,
          'has-run': day.hasRun,
          'active': day.isActive,
          'today': day.isToday
        }" @click="day.date && selectDay(day)">
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
          <span class="run-time">{{ run.startTime.replace('T', ' ') }}</span>
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
        </div>

        <div class="run-map-preview">
          <img :src="run.imageUrl ? 'http://localhost:8080' + run.imageUrl : defaultImage" alt="러닝 루트"
            class="route-image" />
        </div>
      </div>
    </div>

    <!-- 러닝 기록이 없는 경우 -->
    <div v-else-if="selectedDay && !selectedDay.hasRun" class="no-run-message">
      <span class="icon">🏃</span>
      <p>{{ formatDate(selectedDay.date) }}에는 러닝 기록이 없습니다.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import profileImg from '../assets/profile.png';
import defaultImage from '@/assets/default-map.jpg';

// 반응형 상태 정의
const profile = ref(profileImg);
const token = ref('');
const userId = ref(null);
const userName = ref('');
const userInfo = ref({});

const currentDate = ref(new Date());
const selectedDate = ref(new Date());
const selectedDay = ref(null);
const monthlyStats = ref({
  monthdistance: '',
  monthduration: ''
});

const weekdays = ref(['일', '월', '화', '수', '목', '금', '토']);
const calendarDays = ref([]);

// 러닝 데이터
const runningData = ref([]);

// Computed 속성
const currentMonth = computed(() => {
  const months = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];
  return months[selectedDate.value.getMonth()];
});

const currentMonthYear = computed(() => {
  const months = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];
  return `${months[selectedDate.value.getMonth()]} ${selectedDate.value.getFullYear()}`;
});

const selectedDayRuns = computed(() => {
  if (!selectedDay.value) return [];
  const run = runningData.value.find(data =>
    isSameDay(data.startTime, selectedDay.value.date)
  );
  return run ? [run] : [];
});

// 메서드
function generateCalendar() {
  const year = selectedDate.value.getFullYear();
  const month = selectedDate.value.getMonth();

  // 선택된 달의 첫날과 마지막 날
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);

  // 첫날의 요일 (0: 일요일, 6: 토요일)
  const firstDayOfWeek = firstDay.getDay();

  // 캘린더 배열 초기화
  calendarDays.value = [];

  // 첫 주의 빈 칸 채우기
  for (let i = 0; i < firstDayOfWeek; i++) {
    calendarDays.value.push({ date: null });
  }

  // 날짜 채우기
  for (let i = 1; i <= lastDay.getDate(); i++) {
    const currentDateObj = new Date(year, month, i);

    // 러닝 기록 확인
    const hasRun = runningData.value.some(data =>
      isSameDay(data.startTime, currentDateObj)
    );

    calendarDays.value.push({
      date: currentDateObj,
      hasRun: hasRun,
      isToday: isSameDay(currentDateObj, currentDate.value),
      isActive: selectedDay.value ? isSameDay(currentDateObj, selectedDay.value.date) : false
    });
  }

  // 마지막 주의 빈 칸 채우기 (7의 배수로 맞추기)
  const remaining = 7 - (calendarDays.value.length % 7);
  if (remaining < 7) {
    for (let i = 0; i < remaining; i++) {
      calendarDays.value.push({ date: null });
    }
  }
}

function changeMonth(delta) {
  const newDate = new Date(selectedDate.value);
  newDate.setMonth(newDate.getMonth() + delta);
  selectedDate.value = newDate;
  selectedDay.value = null;
  generateCalendar();
}

function selectDay(day) {
  selectedDay.value = day;

  // 활성화 상태 업데이트
  calendarDays.value = calendarDays.value.map(d => ({
    ...d,
    isActive: d.date && day.date ? isSameDay(d.date, day.date) : false
  }));
}

function isSameDay(date1, date2) {
  const d1 = new Date(date1);
  const d2 = new Date(date2);
  return d1.getFullYear() === d2.getFullYear() &&
    d1.getMonth() === d2.getMonth() &&
    d1.getDate() === d2.getDate();
}

function formatDate(date) {
  const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' };
  return date.toLocaleDateString('ko-KR', options);
}

/* user 불러오기 */
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
    userInfo.value = data.user;

    token.value = currentToken;
    userId.value = user.id;
    userName.value = user.nickname;

  } catch (err) {
    console.error('사용자 정보 요청 실패:', err);
    alert('로그인이 필요합니다.');
  }
};

/* 월별 통계 */
const getMonthlyStats = async (userId) => {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth() + 1;

  try {
    const res = await fetch(
      `http://localhost:8080/api/users/${userId}/records?year=${year}&month=${month}`,
      {
        headers: {
          Authorization: `Bearer ${token.value}`
        }
      }
    );

    const data = await res.json();
    if (Array.isArray(data)) {
      const totalDistance = data.reduce((sum, rec) => sum + (rec.distance || 0), 0);
      const totalDuration = data.reduce((sum, rec) => sum + (rec.duration || 0), 0);

      monthlyStats.value.monthdistance = totalDistance.toFixed(1) + 'km';
      monthlyStats.value.monthduration = formatDuration(totalDuration);
    } else {
      console.warn("예상과 다른 응답 구조:", data);
    }

  } catch (err) {
    console.error("월별 러닝 정보 조회 실패:", err);
  }
};

const formatDuration = (min) => {
  if (!min) return "0분";
  const hr = Math.floor(min / 60);
  const m = min % 60;
  if (hr < 24)
    return `${hr}시간 ${m.toFixed(0)}분`;
  else {
    const day = Math.floor(hr / 24);
    const h = hr % 24;
    return `${day}일 ${h.toFixed(0)}시간 ${m.toFixed(0)}분`;
  }
};

/* 유저의 모든 루트 기록 */
const getAllRoutes = async () => {
  try {
    const res = await fetch(
      `http://localhost:8080/api/my/route`,
      {
        headers: {
          Authorization: `Bearer ${token.value}`
        }
      }
    );

    const data = await res.json();
    console.log(data);
    runningData.value = data;

  } catch (err) {
    console.error("모든 루트 정보 조회 실패:", err);
  }
};


// 컴포넌트 마운트 시 실행
onMounted(async () => {
  await getCurrentUser();
  await getMonthlyStats(userId.value);
  await getAllRoutes();
  generateCalendar();

  // 오늘 날짜에 러닝 기록이 있는지 확인하고 선택
  const today = calendarDays.value.find(day =>
    day.date && isSameDay(day.date, currentDate.value)
  );

  if (today && today.hasRun) {
    selectDay(today);
  }
});
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
  padding: 10px;
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
  padding: 10px;
  border-radius: var(--border-radius, 12px);
}

.stat-icon {
  font-size: 24px;
  margin-right: 4px;
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
  overflow-x: hidden;
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
  gap: 8px;
  padding: 8px;
}

.calendar-day {
  position: relative;
  aspect-ratio: 1 / 1;
  width: 100%;
  background-color: transparent;
  border: none;
  outline: none;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  border-radius: 50%;
  transition: background-color 0.2s ease;
  user-select: none;
  max-width: 40px;
}

.calendar-day:hover {
  background-color: rgba(255, 87, 34, 0.1);
}

.calendar-day.active {
  background-color: #FF5722;
  color: white;
}

.calendar-day.today {
  border: 2px solid #FF5722;
  box-sizing: border-box;
}

@media (max-width: 740px) {
  .calendar-day.active {
    width: 30px !important;
    height: 30px !important;
  }

  .calendar-day.today {
    width: 30px !important;
    height: 30px !important;
  }

  .run-indicator {
    bottom: -6px !important;
  }
}


.calendar-day.empty {
  visibility: hidden;
}

.day-number {
  font-size: 14px;
  font-weight: 500;
}

.run-indicator {
  width: 6px;
  height: 6px;
  background-color: #FF5722;
  border-radius: 50%;
  position: absolute;
  bottom: 2px;
  left: 50%;
  transform: translateX(-50%);
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
</style>