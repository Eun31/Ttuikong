// views/Board.vue (게시판 페이지)
<template>
  <div class="board-container">
    <h2 class="page-title">게시판</h2>
    
    <!-- 게시글 목록 -->
    <post-card 
      v-for="post in posts" 
      :key="post.id" 
      :post="post"
      @click="viewPostDetail(post.id)"
    />

    <!-- 게시글이 없을 경우 -->
    <div class="empty-state" v-if="posts.length === 0">
      <p>게시글이 없습니다.</p>
    </div>
  </div>
</template>

<script>
import PostCard from '../components/PostCard.vue';

export default {
  name: 'Board',
  components: {
    PostCard
  },
  data() {
    return {
      posts: [
        {
          id: 1,
          user: {
            name: 'TTUIKONG',
            avatar: 'https://via.placeholder.com/72/e8f5e9/2e7d32?text=T',
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
          liked: true
        },
        {
          id: 2,
          user: {
            name: 'TTUIKONG',
            avatar: 'https://via.placeholder.com/72/e8f5e9/2e7d32?text=T',
            level: '커뮤니티',
            verified: true
          },
          title: '자전거 타기 좋은 코스 추천',
          description: '주말에 자전거 타기 좋은 코스를 찾고 있는데 추천해주실 분 있나요? 한강 이외에 서울 근교 코스로 추천 부탁드립니다.',
          location: '서울특별시',
          likes: 8,
          comments: 12,
          liked: false
        },
        {
          id: 3,
          user: {
            name: '맛집탐험가',
            avatar: 'https://via.placeholder.com/72/f5f5f5/555555?text=P',
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
          liked: true
        },
        {
          id: 4,
          user: {
            name: '달리는 토끼',
            avatar: 'https://via.placeholder.com/72/f5f5f5/555555?text=D',
            level: '커뮤니티',
            verified: true
          },
          title: '타이투를 하고싶은 좋은 웹사이트는?',
          description: '자바와 리액트로 타이투 게임을 만들고 싶은데 좋은 웹사이트를 공유해줄 수 있을까요? RPM이나 웹애니메이션도 함께 있으면 더 좋을 것 같습니다!',
          tags: ['개발', '웹사이트', '추천요청'],
          location: '강남구 삼성동',
          likes: 15,
          comments: 23,
          liked: false
        },
        {
          id: 5,
          user: {
            name: '밤의 소리',
            avatar: 'https://via.placeholder.com/72/f5f5f5/555555?text=R',
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
          liked: false
        }
      ]
    };
  },
  methods: {
    viewPostDetail(postId) {
      this.$router.push({ name: 'post-detail', params: { id: postId } });
    }
  }
};
</script>

<style scoped>
.board-container {
  padding-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 20px;
  color: #2e7d32;
}

.empty-state {
  background-color: white;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>