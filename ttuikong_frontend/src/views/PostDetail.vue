<template>
  <div class="container">
    <!-- 헤더 -->
    <header class="header">
      <button class="back-btn" @click="goBack">
        <i class="icon-arrow-left"></i>
      </button>
      <h1 class="header-title">게시글</h1>
      <button class="menu-btn" @click="toggleOptions">
        <i class="ri-more-line"></i>
      </button>
    </header>

    <!-- 게시글 카드 -->
    <div class="post-card">
      <!-- 작성자 정보 -->
      <div class="user-profile">
        <img :src="post.author.avatar" alt="프로필" class="user-avatar">
        <div class="user-details">
          <div class="user-name">
            {{ post.author.name }}
            <span v-if="post.author.verified" class="badge">
              <i class="icon-check"></i>
            </span>
          </div>
          <div class="post-time">{{ post.createdAt }}</div>
        </div>
        <div class="post-options">
          <button class="options-btn" @click="toggleOptions">
            <i class="ri-more-line"></i>
          </button>
          <div class="options-menu" :class="{ show: showOptions }">
            <div class="option-item" @click="editPost">수정하기</div>
            <div class="option-item" @click="deletePost">삭제하기</div>
            <div class="option-item" @click="reportPost">신고하기</div>
          </div>
        </div>
      </div>

      <!-- 게시글 내용 -->
      <div class="post-content">
        <h2 class="post-title">{{ post.title }}</h2>
        <div class="post-body">{{ post.content }}</div>
        
        <div v-if="post.image" class="post-image">
          <img :src="post.image" :alt="post.title">
        </div>
        
        <div v-if="post.location" class="post-location">
          <span class="location-icon">📍</span>
          <span>{{ post.location }}</span>
        </div>

        <div v-if="post.tags && post.tags.length > 0" class="post-tags">
          <span v-for="(tag, index) in post.tags" :key="index" class="tag">{{ tag }}</span>
        </div>

        <div class="post-actions">
          <button class="action-btn like-btn" :class="{ active: post.isLiked }" @click="toggleLike">
            <span class="action-icon">{{ post.isLiked ? '❤️' : '♡' }}</span>
            <span class="action-count">{{ post.likes }}</span>
          </button>
          <button class="action-btn">
            <span class="action-icon">💬</span>
            <span class="action-count">{{ post.comments.length }}</span>
          </button>
          <button class="action-btn" @click="sharePost">
            <span class="action-icon">↗️</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 댓글 카드 -->
    <div class="comments-card">
      <div class="comments-header">댓글 {{ post.comments.length }}개</div>
      
      <!-- 댓글 목록 -->
      <div v-for="(comment, index) in post.comments" :key="index" class="comment">
        <div class="comment-author">
          <img :src="comment.author.avatar" :alt="comment.author.name" class="comment-avatar">
          <div class="author-info">
            <div class="comment-name">
              {{ comment.author.name }}
              <span v-if="comment.author.verified" class="comment-badge">
                <span class="icon">✓</span>
              </span>
            </div>
            <div class="comment-time">{{ comment.createdAt }}</div>
          </div>
        </div>
        <div class="comment-body">{{ comment.text }}</div>
        <div class="comment-actions">
          <button class="comment-action comment-like" :class="{ active: comment.isLiked }" @click="likeComment(index)">
            <span class="icon">{{ comment.isLiked ? '❤️' : '♡' }}</span>
            <span>{{ comment.likes }}</span>
          </button>
          <button class="comment-action" @click="replyToComment(comment.author.name)">
            <span class="icon">↩️</span>
            <span>답글</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 댓글 입력 영역 -->
    <div class="comment-input-container">
      <img :src="currentUser.avatar" alt="내 프로필" class="my-avatar">
      <div class="comment-input-wrapper">
        <input 
          type="text" 
          v-model="commentInput"
          class="comment-input" 
          placeholder="댓글을 남겨보세요..."
          @keypress.enter="addComment"
        >
      </div>
      <button class="send-btn" :disabled="!commentInput.trim()" @click="addComment">
        <i class="ri-send-plane-fill"></i>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import profileImg from '../assets/profile.png';

// 라우터 가져오기
const router = useRouter();

// 반응형 데이터 정의
const showOptions = ref(false);
const commentInput = ref('');

// 현재 사용자 정보
const currentUser = ref({
  id: 'me123',
  name: '나',
  avatar: profileImg
});

