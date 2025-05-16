// views/Board.vue (게시판 페이지)
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
    
    <!-- 카테고리 필터 -->
    <div class="category-filter">
      <button class="category-btn active" @click="filterByCategory('all')">전체</button>
      <button class="category-btn" @click="filterByCategory('daily')">일상</button>
      <button class="category-btn" @click="filterByCategory('food')">맛집</button>
      <button class="category-btn" @click="filterByCategory('travel')">여행</button>
      <button class="category-btn" @click="filterByCategory('tech')">기술</button>
      <button class="category-btn" @click="filterByCategory('music')">음악</button>
    </div>
    
    <!-- 게시글 목록 -->
    <div class="posts-grid">
      <post-card 
        v-for="post in filteredPosts" 
        :key="post.id" 
        :post="post"
        @click="viewPostDetail(post.id)"
      />
    </div>

    <!-- 게시글이 없을 경우 -->
    <div class="empty-state" v-if="filteredPosts.length === 0">
      <img src="../assets/images/empty-box.svg" alt="게시글 없음" class="empty-icon">
      <p>게시글이 없습니다.</p>
      <p class="empty-subtitle">첫 번째 게시글을 작성해보세요!</p>
      <button class="write-btn-empty" @click="goToWritePage">글쓰기</button>
    </div>
    
    <!-- 페이지네이션 -->
    <div class="pagination">
      <button class="page-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">
        <i class="fas fa-chevron-left"></i>
      </button>
      <div class="page-numbers">
        <button 
          v-for="page in totalPages" 
          :key="page" 
          :class="['page-number', { active: page === currentPage }]"
          @click="changePage(page)"
        >
          {{ page }}
        </button>
      </div>
      <button class="page-btn" :disabled="currentPage === totalPages" @click="changePage(currentPage + 1)">
        <i class="fas fa-chevron-right"></i>
      </button>
    </div>
  </div>
</template>

<script>
import PostCard from '../components/PostCard.vue';
import profileImg from '../assets/profile.png';

export default {
  name: 'Board',
  components: {
    PostCard
  },
  data() {
    return {
      searchQuery: '',
      currentCategory: 'all',
      currentPage: 1,
      postsPerPage: 6,
      posts: [
        {
          id: 1,
          category: 'daily',
          user: {
            name: 'TTUIKONG',
            avatar: profileImg,
            level: '커뮤니티',
            verified: true
          },
          title: '오늘 한강에서 찍은 풍경',
          description: '오늘 한강에서 산책하다가 너무 예쁜 풍경을 봐서 사진을 찍고, 한강가 벤치에서 책도 읽고, 행복한 하루였습니다. 다들 좋은 저녁 되세요!',
          image: 'https://via.placeholder.com/600x300/e8f5e9/2e7d32?text=한강',
          tags: ['자연', '여행', '일상'],
          location: '한강공원 여의도',
          likes: 24,
          comments: 3,
          liked: true,
          createdAt: new Date('2025-05-15T14:32:00')
        },
        {
          id: 2,
          category: 'travel',
          user: {
            name: 'TTUIKONG',
            avatar: profileImg,
            level: '커뮤니티',
            verified: true
          },
          title: '자전거 타기 좋은 코스 추천',
          description: '주말에 자전거 타기 좋은 코스를 찾고 있는데 추천해주실 분 있나요? 한강 이외에 서울 근교 코스로 추천 부탁드립니다.',
          location: '서울특별시',
          likes: 8,
          comments: 12,
          liked: false,
          createdAt: new Date('2025-05-14T09:15:00')
        },
        {
          id: 3,
          category: 'food',
          user: {
            name: '맛집탐험가',
            avatar: profileImg,
            level: '커뮤니티',
            verified: false
          },
          title: '맛집 공유합니다!',
          description: '어제 발견한 숨은 맛집인데 정말 맛있어서 공유합니다. 여기 돈까스가 진짜 엄청 두껍고 바삭바삭해요. 가격도 합리적이고 사장님도 친절하셔서 추천해요!',
          image: 'https://via.placeholder.com/600x300/f5f6ce/555555?text=맛집',
          tags: ['맛집', '돈까스', '추천'],
          location: '강남구 역삼동',
          likes: 42,
          comments: 7,
          liked: true,
          createdAt: new Date('2025-05-13T18:45:00')
        },
        {
          id: 4,
          category: 'tech',
          user: {
            name: '달리는 토끼',
            avatar: profileImg,
            level: '커뮤니티',
            verified: true
          },
          title: '타이투를 하고싶은 좋은 웹사이트는?',
          description: '자바와 리액트로 타이투 게임을 만들고 싶은데 좋은 웹사이트를 공유해줄 수 있을까요? RPM이나 웹애니메이션도 함께 있으면 더 좋을 것 같습니다!',
          tags: ['개발', '웹사이트', '추천요청'],
          location: '강남구 삼성동',
          likes: 15,
          comments: 23,
          liked: false,
          createdAt: new Date('2025-05-12T10:20:00')
        },
        {
          id: 5,
          category: 'music',
          user: {
            name: '밤의 소리',
            avatar: profileImg,
            level: '커뮤니티',
            verified: false
          },
          title: '밤에 들으면 좋은 음악 추천',
          description: '차분한 밤에 들으면 좋은 음악을 공유합니다. 잠들기 전 감성에 젖고 싶을 때 추천해요.',
          mediaContent: {
            type: 'music',
            title: 'Everything Will Be Alright',
            artist: 'Robokid',
            album: 'The Nightrunners'
          },
          tags: ['음악', '추천', '밤'],
          location: 'Sound Cloud',
          likes: 12,
          comments: 3,
          liked: false,
          createdAt: new Date('2025-05-11T22:08:00')
        }
      ]
    };
  },
  computed: {
    filteredPosts() {
      let result = this.posts;
      
      // 카테고리 필터링
      if (this.currentCategory !== 'all') {
        result = result.filter(post => post.category === this.currentCategory);
      }
      
      // 검색 필터링
      if (this.searchQuery.trim()) {
        const query = this.searchQuery.toLowerCase().trim();
        result = result.filter(post => 
          post.title.toLowerCase().includes(query) || 
          post.description.toLowerCase().includes(query) ||
          (post.tags && post.tags.some(tag => tag.toLowerCase().includes(query)))
        );
      }
      
      // 정렬 (최신순)
      result = [...result].sort((a, b) => b.createdAt - a.createdAt);
      
      return result;
    },
    paginatedPosts() {
      const start = (this.currentPage - 1) * this.postsPerPage;
      const end = start + this.postsPerPage;
      return this.filteredPosts.slice(start, end);
    },
    totalPages() {
      return Math.max(1, Math.ceil(this.filteredPosts.length / this.postsPerPage));
    }
  },
  methods: {
    viewPostDetail(postId) {
      this.$router.push({ name: 'post-detail', params: { id: postId } });
    },
    filterByCategory(category) {
      this.currentCategory = category;
      this.currentPage = 1;
    },
    changePage(page) {
      this.currentPage = page;
      // 페이지 변경 시 상단으로 스크롤
      window.scrollTo(0, 0);
    },
    goToWritePage() {
      this.$router.push({ name: 'post-write' });
    }
  },
  mounted() {
    // FontAwesome이나 다른 아이콘 라이브러리 로드 (기존에 로드되어 있지 않다면)
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = 'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css';
    document.head.appendChild(link);
  }
};
</script>

<style>
@import '../assets/css/board.css';
</style>