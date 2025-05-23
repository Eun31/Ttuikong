<template>
  <div class="container">
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
      <div class="post-card">
        <div class="user-profile" @click.stop="goToUserProfile(post.userId)">
          <img :src="getProfileImage()" alt="프로필" class="user-avatar">
          <div class="user-details">
            <div class="user-name">
              {{ post.userNickname }}
              <span v-if="isAuthor" class="badge">
                <i class="icon-check"></i>
              </span>
            </div>
            <div class="post-time">{{ formatDate(post.createdAt || post.created_at) }}</div>
          </div>
          <div v-if="isAuthor" class="post-options">
            <button class="post-options-btn" @click="toggleOptions">
            </button>
            <div class="options-menu" :class="{ show: showOptions }">
              <div class="option-item edit-option" @click="editPost">
                <span>수정하기</span>
              </div>
              <div class="option-item delete-option" @click="deletePost">
                <span>삭제하기</span>
              </div>
            </div>
          </div>
        </div>

        <div class="post-content">
          <h2 class="post-title">{{ post.title }}</h2>
          <div class="post-body">{{ post.content }}</div>
          
          <div v-if="validImageUrl" class="post-image">
            <img 
              :src="validImageUrl" 
              @error="handleImageError"
              :alt="post.title"
            >
          </div>
          
          <div v-if="post.location" class="post-location">
            <span class="location-icon">📍</span>
            <span>{{ post.location }}</span>
          </div>

          <div v-if="post.category" class="post-tags">
            <span class="tag">{{ post.category }}</span>
          </div>

          <div class="post-actions">
            <button 
              class="heart-btn" 
              :class="{ liked: isLiked, disabled: !token }"
              @click="toggleLike"
              :disabled="likeLoading"
            >
              <span v-if="likeLoading" class="spinning">🔄</span>
              <span v-else-if="isLiked" class="heart-emoji liked">❤️</span>
              <span v-else class="heart-emoji">♡</span>
            </button>
            
            <div class="like-text-container">
              <span class="like-status-text clickable" @click="openLikeUsersModal" >
                {{ likeCount }}명이 이 게시글을 좋아합니다
              </span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="showLikeUsersModal" class="modal-overlay" @click="closeLikeUsersModal">
        <div class="like-users-modal" @click.stop>
          <div class="modal-header">
            <h3>좋아요한 사용자</h3>
            <button @click="closeLikeUsersModal" class="modal-close-btn">✕</button>
          </div>
          
          <div class="modal-content">
            <div v-if="loadingLikeUsers" class="modal-loading">
              <div class="spinner"></div>
              <p>사용자 목록을 불러오는 중...</p>
            </div>
            
            <div v-else-if="likeUsers.length === 0" class="modal-empty">
              <div class="empty-icon">💔</div>
              <p>좋아요한 사용자 정보를 불러올 수 없습니다.</p>
            </div>
            
            <div v-else class="like-users-grid">
              <div v-for="user in likeUsers" :key="user.id" class="like-user-card">
                <img :src="getProfileImage()" alt="프로필" class="user-card-avatar">
                <div class="user-card-info">
                  <div class="user-card-name">{{ user.nickname || user.email || '익명' }}</div>
                  <div class="user-card-level">{{ user.level || '일반 사용자' }}</div>
                </div>
                <div class="user-card-heart">❤️</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="comments-section">
        <div class="comments-header">
          <h3>댓글 {{ comments.length }}</h3>
        </div>

        <div v-if="token" class="comment-form">
          <div class="comment-input-wrapper">
            <img :src="getProfileImage()" alt="프로필" class="comment-user-avatar">
            <div class="comment-input-container">
              <div class="comment-input-row">
                <textarea 
                  v-model="newComment"
                  placeholder="댓글을 입력하세요..."
                  class="comment-input"
                  rows="1"
                  @input="autoResize"
                  @keydown.enter="handleEnterKey"
                  ref="commentTextarea"
                ></textarea>
                <button 
                  @click="submitComment" 
                  :disabled="!newComment.trim() || submittingComment"
                  class="submit-comment-btn"
                >
                  <span v-if="submittingComment">
                    <span class="spinning">🔄</span>
                  </span>
                  <span v-else>
                  </span>
                  등록
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="comment-login-prompt">
          <p>댓글을 작성하려면 로그인이 필요합니다.</p>
          <button @click="goToLogin" class="login-btn">로그인하기</button>
        </div>

        <div class="comments-list">
          <div v-if="loadingComments" class="comments-loading">
            <div class="spinner small"></div>
            <span>댓글을 불러오는 중...</span>
          </div>

          <div v-else-if="comments.length === 0" class="no-comments">
            <p>아직 댓글이 없습니다. 첫 번째 댓글을 작성해보세요!</p>
          </div>

          <div v-else>
            <div 
              v-for="comment in comments" 
              :key="comment.id"
              class="comment-item"
            >
              <img :src="getProfileImage()" alt="프로필" class="comment-avatar">
              <div class="comment-content">
                <div class="comment-header">
                  <span class="comment-author" @click.stop="goToUserProfile(comment.userId)">{{ comment.userNickname }}</span>
                  <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
                  
                  <div v-if="isCommentAuthor(comment)" class="comment-options">
                    <button class="comment-options-btn" @click="toggleCommentOptions(comment.id)">
                    </button>
                    <div 
                      class="comment-options-menu" 
                      :class="{ show: comment.showOptions }"
                    >
                      <div class="comment-option-item" @click="startEditComment(comment)">
                        <span>수정</span>
                      </div>
                      <div class="comment-option-item delete" @click="deleteComment(comment.id)">
                        <span>삭제</span>
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="!comment.isEditing" class="comment-text">
                  {{ comment.content }}
                </div>

                <div v-else class="comment-edit-form">
                  <textarea 
                    v-model="comment.editContent"
                    class="comment-edit-input"
                    rows="2"
                  ></textarea>
                  <div class="comment-edit-actions">
                    <button @click="saveEditComment(comment)" class="save-btn">저장</button>
                    <button @click="cancelEditComment(comment)" class="cancel-btn">취소</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted} from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import profileImg from '../assets/profile.png';

