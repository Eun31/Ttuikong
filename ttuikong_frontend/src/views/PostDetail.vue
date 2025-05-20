<template>
  <div class="container">
    <!-- 로딩 인디케이터 -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>게시글을 불러오는 중...</p>
    </div>

    <div v-else-if="error" class="error-container">
      <p>{{ error }}</p>
      <button class="retry-btn" @click="fetchPostDetail">다시 시도</button>
      <button class="back-btn" @click="goBack">돌아가기</button>
    </div>

    <div v-else>

      <!-- 게시글 카드 -->
      <div class="post-card">
        <!-- 작성자 정보 -->
        <div class="user-profile">
          <img :src="getProfileImage()" alt="프로필" class="user-avatar">
          <div class="user-details">
            <div class="user-name">
              {{ post.userNickname || post.user_nickname }}
              <span v-if="isAuthor" class="badge">
                <i class="icon-check"></i>
              </span>
            </div>
            <div class="post-time">{{ formatDate(post.createdAt || post.created_at) }}</div>
          </div>
          <!-- 모든 사용자에게 옵션 메뉴 표시하되, 내용을 구분 -->
          <div class="post-options">
            <button class="post-options-btn" @click="toggleOptions">
              <!-- 점 세 개 표시는 CSS로 처리 -->
            </button>
            <div class="options-menu" :class="{ show: showOptions }">
              <!-- 작성자일 때만 수정/삭제 표시 -->
              <div v-if="isAuthor" class="option-item edit-option" @click="editPost">
                <i class="ri-edit-line"></i>
                <span>수정하기</span>
              </div>
              <div v-if="isAuthor" class="option-item delete-option" @click="deletePost">
                <i class="ri-delete-bin-line"></i>
                <span>삭제하기</span>
              </div>
              <!-- 작성자가 아닐 때는 신고 버튼만 표시 -->
              <div v-if="!isAuthor && token" class="option-item report-option" @click="reportPost">
                <i class="ri-flag-line"></i>
                <span>신고하기</span>
              </div>
              <!-- 로그인하지 않은 경우 -->
              <div v-if="!token" class="option-item login-option" @click="goToLogin">
                <i class="ri-login-box-line"></i>
                <span>로그인 필요</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 게시글 내용 -->
        <div class="post-content">
          <h2 class="post-title">{{ post.title }}</h2>
          <div class="post-body">{{ post.content }}</div>
          
          <div v-if="post.imageUrl || post.image_url" class="post-image">
            <img :src="getFullImageUrl(post.imageUrl || post.image_url)" :alt="post.title" @error="handleImageError">
          </div>
          
          <div v-if="post.location" class="post-location">
            <span class="location-icon">📍</span>
            <span>{{ post.location }}</span>
          </div>

          <div v-if="post.category" class="post-tags">
            <span class="tag">{{ post.category }}</span>
          </div>

          <div class="post-actions">
            <button class="action-btn share-action" @click="sharePost">
              <span class="action-icon">
                <i class="ri-share-forward-line"></i>
              </span>
              <span>공유하기</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import profileImg from '../assets/profile.png';
import defaultImg from '../assets/profile.png';

// 라우터와 라우트 가져오기
const router = useRouter();
const route = useRoute();

const token = localStorage.getItem('jwt');

// postId를 숫자로 변환하여 저장
const postId = ref(parseInt(route.params.id, 10));

// postId 유효성 검사
const isValidPostId = computed(() => {
  const valid = !isNaN(postId.value) && postId.value > 0;
  console.log('postId 유효성 검사 결과:', valid, 'postId:', postId.value);
  return valid;
});

// 반응형 데이터 정의
const loading = ref(true);
const error = ref(null);
const post = ref({});
const showOptions = ref(false);

// 현재 사용자 정보
const currentUser = ref({
  id: null,
  nickname: '',
  token: localStorage.getItem('jwt') || ''
});

// API 기본 URL
const API_URL = 'http://localhost:8080/api';

// 헤더에 토큰 설정
const authHeader = computed(() => {
  return currentUser.value.token ? 
    { 'Authorization': `Bearer ${currentUser.value.token}` } : {};
});

// 현재 사용자가 글 작성자인지 확인
const isAuthor = computed(() => {
  console.log(currentUser.value.id)
  console.log(post.value.userId)
  return currentUser.value.id && post.value.userId === currentUser.value.id;
});

// 이미지 URL을 완전한 URL로 변환하는 함수
function getFullImageUrl(imageUrl) {
  if (!imageUrl) return '';
  
  // 이미 완전한 URL인 경우 (http로 시작하는 경우)
  if (imageUrl.startsWith('http')) {
    return imageUrl;
  }
  
  // 상대 경로인 경우 백엔드 서버 URL을 앞에 붙임
  return `http://localhost:8080${imageUrl}`;
}