// 게시글 데이터
const post = ref({
  id: '1',
  title: '오늘 한강에서 찍은 풍경',
  content: '오늘 한강에서 산책하다가 너무 예쁜 풍경을 봐서 사진을 찍고, 한강가 벤치에서 책도 읽고, 행복한 하루였습니다. 다들 좋은 저녁 되세요!',
  image: 'https://via.placeholder.com/600x300/FFCCBC/FF5722?text=한강',
  location: '한강공원 여의도',
  tags: ['자연', '여행', '일상', '한강', '산책'],
  likes: 24,
  isLiked: false,
  createdAt: '오늘 오후 3:24',
  author: {
    id: 'user1',
    name: 'TTUIKONG',
    avatar: profileImg,
    verified: true
  },
  comments: [
    {
      id: 'c1',
      text: '정말 좋은 곳이네요! 다음에 저도 가보고 싶어요.',
      likes: 2,
      isLiked: false,
      createdAt: '30분 전',
      author: {
        id: 'user2',
        name: '라면이',
        avatar: profileImg,
        verified: false
      }
    },
    {
      id: 'c2',
      text: '사진 너무 예쁘게 찍으셨네요~ 무슨 카메라 쓰시나요?',
      likes: 1,
      isLiked: true,
      createdAt: '2시간 전',
      author: {
        id: 'user3',
        name: '달리는 토끼',
        avatar: profileImg,
        verified: true
      }
    },
    {
      id: 'c3',
      text: '저도 오늘 한강 갔었는데 정말 날씨 좋았어요!',
      likes: 0,
      isLiked: false,
      createdAt: '3시간 전',
      author: {
        id: 'user4',
        name: '테디',
        avatar: profileImg,
        verified: false
      }
    }
  ]
});

// 메소드 정의
function toggleOptions() {
  showOptions.value = !showOptions.value;
}

function goBack() {
  router.push('/board');
}

function toggleLike() {
  post.value.isLiked = !post.value.isLiked;
  post.value.likes += post.value.isLiked ? 1 : -1;
}

function likeComment(index) {
  const comment = post.value.comments[index];
  comment.isLiked = !comment.isLiked;
  comment.likes += comment.isLiked ? 1 : -1;
}

function replyToComment(username) {
  commentInput.value = `@${username} `;
  // 댓글 입력 필드에 포커스
  nextTick(() => {
    document.querySelector('.comment-input').focus();
  });
}

function addComment() {
  const text = commentInput.value.trim();
  if (!text) return;
  
  // 새 댓글 객체 생성
  const newComment = {
    id: `c${post.value.comments.length + 1}`,
    text: text,
    likes: 0,
    isLiked: false,
    createdAt: '방금 전',
    author: currentUser.value
  };
  
  // 댓글 목록에 추가
  post.value.comments.unshift(newComment);
  
  // 입력 필드 초기화
  commentInput.value = '';
}

function editPost() {
  // 수정 페이지로 이동
  alert('게시글 수정 페이지로 이동합니다.');
  toggleOptions();
}

function deletePost() {
  if (confirm('정말 이 게시글을 삭제하시겠습니까?')) {
    // 실제 구현에서는 서버에 삭제 요청을 보냄
    alert('게시글이 삭제되었습니다.');
    goBack();
  }
  toggleOptions();
}

function reportPost() {
  alert('게시글이 신고되었습니다.');
  toggleOptions();
}

function sharePost() {
  alert('공유 기능은 준비 중입니다.');
}
</script>

<style scoped>
/* 컨테이너 */
.container {
  max-width: 100%;
  margin: 0 auto;
  background-color: var(--background-color);
  min-height: 100vh;
  padding: 16px;
  padding-bottom: 70px; /* 댓글 입력 영역을 위한 여백 */
}

@media (min-width: 768px) {
  .container {
    max-width: 700px;
    margin: 0 auto;
  }
}

/* 헤더 스타일 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.back-btn, .menu-btn {
  font-size: 24px;
  color: var(--primary-color);
  background: none;
  border: none;
  padding: 8px;
  cursor: pointer;
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--dark-text);
}

/* 게시글 카드 */
.post-card {
  background-color: var(--card-color);
  border-radius: var(--border-radius);
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: var(--shadow-md);
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

/* 작성자 정보 */
.user-profile {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  position: relative;
}

.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  margin-right: 12px;
  object-fit: cover;
}

.user-details {
  flex: 1;
}

