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
            <span class="icon-verified"></span>
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
      <div class="media-content" v-if="post.mediaContent" 
           style="margin-bottom: 12px; padding: 10px; background-color: #f5f5f5; border-radius: 12px;">
        <div style="display: flex; align-items: center;">
          <div style="width: 40px; height: 40px; background-color: #333; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: white; font-size: 18px;">▶</div>
          <div style="margin-left: 12px;">
            <div style="font-weight: 600; font-size: 14px;">{{ post.mediaContent.title }}</div>
            <div style="font-size: 12px; color: #666;">{{ post.mediaContent.artist }} • {{ post.mediaContent.album }}</div>
          </div>
        </div>
      </div>
      
      <!-- 태그 (있는 경우만) -->
      <div class="post-tags" v-if="post.tags && post.tags.length">
        <span class="tag" v-for="(tag, index) in post.tags" :key="index">{{ tag }}</span>
      </div>
      
      <div class="post-footer">
        <div class="post-location" v-if="post.location">
          <span class="post-location-icon icon-location"></span>
          <span>{{ post.location }}</span>
        </div>
        <div class="post-stats">
          <div class="stat">
            <span class="stat-icon" :class="post.liked ? 'icon-heart' : 'icon-heart-o'" @click.stop="toggleLike"></span>
            <span>{{ post.likes }}</span>
          </div>
          <div class="stat">
            <span class="stat-icon icon-comment"></span>
            <span>{{ post.comments }}</span>
          </div>
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
  background-color: white;
  border-radius: 20px;  /* 둥근 모서리 */
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
}

/* 사용자 정보 */
.user-profile {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
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
}

.badge {
  color: #4caf50;
  margin-left: 5px;
}

.user-level {
  font-size: 12px;
  color: #757575;
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
}

.post-desc {
  font-size: 14px;
  color: #555;
  margin-bottom: 12px;
}

.post-image {
  margin-bottom: 12px;
  border-radius: 12px;
  overflow: hidden;
}

.post-image img {
  width: 100%;
  object-fit: cover;
  display: block;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.tag {
  background-color: #e8f5e9;
  color: #2e7d32;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
  font-size: 12px;
  color: #757575;
}

.post-location {
  display: flex;
  align-items: center;
}

.post-location-icon {
  margin-right: 4px;
}

.post-stats {
  display: flex;
  gap: 12px;
}

.stat {
  display: flex;
  align-items: center;
}

.stat-icon {
  margin-right: 4px;
  cursor: pointer;
}

@media (min-width: 600px) {
  .post-card {
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  }

  .post-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}
</style>