// 라우트 파라미터 변경 감지 (뒤로 가기 등으로 다른 게시글로 이동할 때)
watch(() => route.params.id, (newId) => {
  console.log('라우트 ID 변경 감지:', newId);
  postId.value = parseInt(newId, 10);
  
  // ID가 유효하면 게시글 다시 로드
  if (isValidPostId.value) {
    fetchPostDetail();
  } else {
    error.value = '유효하지 않은 게시글 ID입니다.';
    loading.value = false;
  }
});

// 로그인 사용자 정보 가져오기
async function fetchCurrentUser() {
  if (!token) {
    console.log('로그인되지 않은 사용자입니다.');
    return;
  }
  
  try {
    const response = await axios.get(`${API_URL}/users/me`, {
      headers: authHeader.value
    });
    
    console.log('API 응답 원본:', response.data);
    
    // 응답 데이터 구조에 맞게 수정: user 객체 안에 실제 데이터가 있음
    const userData = response.data.user || response.data; // user 객체가 있으면 사용, 없으면 직접 접근
    
    currentUser.value.id = userData.id;
    currentUser.value.nickname = userData.nickname || userData.email || '사용자';
    
    console.log('로그인 사용자 정보 로드 완료:', {
      id: currentUser.value.id,
      nickname: currentUser.value.nickname,
      원본_응답: response.data,
      사용된_유저데이터: userData
    });
  } catch (err) {
    console.error('사용자 정보를 불러오는 중 오류가 발생했습니다:', err);
    console.error('오류 상세:', err.response?.data);
    
    // 토큰이 만료되었거나 유효하지 않은 경우
    if (err.response && (err.response.status === 401 || err.response.status === 403)) {
      console.log('토큰이 만료되었거나 유효하지 않음');
      localStorage.removeItem('jwt');
      currentUser.value.id = null;
      currentUser.value.nickname = '';
    }
  }
}

// 게시글 상세 정보 불러오기
async function fetchPostDetail() {
  loading.value = true;
  error.value = null;
  
  // 유효한 ID 확인
  if (!isValidPostId.value) {
    error.value = '유효하지 않은 게시글 ID입니다.';
    loading.value = false;
    return;
  }
  
  try {
    // API 호출 URL 로깅
    const apiUrl = `${API_URL}/board/${postId.value}`;
    console.log('API 호출 URL:', apiUrl);
    
    const response = await axios.get(apiUrl);
    console.log('API 응답:', response.data);
    
    // 응답 데이터가 null이거나 undefined인 경우 처리
    if (!response.data) {
      throw new Error('게시글 데이터가 없습니다.');
    }
    
    post.value = response.data;
    
    // 이미지 URL 로깅
    const imageUrl = response.data.imageUrl || response.data.image_url;
    if (imageUrl) {
      console.log('원본 이미지 URL:', imageUrl);
      console.log('변환된 이미지 URL:', getFullImageUrl(imageUrl));
    }
    
    // 게시글 정보와 현재 사용자 정보 디버깅
    console.log('=== 게시글 로드 완료 디버깅 ===');
    console.log('전체 게시글 데이터:', response.data);
    console.log('게시글 작성자 정보:', {
      userId: response.data.userId,
      user_id: response.data.user_id,
      userNickname: response.data.userNickname,
      user_nickname: response.data.user_nickname
    });
    console.log('현재 로그인 사용자:', {
      id: currentUser.value.id,
      nickname: currentUser.value.nickname
    });
    console.log('isAuthor 계산 결과:', isAuthor.value);
  } catch (err) {
    console.error('게시글 조회 중 오류가 발생했습니다:', err);
    
    // 상세 오류 정보 로깅
    if (err.response) {
      console.log('오류 상태:', err.response.status);
      console.log('오류 데이터:', err.response.data);
      
      // 404 오류인 경우 사용자 친화적 메시지
      if (err.response.status === 404) {
        error.value = '게시글을 찾을 수 없습니다.';
      } else {
        error.value = '게시글을 불러오는데 실패했습니다. 다시 시도해 주세요.';
      }
    } else {
      error.value = err.message || '게시글을 불러오는데 실패했습니다. 다시 시도해 주세요.';
    }
  } finally {
    loading.value = false;
  }
}

// 옵션 메뉴 토글
function toggleOptions() {
  showOptions.value = !showOptions.value;
}

// 뒤로 가기
function goBack() {
  router.push('/board');
}

// 게시글 수정
function editPost() {
  // 수정 페이지로 이동
  router.push(`/board/edit/${postId.value}`);
  toggleOptions();
}

// 로그인 페이지로 이동
function goToLogin() {
  router.push('/login');
  toggleOptions();
}