const router = useRouter();
const route = useRoute();

const token = localStorage.getItem('jwt');
const postId = ref(parseInt(route.params.id, 10));

const isValidPostId = computed(() => {
  const valid = !isNaN(postId.value) && postId.value > 0;
  console.log('postId 유효성 검사 결과:', valid, 'postId:', postId.value);
  return valid;
});

const goToUserProfile = (userId) => {
  if (userId === currentUser.value.id) {
    router.push('/profile');
  } 
  else {
    router.push(`/profile/${userId}`);
  }
};

const loading = ref(true);
const error = ref(null);
const post = ref({});
const showOptions = ref(false);

const isLiked = ref(false);
const likeCount = ref(0);
const likeLoading = ref(false);
const showLikeUsersModal = ref(false)
const likeUsers = ref([]);
const loadingLikeUsers = ref(false);

const comments = ref([]);
const newComment = ref('');
const loadingComments = ref(false);
const submittingComment = ref(false);
const commentTextarea = ref(null);

const currentUser = ref({
  id: null,
  nickname: '',
  token: localStorage.getItem('jwt') || ''
});

const API_URL = 'http://localhost:8080/api';

const authHeader = computed(() => {
  return currentUser.value.token ? 
    { 'Authorization': `Bearer ${currentUser.value.token}` } : {};
});

const isAuthor = computed(() => {
  return currentUser.value.id && post.value.userId === currentUser.value.id;
});

const isCommentAuthor = (comment) => {
  return currentUser.value.id && (comment.userId === currentUser.value.id);
};

