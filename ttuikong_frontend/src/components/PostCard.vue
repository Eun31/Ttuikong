// components/PostCard.vue
<template>
  <div class="post-card" @click="$emit('click')">
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
            <!-- 아이콘 대체 (텍스트 이모지 사용) -->
            <span class="heart-icon" :class="{ 'liked': post.liked }">
              {{ post.liked ? '❤️' : '♡' }}
            </span>
            <span>{{ post.likes }}</span>
          </div>
          <div class="stat">
            <span class="comment-icon">💬</span>
            <span>{{ post.comments }}</span>
          </div>
        </div>
      </div>
  </div>
</template>

<script>
export default {
  name: 'PostCard',
  props: {
    post: {
      type: Object,
      required: true
    }
  },
  methods: {
    toggleLike() {
      // 실제 구현에서는 API 호출을 통해 좋아요 처리
      this.post.liked = !this.post.liked;
      this.post.likes += this.post.liked ? 1 : -1;
    }
  }
};
</script>

<style scoped>
/* 게시글 카드 */
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

/* 사용자 정보 */
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

/* 게시글 내용 */
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
  gap: 12px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 좋아요 아이콘 스타일 */
.like-stat {
  cursor: pointer;
}

.heart-icon {
  font-size: 14px;
  transition: transform 0.2s ease;
}

.heart-icon:hover {
  transform: scale(1.2);
}

.heart-icon.liked {
  color: #e91e63;
}

/* 댓글 아이콘 스타일 */
.comment-icon {
  font-size: 14px;
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