// 게시글 삭제
async function deletePost() {
  console.log('삭제 버튼 클릭됨');
  console.log('현재 사용자 ID:', currentUser.value.id);
  console.log('토큰 존재:', !!token);
  
  if (!token) {
    alert('로그인이 필요한 기능입니다.');
    return;
  }
  
  if (confirm('정말 이 게시글을 삭제하시겠습니까?')) {
    try {
      await axios.delete(`${API_URL}/board/${postId.value}`, {
        headers: authHeader.value
      });
      alert('게시글이 삭제되었습니다.');
      goBack();
    } catch (err) {
      console.error('게시글 삭제 중 오류가 발생했습니다:', err);
      
      if (err.response && err.response.status === 401) {
        alert('로그인이 만료되었습니다. 다시 로그인해 주세요.');
        localStorage.removeItem('jwt');
        router.push('/login');
      } else {
        alert('게시글 삭제에 실패했습니다. 다시 시도해 주세요.');
      }
    }
  }
  toggleOptions();
}

// 게시글 신고
function reportPost() {
  alert('게시글이 신고되었습니다.');
  toggleOptions();
}

// 게시글 공유
function sharePost() {
  alert('공유 기능은 준비 중입니다.');
}

// 날짜 형식 변환
function formatDate(dateString) {
  if (!dateString) return '';
  
  const now = new Date();
  const date = new Date(dateString);
  
  // 날짜 차이 계산 (밀리초)
  const diff = now - date;
  
  // 1분 미만
  if (diff < 60 * 1000) {
    return '방금 전';
  }
  
  // 1시간 미만
  if (diff < 60 * 60 * 1000) {
    const minutes = Math.floor(diff / (60 * 1000));
    return `${minutes}분 전`;
  }
  
  // 오늘
  if (date.toDateString() === now.toDateString()) {
    return `오늘 ${date.getHours()}시 ${date.getMinutes()}분`;
  }
  
  // 어제
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (date.toDateString() === yesterday.toDateString()) {
    return `어제 ${date.getHours()}시 ${date.getMinutes()}분`;
  }
  
  // 1주일 이내
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = Math.floor(diff / (24 * 60 * 60 * 1000));
    return `${days}일 전`;
  }
  
  // 그 외 날짜 형식
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

// 프로필 이미지 얻기
function getProfileImage() {
  return profileImg;
}

// 이미지 로딩 에러 처리
function handleImageError(event) {
  console.error('이미지 로딩 실패:', event.target.src);
  event.target.src = defaultImg;
}

// 컴포넌트 마운트 시 실행
onMounted(async () => {
  console.log('=== PostDetail 컴포넌트 마운트 ===');
  console.log('ID 파라미터:', route.params.id);
  console.log('사용 가능한 토큰:', !!token);
  console.log('토큰 값 (일부):', token ? token.substring(0, 20) + '...' : 'null');
  
  // postId 유효성 검사
  if (!isValidPostId.value) {
    error.value = '유효하지 않은 게시글 ID입니다.';
    loading.value = false;
    return;
  }
  
  // 로그인 사용자 정보 가져오기
  await fetchCurrentUser();
  
  // 게시글 상세 정보 가져오기
  await fetchPostDetail();
});
</script>


<style scoped>
.container {
  max-width: 100%;
  margin: 0 auto;
  background-color: var(--background-color);
  min-height: 100vh;
  padding: 16px;
  padding-bottom: 70px;
}

@media (min-width: 768px) {
  .container {
    max-width: 700px;
    margin: 0 auto;
  }
}

/* 헤더 스타일 - 테마 맞춤 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e0e0e0;
}

.header-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: white;
  border: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #757575;
  cursor: pointer;
  transition: all 0.2s ease;
}

.header-btn:hover {
  border-color: #FF7E47;
  color: #FF7E47;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(255, 126, 71, 0.2);
}

.header-btn:active {
  transform: translateY(0);
}

/* 메뉴 버튼 스타일 개선 */
.options-btn {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: white;
  border: 1px solid #e0e0e0;
  cursor: pointer;
  transition: all 0.2s ease;
}

.options-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 4px;
  height: 4px;
  background: #757575;
  border-radius: 50%;
  box-shadow: 
    0 -6px 0 #757575,
    0 6px 0 #757575;
  transition: all 0.2s ease;
}

.options-btn:hover {
  border-color: #FF7E47;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(255, 126, 71, 0.2);
}

.options-btn:hover::before {
  background: #FF7E47;
  box-shadow: 
    0 -6px 0 #FF7E47,
    0 6px 0 #FF7E47;
}

.header-center {
  flex: 1;
  text-align: center;
}

.header-title {
  font-size: 28px;
  font-weight: 700;
  color: #FF7E47;
  margin: 0;
  position: relative;
  display: inline-block;
}

.header-title::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 3px;
  background-color: #FFF8F2;
  border-radius: 2px;
}

