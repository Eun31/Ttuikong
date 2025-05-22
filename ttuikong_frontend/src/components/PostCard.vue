// components/PostCard.vue
<template>
  <div class="post-card" @click="handleClick">
    <!-- 작성자 정보 -->
    <div class="user-profile">
      <img :src="post.user.avatar" alt="프로필" class="user-avatar">
      <div class="user-details">
        <div class="user-name">
          {{ post.user.name }}
          <span class="badge" v-if="post.user.verified">
            <i class="ri-check-line"></i>
          </span>
        </div>
        <div class="user-level">{{ post.user.level }}</div>
      </div>
    </div>

    <!-- 게시글 내용 -->
    <div class="post-content">
      <h2 class="post-title">{{ post.title }}</h2>
      <p class="post-desc">{{ post.description }}</p>

      <!-- 게시글 이미지 (있는 경우만) -->
      <div class="post-image" v-if="post.image">
        <img :src="post.image" :alt="post.title">
      </div>

      <!-- 미디어 컨텐츠 (있는 경우만) -->
      <div class="media-content" v-if="post.mediaContent">
        <div class="media-container">
          <div class="media-icon">▶</div>
          <div class="media-info">
            <div class="media-title">{{ post.mediaContent.title }}</div>
            <div class="media-subtitle">{{ post.mediaContent.artist }} • {{ post.mediaContent.album }}</div>
          </div>
        </div>
      </div>

      <!-- 태그 (있는 경우만) -->
      <div class="post-tags" v-if="post.tags && post.tags.length">
        <span class="tag" v-for="(tag, index) in post.tags" :key="index">{{ tag }}</span>
      </div>

      <div class="post-footer">
        <div class="post-location" v-if="post.location">
          <i class="ri-map-pin-line location-icon"></i>
          <span>{{ post.location }}</span>
        </div>
        <div class="post-stats">
            <span class="heart-icon" :class="{ 'liked': isLiked, 'loading': likeLoading }">
              <span>{{ isLiked ? '❤️' : '♡' }}</span>
            </span>
            <span>{{ likeCount }}</span>
          <!-- 댓글 수 -->
          <div class="stat">
            <span class="comment-icon">💬</span>
            <span>{{ comments.length }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, toRefs } from 'vue';
import axios from 'axios';

const props = defineProps({
  post: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['click']);

const { post } = toRefs(props);

// 댓글 관련 상태
const comments = ref([]);

// 좋아요 관련 상태
const isLiked = ref(false);
const likeCount = ref(0);
const likeLoading = ref(false);

const API_URL = 'http://localhost:8080/api';
const token = localStorage.getItem('jwt');

// 헤더에 토큰 설정
const authHeader = () => {
  return token ? { 'Authorization': `Bearer ${token}` } : {};
};

// 댓글 목록 가져오기
const fetchComments = async () => {
  try {
    const response = await axios.get(`${API_URL}/board/${post.value.id}/comment`);
    comments.value = response.data;
  } catch (err) {
    console.error('댓글 조회 실패:', err);
    comments.value = [];
  }
};

// 좋아요 개수 가져오기
const fetchLikeCount = async () => {
  try {
    const response = await axios.get(`${API_URL}/board/${post.value.id}/like/count`);
    likeCount.value = response.data || 0;
    console.log(`게시글 ${post.value.id} 좋아요 개수:`, likeCount.value);
  } catch (err) {
    console.error('좋아요 개수 조회 실패:', err);
    likeCount.value = 0;
  }
};

// 좋아요 상태 확인
const checkLikeStatus = async () => {
  if (!token) {
    isLiked.value = false;
    return;
  }
  
  try {
    const response = await axios.get(`${API_URL}/board/${post.value.id}/like/status`, {
      headers: authHeader()
    });
    isLiked.value = response.data;
    console.log(`게시글 ${post.value.id} 좋아요 상태:`, isLiked.value);
  } catch (err) {
    console.error('좋아요 상태 확인 실패:', err);
    isLiked.value = false;
  }
};

// 클릭 이벤트 핸들러
const handleClick = () => {
  emit('click', post.value.id);
};

onMounted(async () => {
  await Promise.all([
    fetchComments(),
    fetchLikeCount(),
    checkLikeStatus()
  ]);
});
</script>

<style scoped>
.post-card {
  background-color: var(--card-color, white);
  border-radius: var(--border-radius, 16px);
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: var(--shadow-md, 0 2px 8px rgba(0, 0, 0, 0.08));
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
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

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg, 0 4px 12px rgba(0, 0, 0, 0.1));
}

.user-profile {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color, #f0f0f0);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 12px;
  object-fit: cover;
}

.user-details {
  flex: 1;
}

.user-name {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: var(--dark-text, #333);
}

.badge {
  color: var(--primary-color, #FF5722);
  margin-left: 5px;
  font-size: 14px;
}

.user-level {
  font-size: 12px;
  color: var(--medium-text, #757575);
}

.post-content {
  padding: 16px;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  line-height: 1.4;
  color: var(--dark-text, #333);
}

.post-desc {
  font-size: 14px;
  color: var(--medium-text, #555);
  margin-bottom: 12px;
}

.post-image {
  margin-bottom: 12px;
  border-radius: var(--border-radius, 12px);
  overflow: hidden;
}

.post-image img {
  width: 100%;
  object-fit: cover;
  display: block;
}

/* 미디어 컨텐츠 스타일 */
.media-content {
  margin-bottom: 12px;
  padding: 10px;
  background-color: rgba(255, 87, 34, 0.05);
  border-radius: var(--border-radius, 12px);
}

.media-container {
  display: flex;
  align-items: center;
}

.media-icon {
  width: 40px;
  height: 40px;
  background-color: var(--primary-color, #FF5722);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
}

.media-info {
  margin-left: 12px;
}

.media-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--dark-text, #333);
}

.media-subtitle {
  font-size: 12px;
  color: var(--medium-text, #666);
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.tag {
  background-color: rgba(255, 87, 34, 0.1);
  color: var(--primary-color, #FF5722);
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid var(--border-color, #f0f0f0);
  font-size: 12px;
  color: var(--medium-text, #757575);
}

.post-location {
  display: flex;
  align-items: center;
}

.location-icon {
  margin-right: 4px;
  color: var(--primary-color, #FF5722);
}

.post-stats {
  display: flex;
  align-items: center;
  gap: 7px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-count {
  font-size: 12px;
  color: var(--medium-text, #757575);
  font-weight: 500;
  line-height: 1;
}

/* 좋아요 버튼 스타일 */
.like-stat {
  cursor: pointer;
  transition: transform 0.2s ease;
}

.like-stat:hover {
  transform: scale(1.05);
}

.like-stat:active {
  transform: scale(0.95);
}

.heart-icon {
  font-size: 16px;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
}

.heart-icon:hover {
  transform: scale(1.2);
}

.heart-icon.liked {
  animation: heartBeat 0.3s ease-in-out;
}

.heart-icon.loading {
  animation: spin 1s linear infinite;
}

@keyframes heartBeat {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 댓글 아이콘 스타일 */
.comment-icon {
  margin-left: 10px;
  font-size: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
}

@media (max-width: 600px) {
  .post-card {
    border-radius: var(--border-radius, 12px);
    margin-bottom: 12px;
  }

  .post-content {
    padding: 12px;
  }
}
</style>