<template>
  <div class="board-container">
    <div class="board-header">
      <h2 class="page-title">커뮤니티 게시판</h2>
      <div class="board-actions">
        <div class="search-box">
          <input type="text" placeholder="게시글 검색..." v-model="searchQuery" />
          <button class="search-btn"><i class="fas fa-search"></i></button>
        </div>
        <button class="write-btn" @click="goToWritePage">글쓰기</button>
      </div>
    </div>

    <div class="category-filter">
      <button class="category-btn" :class="{ active: currentCategory === 'all' }"
        @click="filterByCategory('all')">전체</button>
      <button class="category-btn" :class="{ active: currentCategory === '자유' }"
        @click="filterByCategory('자유')">자유</button>
      <button class="category-btn" :class="{ active: currentCategory === '루틴공유' }"
        @click="filterByCategory('루틴공유')">루틴공유</button>
      <button class="category-btn" :class="{ active: currentCategory === '운동후기' }"
        @click="filterByCategory('운동후기')">운동후기</button>
      <button class="category-btn" :class="{ active: currentCategory === '장소추천' }"
        @click="filterByCategory('장소추천')">장소추천</button>
      <button class="category-btn" :class="{ active: currentCategory === '노래추천' }"
        @click="filterByCategory('노래추천')">노래추천</button>
      <button class="category-btn" :class="{ active: currentCategory === '건강식품' }"
        @click="filterByCategory('건강식품')">건강식품</button>
    </div>
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>게시글을 불러오는 중...</p>
    </div>
    <div v-else-if="posts.length > 0" class="posts-grid">
      <post-card v-for="post in paginatedPosts" :key="post.id" :post="post" @click="viewPostDetail(post.id);" />
    </div>
    <div v-else class="empty-state">
      <p>게시글이 없습니다.</p>
      <p class="empty-subtitle">첫 번째 게시글을 작성해보세요!</p>
      <button class="write-btn-empty" @click="goToWritePage">글쓰기</button>
    </div>
    <div v-if="posts.length > 0" class="pagination">
      <button class="page-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">
        <i class="fas fa-chevron-left"></i>
      </button>
      <div class="page-numbers">
        <button v-for="page in totalPages" :key="page" :class="['page-number', { active: page === currentPage }]"
          @click="changePage(page)">
          {{ page }}
        </button>
      </div>
      <button class="page-btn" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">
        <i class="fas fa-chevron-right"></i>
      </button>
    </div>

    <div v-if="error" class="error-message">
      <p>{{ error }}</p>
      <button @click="fetchPosts" class="retry-btn">다시 시도</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import PostCard from '@/components/PostCard.vue';
import profileImg from '@/assets/profile.png';

const router = useRouter();
const searchQuery = ref('');
const currentCategory = ref('all');
const currentPage = ref(1);
const postsPerPage = 6;
const posts = ref([]);
const loading = ref(true);
const error = ref('');

function getFullImageUrl(imageUrl) {
  if (!imageUrl) return null;

  if (imageUrl.startsWith('http')) {
    return imageUrl;
  }

  return `http://localhost:8080${imageUrl}`;
}

function fetchPosts() {
  loading.value = true;
  error.value = '';

  axios.get('http://localhost:8080/api/board')
    .then(response => {
      if (Array.isArray(response.data)) {
        const transformedPosts = response.data.map(item => {
          const originalImageUrl = item.imageUrl;
          const fullImageUrl = getFullImageUrl(originalImageUrl);

          return {
            id: item.postId,
            category: item.category || '자유',
            user: {
              name: item.userNickname,
              avatar: profileImg,
              id: item.userId
            },
            title: item.
              title || '',
            description: item.content || '',
            image: fullImageUrl,
            likes: 0,
            comments: 0,
            liked: false,
            createdAt: item.createdAt
          };
        });

        posts.value = transformedPosts;
      } else {
        posts.value = [];
      }
    })
    .catch(error => {
      console.error('게시글을 불러오는 중 오류가 발생했습니다:', error);
      posts.value = [];
      error.value = '게시글을 불러오는 중 오류가 발생했습니다. 다시 시도해 주세요.';
    })
    .finally(() => {
      loading.value = false;
    });
}

const filteredPosts = computed(() => {
  let result = posts.value;
  if (currentCategory.value !== 'all') {
    result = result.filter(post => post.category === currentCategory.value);
  }
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase().trim();
    result = result.filter(post =>
      (post.title && post.title.toLowerCase().includes(query)) ||
      (post.description && post.description.toLowerCase().includes(query))
    );
  }

  result = [...result].sort((a, b) => {
    const dateA = a.createdAt instanceof Date ? a.createdAt : new Date(a.createdAt);
    const dateB = b.createdAt instanceof Date ? b.createdAt : new Date(b.createdAt);
    return dateB - dateA;
  });

  return result;
});

const paginatedPosts = computed(() => {
  const start = (currentPage.value - 1) * postsPerPage;
  const end = start + postsPerPage;
  return filteredPosts.value.slice(start, end);
});

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(filteredPosts.value.length / postsPerPage));
});

function viewPostDetail(postId) {
  if (postId === undefined || postId === null) {
    console.error('유효하지 않은 게시글 ID');
    return;
  }

  try {
    router.push({
      name: 'post-detail',
      params: { id: postId }
    });
  } catch (error) {
    console.error('라우터 이동 중 오류:', error);
  }
}

function filterByCategory(category) {
  currentCategory.value = category;
  currentPage.value = 1;
}

function changePage(page) {
  currentPage.value = page;
  window.scrollTo(0, 0);
}

function goToWritePage() {
  router.push({ name: 'post-write' });
}

onMounted(() => {
  fetchPosts();
});
</script>
<style>
@import '@/assets/css/board.css';

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  margin: 50px 0;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border-left-color: #3498db;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

.error-message {
  text-align: center;
  color: #e74c3c;
  background-color: #fef2f1;
  padding: 15px;
  border-radius: 8px;
  margin: 20px 0;
}

.retry-btn {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  margin-top: 10px;
  cursor: pointer;
}

.retry-btn:hover {
  background-color: #2980b9;
}
</style>