.header-subtitle {
  font-size: 12px;
  color: #757575;
  font-weight: 400;
  margin-top: 8px;
}

/* 게시글 카드 */
.post-card {
  background-color: var(--card-color);
  border-radius: 20px;
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  animation: fadeIn 0.4s ease-out;
  border: 1px solid rgba(255, 87, 34, 0.08);
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

/* 작성자 정보 */
.user-profile {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
  position: relative;
  background: linear-gradient(135deg, rgba(255, 87, 34, 0.02) 0%, transparent 100%);
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 14px;
  object-fit: cover;
  border: 2px solid rgba(255, 87, 34, 0.15);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-details {
  flex: 1;
}

.user-name {
  font-weight: 600;
  display: flex;
  align-items: center;
  color: var(--dark-text);
  font-size: 16px;
}

.badge {
  color: var(--primary-color);
  margin-left: 6px;
  font-size: 14px;
  background: rgba(255, 87, 34, 0.1);
  padding: 2px 6px;
  border-radius: 6px;
}

.post-time {
  font-size: 13px;
  color: var(--medium-text);
  margin-top: 2px;
}

.post-options {
  position: relative;
}

.post-options-btn {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: white;
  border: 1px solid #e0e0e0;
  cursor: pointer;
  transition: all 0.2s ease;
}

.post-options-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 3px;
  height: 3px;
  background: #757575;
  border-radius: 50%;
  box-shadow: 
    0 -5px 0 #757575,
    0 5px 0 #757575;
  transition: all 0.2s ease;
}

.post-options-btn:hover {
  border-color: #FF7E47;
  background: rgba(255, 126, 71, 0.05);
  transform: scale(1.05);
}

.post-options-btn:hover::before {
  background: #FF7E47;
  box-shadow: 
    0 -5px 0 #FF7E47,
    0 5px 0 #FF7E47;
}

.options-menu {
  position: absolute;
  top: 100%;
  right: 0;
  width: 160px;
  background-color: var(--card-color);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  display: none;
  z-index: 50;
  overflow: hidden;
  border: 1px solid var(--border-color);
  margin-top: 8px;
}

.options-menu.show {
  display: block;
  animation: slideDown 0.2s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.option-item {
  padding: 14px 16px;
  font-size: 14px;
  color: var(--dark-text);
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 10px;
}

.option-item:hover {
  background-color: rgba(255, 87, 34, 0.05);
}

.option-item i {
  font-size: 16px;
  color: var(--medium-text);
}

.edit-option:hover {
  background-color: rgba(34, 197, 94, 0.1);
}

.edit-option:hover i {
  color: #22c55e;
}

.delete-option:hover {
  background-color: rgba(239, 68, 68, 0.1);
}

.delete-option:hover i {
  color: #ef4444;
}

.report-option:hover {
  background-color: rgba(245, 158, 11, 0.1);
}

.report-option:hover i {
  color: #f59e0b;
}

/* 게시글 내용 */
.post-content {
  padding: 24px;
}

.post-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 16px;
  line-height: 1.4;
  color: var(--dark-text);
}

.post-body {
  font-size: 16px;
  margin-bottom: 20px;
  line-height: 1.6;
  color: var(--dark-text);
}

.post-image {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.post-image img {
  width: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s ease;
}

.post-image:hover img {
  transform: scale(1.02);
}

.post-location {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: var(--medium-text);
  margin-bottom: 16px;
  background: rgba(255, 87, 34, 0.05);
  padding: 8px 12px;
  border-radius: 8px;
  width: fit-content;
}

.location-icon {
  margin-right: 8px;
  color: var(--primary-color);
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.tag {
  background: linear-gradient(135deg, rgba(255, 87, 34, 0.1) 0%, rgba(255, 87, 34, 0.05) 100%);
  color: var(--primary-color);
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid rgba(255, 87, 34, 0.15);
}

.post-actions {
  display: flex;
  border-top: 1px solid var(--border-color);
  padding-top: 16px;
  margin-top: 20px;
}

.action-btn {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(255, 87, 34, 0.08) 0%, rgba(255, 87, 34, 0.05) 100%);
  border: 1px solid rgba(255, 87, 34, 0.15);
  cursor: pointer;
  color: var(--primary-color);
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: linear-gradient(135deg, rgba(255, 87, 34, 0.15) 0%, rgba(255, 87, 34, 0.1) 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 87, 34, 0.2);
}

.action-icon {
  font-size: 18px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border-left-color: var(--primary-color);
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-container {
  text-align: center;
  padding: 40px 16px;
  color: #e74c3c;
}

.retry-btn {
  background: var(--primary-color);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  margin: 8px;
  transition: all 0.3s ease;
}

.retry-btn:hover{
  background: #e65100;
  transform: translateY(-2px);
}
</style>