const validImageUrl = computed(() => {
  if (!post.value?.imageUrl) {
    return null;
  }
  
  const imageUrl = post.value.imageUrl || post.value.image_url;
  
  if (!imageUrl || imageUrl.trim() === '') {
    return null;
  }
  
  if (imageUrl.startsWith('/uploads/')) {
    return `${API_URL.replace('/api', '')}${imageUrl}`;
  }
  
  return imageUrl;
});

const handleImageError = (event) => {
  console.warn('이미지 로드 실패:', event.target.src);
  event.target.style.display = 'none';
};

function getFullImageUrl(imageUrl) {
  if (!imageUrl) return '';
  
  if (imageUrl.startsWith('http')) {
    return imageUrl;
  }
  
  return `http://localhost:8080${imageUrl}`;
}

async function toggleLike() {
  if (!token) {
    alert('로그인이 필요한 기능입니다.');
    return;
  }
  
  if (likeLoading.value) return;
  
  likeLoading.value = true;
  
  try {
    await axios.post(`${API_URL}/board/${postId.value}/like`, {}, {
      headers: authHeader.value
    });
    
    await Promise.all([
      checkLikeStatus(),
      fetchLikeCount()
    ]);
    
    console.log('좋아요 토글 완료');
  } catch (err) {
    console.error('좋아요 처리 중 오류가 발생했습니다:', err);
    
    if (err.response && err.response.status === 401) {
      alert('로그인이 만료되었습니다. 다시 로그인해 주세요.');
      localStorage.removeItem('jwt');
      router.push('/login');
    } else {
      alert('좋아요 처리에 실패했습니다. 다시 시도해 주세요.');
    }
  } finally {
    likeLoading.value = false;
  }
}

async function checkLikeStatus() {
  if (!token) {
    console.log('로그인되지 않은 사용자입니다.');
    isLiked.value = false;
    return;
  }
  
  try {
    const response = await axios.get(`${API_URL}/board/${postId.value}/like/status`, {
      headers: authHeader.value
    });

    isLiked.value = response.data;
    console.log('좋아요 상태 확인 완료:', isLiked.value);
  } catch (err) {
    console.error('좋아요 상태 확인 중 오류가 발생했습니다:', err);
    isLiked.value = false;
  }
}

async function fetchLikeCount() {
  try {
    const response = await axios.get(`${API_URL}/board/${postId.value}/like/count`);
    likeCount.value = response.data || 0;
    console.log('좋아요 개수 로드 완료:', likeCount.value);
  } catch (err) {
    console.error('좋아요 개수를 불러오는 중 오류가 발생했습니다:', err);
    likeCount.value = 0;
  }
}

async function fetchLikeUsers() {
  try {
    const response = await axios.get(`${API_URL}/board/${postId.value}/like/users`);
    return response.data || [];
  } catch (err) {
    console.error('좋아요한 사용자 목록을 불러오는 중 오류가 발생했습니다:', err);
    return [];
  }
}

async function openLikeUsersModal() {
  if (likeCount.value === 0) return;
  
  showLikeUsersModal.value = true;
  loadingLikeUsers.value = true;
  
  try {
    const users = await fetchLikeUsers();
    likeUsers.value = users;
  } catch (err) {
    console.error('좋아요한 사용자 목록 표시 중 오류:', err);
    likeUsers.value = [];
  } finally {
    loadingLikeUsers.value = false;
  }
}

function closeLikeUsersModal() {
  showLikeUsersModal.value = false;
  likeUsers.value = [];
}

