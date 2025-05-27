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
          <img :src="profile" alt="프로필 이미지" />
        </div>
        <div class="profile-details">
          <h2 class="profile-name">{{ userName }}</h2>
          <p class="profile-level">{{ userInfo.activityLevel }}</p>
          <div class="profile-stats">
            <div class="stat">
              <span class="stat-label">전체 거리</span>
              <span class="stat-value">{{ userInfo.totalDistance }}km</span>
            </div>
            <div class="stat">
              <span class="stat-label">전체 칼로리 소모</span>
              <span class="stat-value"
                >{{
                  Math.round(userInfo.totalDistance * 1.2 * userInfo.weight)
                }}
                kcal</span
              >
            </div>
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
        <div class="stat-item">
          <span class="stat-icon">🔥</span>
          <div class="stat-content">
            <span class="stat-value"
              >{{
                Math.round(
                  parseFloat(monthlyStats.monthdistance.replace("km", "")) *
                    1.2 *
                    userInfo.weight
                )
              }}kcal</span
            >
            <span class="stat-label">총 칼로리 소모</span>
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
        <div
          v-for="(day, index) in calendarDays"
          :key="index"
          class="calendar-day"
          :class="{
            empty: !day.date,
            'has-run': day.hasRun,
            active: day.isActive,
            today: day.isToday,
          }"
          @click="day.date && selectDay(day)"
        >
          <span v-if="day.date" class="day-number">{{
            day.date.getDate()
          }}</span>
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
      <div
        class="run-card"
        v-for="(run, index) in selectedDayRuns"
        :key="index"
      >
        <div class="run-header">
          <h3 class="run-title">{{ run.mood }} {{ findEmoji(run.mood) }}</h3>
        </div>

        <div class="run-stats">
          <div class="run-stat">
            <span class="stat-icon">📏</span>
            <span class="stat-label">거리:</span>
            <span class="stat-value"
              >{{ (run.distance / 1000).toFixed(2) }}km</span
            >
          </div>
          <div class="run-stat">
            <span class="stat-icon">⏱️</span>
            <span class="stat-label">시간:</span>
            <span class="stat-value">{{ formatDuration(run.duration) }}</span>
          </div>
          <div v-if="run.calories > 0" class="run-stat">
            <span class="stat-icon">🔥</span>
            <span class="stat-label">칼로리:</span>
            <span class="stat-value">{{ run.calories }}kcal</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 러닝 기록이 없는 경우 -->
    <div v-else-if="selectedDay && !selectedDay.hasRun" class="no-run-message">
      <span class="icon">🏃</span>
      <p>{{ formatDate(selectedDay.date) }}에는 러닝 기록이 없습니다.</p>
    </div>
  </div>

  <!-- 루트 모음 버튼 -->
  <div class="route-collection-btn-container">
    <button class="route-collection-btn" @click="showRouteCollection = true">
      <span class="icon">🗺️</span> 모든 기록 보기
    </button>
  </div>

  <!-- 루트 모음 모달 -->
  <div
    v-if="showRouteCollection"
    class="modal-overlay"
    @click="showRouteCollection = false"
  >
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2 class="modal-title">내 러닝 기록 모음</h2>
        <button class="close-modal-btn" @click="showRouteCollection = false">
          <span class="icon">✕</span>
        </button>
      </div>

      <div class="routes-grid">
        <div v-for="(route, index) in routes" :key="index" class="route-item">
          <div class="route-image-container">
            <img
              :src="
                route.imageUrl
                  ? 'http://192.168.176.13:8080' + route.imageUrl
                  : defaultImage
              "
              alt="러닝 루트"
              class="route-thumbnail"
            />
          </div>
          <div class="route-info">
            <h3 class="route-name">
              <span
                >[{{ route.startTime.slice(0, 10) }}]
                {{ route.routeName }}</span
              >
              <span>{{ findEmoji(route.mood) }}</span>
            </h3>
            <div class="route-meta">
              <span>거리 {{ (route.distance / 1000).toFixed(2) }}km</span>
              <span class="route-separator">•</span>
              <span
                >시간
                {{ route.startTime.slice(11, 19).replace("T", " ") }}</span
              >
              <span class="route-separator">~</span>
              <span>{{
                route.endTime == null
                  ? "-"
                  : route.endTime.slice(11, 19).replace("T", " ")
              }}</span>
              <span
                >{{
                  route.endTime == null
                    ? ""
                    : " (" + formatDuration(route.duration) + ")"
                }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import profileImg from "../assets/profile.png";
import defaultImage from "@/assets/default-map2.png";

// 반응형 상태 정의
const profile = ref(profileImg);
const token = ref("");
const userId = ref(null);
const userName = ref("");
const userInfo = ref({});

const currentDate = ref(new Date());
const selectedDate = ref(new Date());
const selectedDay = ref(null);
const monthlyStats = ref({
  monthdistance: "",
  monthduration: "",
});

const weekdays = ref(["일", "월", "화", "수", "목", "금", "토"]);
const showRouteCollection = ref(false);
const calendarDays = ref([]);

// 러닝 데이터
const runningData = ref([]);
const routes = ref([]);

// Computed 속성
const currentMonth = computed(() => {
  const months = [
    "1월",
    "2월",
    "3월",
    "4월",
    "5월",
    "6월",
    "7월",
    "8월",
    "9월",
    "10월",
    "11월",
    "12월",
  ];
  return months[selectedDate.value.getMonth()];
});

const currentMonthYear = computed(() => {
  const months = [
    "1월",
    "2월",
    "3월",
    "4월",
    "5월",
    "6월",
    "7월",
    "8월",
    "9월",
    "10월",
    "11월",
    "12월",
  ];
  return `${
    months[selectedDate.value.getMonth()]
  } ${selectedDate.value.getFullYear()}`;
});

const selectedDayRuns = computed(() => {
  if (!selectedDay.value) return [];
  const run = runningData.value.find((data) =>
    isSameDay(data.date, selectedDay.value.date)
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
    const hasRun = runningData.value.some((data) =>
      isSameDay(data.date, currentDateObj)
    );

    calendarDays.value.push({
      date: currentDateObj,
      hasRun: hasRun,
      isToday: isSameDay(currentDateObj, currentDate.value),
      isActive: selectedDay.value
        ? isSameDay(currentDateObj, selectedDay.value.date)
        : false,
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

async function changeMonth(delta) {
  const newDate = new Date(selectedDate.value);
  newDate.setMonth(newDate.getMonth() + delta);
  selectedDate.value = newDate;
  selectedDay.value = null;
  await getDayRoutes();
  generateCalendar();
}

async function selectDay(day) {
  selectedDay.value = day;
  // 활성화 상태 업데이트
  calendarDays.value = calendarDays.value.map((d) => ({
    ...d,
    isActive: d.date && day.date ? isSameDay(d.date, day.date) : false,
  }));
}

function isSameDay(date1, date2) {
  const d1 = new Date(date1);
  const d2 = new Date(date2);
  return (
    d1.getFullYear() === d2.getFullYear() &&
    d1.getMonth() === d2.getMonth() &&
    d1.getDate() === d2.getDate()
  );
}

function formatDate(date) {
  const options = {
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "long",
  };
  return date.toLocaleDateString("ko-KR", options);
}

/* user 불러오기 */
const getCurrentUser = async () => {
  const currentToken = localStorage.getItem("jwt");

  try {
    const res = await fetch(`/api/users/me`, {
      headers: {
        Authorization: `Bearer ${currentToken}`,
      },
    });

    const data = await res.json();
    const user = data.user;
    userInfo.value = data.user;

    token.value = currentToken;
    userId.value = user.id;
    userName.value = user.nickname;
  } catch (err) {
    console.error("사용자 정보 요청 실패:", err);
    alert("로그인이 필요합니다.");
  }
};

/* 월별 통계 */
const getMonthlyStats = async (userId) => {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth() + 1;

  try {
    const res = await fetch(
      `/api/users/${userId}/records?year=${year}&month=${month}`,
      {
        headers: {
          Authorization: `Bearer ${token.value}`,
        },
      }
    );

    const data = await res.json();
    if (Array.isArray(data)) {
      const totalDistance = data.reduce(
        (sum, rec) => sum + (rec.distance || 0),
        0
      );
      const totalDuration = data.reduce(
        (sum, rec) => sum + (rec.duration || 0),
        0
      );

      monthlyStats.value.monthdistance =
        (totalDistance / 1000).toFixed(1) + "km";
      monthlyStats.value.monthduration = formatDuration(totalDuration);
    } else {
      console.warn("예상과 다른 응답 구조:", data);
    }
  } catch (err) {
    console.error("월별 러닝 정보 조회 실패:", err);
  }
};

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

/* 유저의 하루 러닝 기록 */
const getDayRoutes = async () => {
  let year, month;

  if (selectedDate.value) {
    year = selectedDate.value.getFullYear();
    month = selectedDate.value.getMonth() + 1;
  } else {
    const now = new Date();
    year = now.getFullYear();
    month = now.getMonth() + 1;
  }

  try {
    const res = await fetch(
      `/api/users/${userId.value}/records?year=${year}&month=${month}`,
      {
        headers: {
          Authorization: `Bearer ${token.value}`,
        },
      }
    );

    const data = await res.json();
    runningData.value = data;
  } catch (err) {
    console.error("하루 러닝 정보 조회 실패:", err);
  }
};

/* 유저의 모든 러닝 기록 */
const getAllRoutes = async () => {
  try {
    const res = await fetch(`/api/my/route`, {
      headers: {
        Authorization: `Bearer ${token.value}`,
      },
    });

    const data = await res.json();
    routes.value = data;
  } catch (err) {
    console.error("모든 러닝 정보 조회 실패:", err);
  }
};

const findEmoji = (label) => {
  if (!label) return "";
  const moodOptions = [
    { label: "기쁨", emoji: "😊" },
    { label: "뿌듯함", emoji: "💪" },
    { label: "아쉬움", emoji: "😕" },
    { label: "화남", emoji: "😠" },
    { label: "슬픔", emoji: "😢" },
    { label: "쏘쏘", emoji: "😐" },
  ];
  const found = moodOptions.find(
    (option) => option.label.trim() === label.trim()
  );
  return found ? found.emoji : "";
};

const reversedRoute = ref([]);
const reverseRoutes = async (routes) => {
  const reversedRoutes = computed(() => [...routes.value].reverse());
  return reversedRoutes;
};

// 컴포넌트 마운트 시 실행
onMounted(async () => {
  await getCurrentUser();
  await getMonthlyStats(userId.value);
  await getAllRoutes();
  await reverseRoutes();
  await getDayRoutes();
  generateCalendar();

  // 오늘 날짜에 러닝 기록이 있는지 확인하고 선택
  const today = calendarDays.value.find(
    (day) => day.date && isSameDay(day.date, currentDate.value)
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
  background-color: var(--background-color, #f5f5f5);
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
  color: var(--primary-color, #ff5722);
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
  color: var(--primary-color, #ff5722);
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
  color: var(--primary-color, #ff5722);
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
  border-bottom: 1px solid var(--border-color, #eee);
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
  background-color: #ff5722;
  color: white;
}

.calendar-day.today {
  border: 2px solid #ff5722;
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
  background-color: #ff5722;
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
  color: var(--primary-color, #ff5722);
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
  background-color: var(--primary-color, #ff5722);
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
  background-color: var(--primary-dark, #e64a19);
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
  background-color: var(--primary-color, #ff5722);
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
  background-color: var(--primary-dark, #e64a19);
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
  border-bottom: 1px solid var(--border-color, #eee);
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
  color: var(--primary-color, #ff5722);
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