.user-name {
  font-weight: 600;
  display: flex;
  align-items: center;
  color: var(--dark-text);
}

.badge {
  color: var(--primary-color);
  margin-left: 4px;
  font-size: 14px;
}

.post-time {
  font-size: 12px;
  color: var(--medium-text);
}

.post-options {
  position: relative;
}

.options-btn {
  font-size: 20px;
  padding: 4px 8px;
  color: var(--medium-text);
  background: none;
  border: none;
  cursor: pointer;
}

.options-menu {
  position: absolute;
  top: 100%;
  right: 0;
  width: 140px;
  background-color: var(--card-color);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-md);
  display: none;
  z-index: 50;
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.options-menu.show {
  display: block;
}

.option-item {
  padding: 12px 16px;
  font-size: 14px;
  color: var(--dark-text);
  cursor: pointer;
  transition: var(--transition);
}

.option-item:hover {
  background-color: rgba(255, 87, 34, 0.05);
}

/* 게시글 내용 */
.post-content {
  padding: 16px;
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
  line-height: 1.3;
  color: var(--dark-text);
}

.post-body {
  font-size: 15px;
  margin-bottom: 16px;
  line-height: 1.6;
  color: var(--dark-text);
}

.post-image {
  margin-bottom: 16px;
  border-radius: var(--border-radius);
  overflow: hidden;
}

.post-image img {
  width: 100%;
  object-fit: cover;
  display: block;
}

.post-location {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: var(--medium-text);
  margin-bottom: 12px;
}

.location-icon {
  margin-right: 8px;
  color: var(--primary-color);
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tag {
  background-color: rgba(255, 87, 34, 0.1);
  color: var(--primary-color);
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.post-actions {
  display: flex;
  border-top: 1px solid var(--border-color);
  padding-top: 12px;
}

.action-btn {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  transition: var(--transition);
  border-radius: var(--input-radius);
  background: none;
  border: none;
  cursor: pointer;
  color: var(--medium-text);
}

.action-btn:hover {
  background-color: rgba(255, 87, 34, 0.05);
}

.action-icon {
  font-size: 18px;
}

.action-count {
  font-size: 14px;
}

.like-btn.active .action-icon,
.like-btn.active .action-count {
  color: #e91e63;
}

/* 댓글 섹션 */
.comments-card {
  background-color: var(--card-color);
  border-radius: var(--border-radius);
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: var(--shadow-md);
  animation: fadeIn 0.4s ease-out;
}

.comments-header {
  font-size: 16px;
  font-weight: 600;
  padding: 16px;
  color: var(--primary-color);
  border-bottom: 1px solid var(--border-color);
}

.comment {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
}

.comment:last-child {
  border-bottom: none;
}

.comment-author {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 10px;
  object-fit: cover;
}

.author-info {
  flex: 1;
}

.comment-name {
  font-weight: 600;
  font-size: 14px;
  display: flex;
  align-items: center;
  color: var(--dark-text);
}

.comment-badge {
  color: var(--primary-color);
  margin-left: 4px;
  font-size: 12px;
}

.comment-time {
  font-size: 11px;
  color: var(--medium-text);
}

.comment-body {
  margin-left: 46px;
  font-size: 14px;
  margin-bottom: 8px;
  color: var(--dark-text);
}

.comment-actions {
  margin-left: 46px;
  display: flex;
  gap: 16px;
}

.comment-action {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--medium-text);
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px 0;
}

.comment-like.active {
  color: #e91e63;
}

/* 댓글 입력 영역 */
.comment-input-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: var(--card-color);
  padding: 12px 16px;
  border-top: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  z-index: 90;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

@media (min-width: 768px) {
  .comment-input-container {
    max-width: 700px;
    left: 50%;
    transform: translateX(-50%);
  }
}

.my-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 12px;
  object-fit: cover;
}

.comment-input-wrapper {
  flex: 1;
  position: relative;
}

.comment-input {
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  padding: 10px 16px;
  font-size: 14px;
  font-family: inherit;
  background-color: #f9f9f9;
  transition: var(--transition);
}

.comment-input:focus {
  outline: none;
  border-color: var(--primary-color);
  background-color: white;
  box-shadow: 0 0 0 3px rgba(255, 87, 34, 0.1);
}

.send-btn {
  margin-left: 12px;
  color: var(--primary-color);
  font-size: 22px;
  background: none;
  border: none;
  padding: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-btn:disabled {
  color: var(--light-text);
  cursor: default;
}
</style>