async function fetchCurrentUser() {
  if (!token) {
    console.log('로그인되지 않은 사용자입니다.');
    return;
  }
  
  try {
    const response = await axios.get(`${API_URL}/users/me`, {
      headers: authHeader.value
    });

    const userData = response.data.user || response.data;
    
    currentUser.value.id = userData.id;
    currentUser.value.nickname = userData.nickname || userData.email || '사용자';
    
  } catch (err) {
    console.error('사용자 정보를 불러오는 중 오류가 발생했습니다:', err);
    console.error('오류 상세:', err.response?.data);

    if (err.response && (err.response.status === 401 || err.response.status === 403)) {
      console.log('토큰이 만료되었거나 유효하지 않음');
      localStorage.removeItem('jwt');
      currentUser.value.id = null;
      currentUser.value.nickname = '';
    }
  }
}

async function fetchPostDetail() {
  loading.value = true;
  error.value = null;
  
  if (!isValidPostId.value) {
    error.value = '유효하지 않은 게시글 ID입니다.';
    loading.value = false;
    return;
  }
  
  try {
    const apiUrl = `${API_URL}/board/${postId.value}`;
    const response = await axios.get(apiUrl);

    if (!response.data) {
      throw new Error('게시글 데이터가 없습니다.');
    }
    
    post.value = response.data;

    const imageUrl = response.data.imageUrl || response.data.image_url;
    if (imageUrl) {
      console.log('원본 이미지 URL:', imageUrl);
      console.log('변환된 이미지 URL:', getFullImageUrl(imageUrl));
    }
   
  } catch (err) {
    console.error('게시글 조회 중 오류가 발생했습니다:', err);
  } finally {
    loading.value = false;
  }
}

async function fetchComments() {
  loadingComments.value = true;
  
  try {
    const response = await axios.get(`${API_URL}/board/${postId.value}/comment`);
    comments.value = response.data.map(comment => ({
      ...comment,
      showOptions: false,
      isEditing: false,
      editContent: comment.content
    }));
    console.log('댓글 목록 로드 완료:', comments.value);
  } catch (err) {
    console.error('댓글을 불러오는 중 오류가 발생했습니다:', err);
  } finally {
    loadingComments.value = false;
  }
}

async function submitComment() {
  if (!newComment.value.trim() || submittingComment.value) return;
  
  submittingComment.value = true;
  
  try {
    await axios.post(`${API_URL}/board/${postId.value}/comment`, {
      content: newComment.value.trim()
    }, {
      headers: authHeader.value
    });
  
    await fetchComments();
    
    newComment.value = '';

    if (commentTextarea.value) {
      commentTextarea.value.style.height = 'auto';
    }
    
    console.log('댓글 작성 및 목록 갱신 완료');
  } catch (err) {
    console.error('댓글 작성 중 오류가 발생했습니다:', err);
    alert('댓글 작성에 실패했습니다. 다시 시도해 주세요.');
  } finally {
    submittingComment.value = false;
  }
}

function handleEnterKey(event) {
  if (event.shiftKey) {
    return;
  }
  event.preventDefault();
  submitComment();
}

function autoResize(event) {
  const textarea = event.target;
  textarea.style.height = 'auto';
  textarea.style.height = textarea.scrollHeight + 'px';
}

function toggleCommentOptions(commentId) {
  comments.value = comments.value.map(comment => ({
    ...comment,
    showOptions: comment.id === commentId ? !comment.showOptions : false
  }));
}

// 댓글 수정 시작
function startEditComment(comment) {
  comment.isEditing = true;
  comment.editContent = comment.content;
  comment.showOptions = false;
}

// 댓글 수정 저장
async function saveEditComment(comment) {
  if (!comment.editContent.trim()) return;
  
  try {
    await axios.put(`${API_URL}/board/${postId.value}/comment/${comment.id}`, {
      content: comment.editContent.trim()
    }, {
      headers: authHeader.value
    });
    
    comment.content = comment.editContent.trim();
    comment.isEditing = false;
    
    console.log('댓글 수정 완료:', comment);
  } catch (err) {
    console.error('댓글 수정 중 오류가 발생했습니다:', err);
    alert('댓글 수정에 실패했습니다. 다시 시도해 주세요.');
  }
}

// 댓글 수정 취소
function cancelEditComment(comment) {
  comment.isEditing = false;
  comment.editContent = comment.content;
}

// 댓글 삭제
async function deleteComment(commentId) {
  if (!confirm('정말 이 댓글을 삭제하시겠습니까?')) return;
  
  try {
    await axios.delete(`${API_URL}/board/${postId.value}/comment/${commentId}`, {
      headers: authHeader.value
    });
    
    comments.value = comments.value.filter(comment => comment.id !== commentId);
    console.log('댓글 삭제 완료:', commentId);
  } catch (err) {
    console.error('댓글 삭제 중 오류가 발생했습니다:', err);
    alert('댓글 삭제에 실패했습니다. 다시 시도해 주세요.');
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

function getProfileImage() {
  return profileImg;
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
  
  // 댓글 목록 가져오기
  await fetchComments();
  
  // 좋아요 관련 데이터 가져오기
  await fetchLikeCount();
  await checkLikeStatus();
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

.option-item span:first-child {
  font-size: 16px;
}

.comment-option-item span:first-child {
  font-size: 14px;
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
  gap: 12px;
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

.action-btn.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.action-btn.disabled:hover {
  transform: none;
  box-shadow: none;
}

/* 좋아요 버튼 스타일 */
.like-action.liked {
  background: linear-gradient(135deg, rgba(255, 87, 34, 0.2) 0%, rgba(255, 87, 34, 0.15) 100%);
  border-color: var(--primary-color);
  color: #d63031;
}

.like-action.liked .action-icon {
  color: #d63031;
}

.like-action.liked:hover {
  background: linear-gradient(135deg, rgba(255, 87, 34, 0.3) 0%, rgba(255, 87, 34, 0.2) 100%);
  box-shadow: 0 4px 16px rgba(255, 87, 34, 0.3);
}

.action-icon {
  font-size: 18px;
  transition: all 0.2s ease;
}

/* 하트 이모지 스타일 */
.heart-emoji {
  font-size: 18px;
  transition: all 0.2s ease;
  display: inline-block;
}

.heart-emoji:hover {
  transform: scale(1.2);
}

.heart-emoji.liked {
  animation: heartBeat 0.3s ease-in-out;
}

@keyframes heartBeat {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

.spinning {
  animation: spin 1s linear infinite;
  display: inline-block;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 좋아요 개수 클릭 가능한 텍스트 스타일 */
.like-count-text.clickable {
  cursor: pointer;
  text-decoration: underline;
  text-decoration-color: transparent;
  transition: text-decoration-color 0.2s ease;
}

.like-count-text.clickable:hover {
  text-decoration-color: currentColor;
}

/* 댓글 섹션 스타일 */
.comments-section {
  background-color: var(--card-color);
  border-radius: 20px;
  overflow: visible;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 87, 34, 0.08);
}

.comments-header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--border-color);
  background: linear-gradient(135deg, rgba(255, 87, 34, 0.02) 0%, transparent 100%);
}

.comments-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--dark-text);
}

/* 댓글 작성 폼 */
.comment-form {
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  background: rgba(255, 87, 34, 0.01);
}

.comment-input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.comment-user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 87, 34, 0.15);
  flex-shrink: 0;
}

.comment-input-container {
  flex: 1;
  position: relative;
}

.comment-input-row {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.comment-input {
  flex: 1;
  min-height: 40px;
  max-height: 120px;
  padding: 10px 14px 10px 14px;
  padding-right: 80px; /* 버튼 공간 확보 */
  border: 1px solid var(--border-color);
  border-radius: 20px;
  resize: none;
  font-family: inherit;
  font-size: 12px;
  line-height: 1.4;
  background: white;
  transition: all 0.2s ease;
  overflow: hidden;
  scrollbar-width: none;
  -ms-overflow-style: none;
  width: 100%;
  box-sizing: border-box;
}

.comment-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(255, 87, 34, 0.1);
}

.comment-input::placeholder {
  color: var(--medium-text);
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.submit-comment-btn {
  position: absolute;
  right: 4px;
  bottom: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 12px;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  min-width: 60px;
  height: 32px;
}

.submit-comment-btn:hover:not(:disabled) {
  background: #e85d2a;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 87, 34, 0.3);
}

.submit-comment-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
  transform: none;
}

/* 로그인 프롬프트 */
.comment-login-prompt {
  padding: 20px 24px;
  text-align: center;
  border-bottom: 1px solid var(--border-color);
  background: rgba(255, 87, 34, 0.02);
}

.comment-login-prompt p {
  margin: 0 0 16px;
  color: var(--medium-text);
}

.login-btn {
  padding: 10px 20px;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.login-btn:hover {
  background: #e85d2a;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 87, 34, 0.3);
}

/* 댓글 목록 */
.comments-list {
  padding: 20px 24px;
}

.comments-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px 0;
  color: var(--medium-text);
}

.no-comments {
  text-align: center;
  padding: 40px 0;
  color: var(--medium-text);
}

.no-comments p {
  margin: 0;
}

/* 댓글 아이템 */
.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 87, 34, 0.15);
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  position: relative;
}

.comment-author {
  font-weight: 600;
  color: var(--dark-text);
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.comment-author:hover {
  color: var(--primary-color); /* 추가 */
}

.comment-time {
  font-size: 12px;
  color: var(--medium-text);
}

.comment-options {
  position: relative;
  margin-left: auto;
}

.comment-options-btn {
  position: relative;
  width: 28px;
  height: 28px;
  border: none;
  background: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  color: var(--medium-text);
}

.comment-options-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 3px;
  height: 3px;
  background: #999;
  border-radius: 50%;
  box-shadow: 
    0 -5px 0 #999,
    0 5px 0 #999;
  transition: all 0.2s ease;
}

.comment-options-btn:hover {
  background: rgba(255, 87, 34, 0.1);
  color: var(--primary-color);
}

.comment-options-btn:hover::before {
  background: var(--primary-color);
  box-shadow: 
    0 -5px 0 var(--primary-color),
    0 5px 0 var(--primary-color);
}

.comment-options-btn:hover {
  background: rgba(255, 87, 34, 0.1);
  color: var(--primary-color);
}

.comment-options-menu {
  position: absolute;
  top: 100%;
  right: 0;
  width: 120px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  display: none;
  z-index: 100;
  overflow: hidden;
  border: 1px solid var(--border-color);
  margin-top: 4px;
}

.comment-options-menu.show {
  display: block;
  animation: slideDown 0.2s ease-out;
}

.comment-option-item {
  padding: 10px 12px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--dark-text);
}

.comment-option-item:hover {
  background: rgba(255, 87, 34, 0.05);
}

.comment-option-item.delete:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.comment-text {
  font-size: 14px;
  line-height: 1.5;
  color: var(--dark-text);
}

/* 댓글 수정 폼 */
.comment-edit-form {
  margin-top: 8px;
}

.comment-edit-input {
  width: 100%;
  min-height: 50px;
  padding: 8px 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  resize: none;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
  background: white;
}

.comment-edit-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(255, 87, 34, 0.1);
}

.comment-edit-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.save-btn, .cancel-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.save-btn {
  background: var(--primary-color);
  color: white;
}

.save-btn:hover {
  background: #e85d2a;
}

.cancel-btn {
  background: #f5f5f5;
  color: var(--dark-text);
}

.cancel-btn:hover {
  background: #e5e5e5;
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

.spinner.small {
  width: 20px;
  height: 20px;
  border-width: 2px;
  margin-bottom: 0;
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  gap: 16px;
}

.retry-btn, .back-btn {
  padding: 10px 20px;
  border: 1px solid var(--primary-color);
  border-radius: 8px;
  background: white;
  color: var(--primary-color);
  cursor: pointer;
  transition: all 0.2s ease;
}

.retry-btn:hover, .back-btn:hover {
  background: var(--primary-color);
  color: white;
}

/* 모바일 반응형 */
@media (max-width: 480px) {
  .comment-input {
    padding-right: 70px; /* 모바일에서 버튼 공간 조정 */
  }
  
  .submit-comment-btn {
    min-width: 55px;
    padding: 6px 10px;
    font-size: 11px;
    gap: 2px;
  }
  
  .btn-text {
    display: none; /* 모바일에서 텍스트 숨김 */
  }
}

@media (max-width: 360px) {
  .comment-input {
    padding-right: 50px; /* 더 작은 화면에서 조정 */
  }
  
  .submit-comment-btn {
    min-width: 40px;
    padding: 6px 8px;
  }
}

/* 좋아요 사용자 목록 섹션 */
.like-users-section {
  background-color: var(--card-color);
  border-radius: 16px;
  margin-top: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 87, 34, 0.08);
  animation: slideDown 0.3s ease-out;
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

.like-users-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
  background: linear-gradient(135deg, rgba(255, 87, 34, 0.02) 0%, transparent 100%);
}

.like-users-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--dark-text);
}

.close-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(255, 87, 34, 0.1);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: var(--primary-color);
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: rgba(255, 87, 34, 0.2);
  transform: scale(1.1);
}

.like-users-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 30px 20px;
  color: var(--medium-text);
}

.no-like-users {
  text-align: center;
  padding: 30px 20px;
  color: var(--medium-text);
}

.no-like-users p {
  margin: 0;
  font-size: 14px;
}

.like-users-list {
  padding: 16px 20px;
  max-height: 300px;
  overflow-y: auto;
}

.like-user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  transition: background-color 0.2s ease;
}

.like-user-item:last-child {
  border-bottom: none;
}

.like-user-item:hover {
  background-color: rgba(255, 87, 34, 0.02);
  border-radius: 8px;
  margin: 0 -8px;
  padding-left: 8px;
  padding-right: 8px;
}

.like-user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 87, 34, 0.15);
  flex-shrink: 0;
}

.like-user-info {
  flex: 1;
  min-width: 0;
}

.like-user-name {
  font-weight: 600;
  color: var(--dark-text);
  font-size: 14px;
  margin-bottom: 2px;
}

.like-user-level {
  font-size: 12px;
  color: var(--medium-text);
}

/* 반응형 */
@media (max-width: 480px) {
  .like-users-header {
    padding: 14px 16px;
  }
  
  .like-users-header h4 {
    font-size: 15px;
  }
  
  .like-users-list {
    padding: 12px 16px;
  }
  
  .like-user-item {
    padding: 10px 0;
  }
  
  .like-user-avatar {
    width: 32px;
    height: 32px;
  }
  
  .like-user-name {
    font-size: 13px;
  }
  
  .like-user-level {
    font-size: 11px;
  }
}

/* 좋아요 버튼과 텍스트 스타일 */
.post-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  border-top: 1px solid var(--border-color);
  padding-top: 16px;
  margin-top: 20px;
}

/* 하트 버튼 스타일 */
.heart-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #ffff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.heart-btn:hover {
  background: linear-gradient(135deg, rgba(255, 87, 34, 0.15) 0%, rgba(255, 87, 34, 0.1) 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 87, 34, 0.2);
}

.heart-btn.liked {
  background: #ffff;
  border-color: var(--primary-color);
}

.heart-btn.liked:hover {
  background: linear-gradient(135deg, rgba(255, 87, 34, 0.3) 0%, rgba(255, 87, 34, 0.2) 100%);
  box-shadow: 0 4px 16px rgba(255, 87, 34, 0.3);
}

.heart-btn.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.heart-btn.disabled:hover {
  transform: none;
  box-shadow: none;
}

/* 하트 이모지 스타일 */
.heart-emoji {
  font-size: 20px;
  transition: all 0.2s ease;
  display: inline-block;
}

.heart-emoji:hover {
  transform: scale(1.1);
}

.heart-emoji.liked {
  animation: heartBeat 0.3s ease-in-out;
}

@keyframes heartBeat {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

/* 좋아요 텍스트 컨테이너 */
.like-text-container {
  flex: 1;
}

/* 좋아요 상태 텍스트 */
.like-status-text {
  font-size: 14px;
  color: var(--medium-text);
  transition: all 0.2s ease;
  user-select: none;
  display: inline-block;
}

.like-status-text.clickable {
  cursor: pointer;
}

.like-status-text.clickable:hover {
  color: var(--primary-color);
  text-decoration: underline;
}

/* 모달 오버레이 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 좋아요 사용자 모달 */
.like-users-modal {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  max-width: 500px;
  width: 100%;
  max-height: 80vh;
  overflow: hidden;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 모달 헤더 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  background: linear-gradient(135deg, rgba(255, 87, 34, 0.05) 0%, transparent 100%);
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--dark-text);
}

.modal-close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 87, 34, 0.1);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: var(--primary-color);
  transition: all 0.2s ease;
}

.modal-close-btn:hover {
  background: rgba(255, 87, 34, 0.2);
  transform: scale(1.1);
}

/* 모달 컨텐츠 */
.modal-content {
  padding: 20px 24px;
  max-height: 60vh;
  overflow-y: auto;
}

.modal-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--medium-text);
}

.modal-loading .spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 87, 34, 0.1);
  border-left-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.modal-empty {
  text-align: center;
  padding: 40px 20px;
  color: var(--medium-text);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.modal-empty p {
  margin: 0;
  font-size: 14px;
}

/* 사용자 카드 그리드 */
.like-users-grid {
  display: grid;
  gap: 12px;
}

.like-user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: rgba(255, 87, 34, 0.02);
  border: 1px solid rgba(255, 87, 34, 0.1);
  transition: all 0.2s ease;
}

.like-user-card:hover {
  background: rgba(255, 87, 34, 0.05);
  border-color: rgba(255, 87, 34, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 87, 34, 0.1);
}

.user-card-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 87, 34, 0.15);
  flex-shrink: 0;
}

.user-card-info {
  flex: 1;
  min-width: 0;
}

.user-card-name {
  font-weight: 600;
  color: var(--dark-text);
  font-size: 14px;
  margin-bottom: 2px;
}

.user-card-level {
  font-size: 12px;
  color: var(--medium-text);
}

.user-card-heart {
  font-size: 16px;
  opacity: 0.6;
}

.spinning {
  animation: spin 1s linear infinite;
  display: inline-block;
  font-size: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 반응형 */
@media (max-width: 480px) {
  .post-actions {
    gap: 12px;
  }
  
  .heart-btn {
    width: 36px;
    height: 36px;
  }
  
  .heart-emoji {
    font-size: 18px;
  }
  
  .like-status-text {
    font-size: 13px;
  }
  
  .modal-overlay {
    padding: 16px;
  }
  
  .like-users-modal {
    border-radius: 16px;
  }
  
  .modal-header {
    padding: 16px 20px;
  }
  
  .modal-header h3 {
    font-size: 16px;
  }
  
  .modal-content {
    padding: 16px 20px;
  }
  
  .like-user-card {
    padding: 10px;
  }
  
  .user-card-avatar {
    width: 36px;
    height: 36px;
  }
  
  .user-card-name {
    font-size: 13px;
  }
  
  .user-card-level {
    font-size: 11px;
  }
}

@media (max-width: 360px) {
  .heart-btn {
    width: 32px;
    height: 32px;
  }
  
  .heart-emoji {
    font-size: 16px;
  }
  
  .like-status-text {
    font-size: 12px;
  }
}
</style>