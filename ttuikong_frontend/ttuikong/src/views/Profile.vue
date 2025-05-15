// views/Profile.vue (프로필 페이지)
<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="profile-cover"></div>
      <div class="profile-avatar-container">
        <img :src="profile.avatar" :alt="profile.name" class="profile-avatar">
        <div class="profile-edit-btn" @click="$router.push('/profile/edit')">
          <span class="icon-edit"></span>
        </div>
      </div>
      <h1 class="profile-name">{{ profile.name }}</h1>
      <p class="profile-bio">{{ profile.bio }}</p>
      <div class="profile-stats">
        <div class="stat-item">
          <span class="stat-value">{{ profile.postCount }}</span>
          <span class="stat-label">게시글</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ profile.followerCount }}</span>
          <span class="stat-label">팔로워</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ profile.followingCount }}</span>
          <span class="stat-label">팔로잉</span>
        </div>
      </div>
    </div>

    <div class="profile-content">
      <div class="section-tabs">
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'posts' }"
          @click="activeTab = 'posts'"
        >
          내 게시글
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'likes' }"
          @click="activeTab = 'likes'"
        >
          좋아요한 글
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'comments' }"
          @click="activeTab = 'comments'"
        >
          내 댓글
        </button>
      </div>

      <!-- 내 게시글 탭 -->
      <div v-if="activeTab === 'posts'" class="tab-content">
        <post-card 
          v-for="post in myPosts" 
          :key="post.id" 
          :post="post"
          @click="viewPostDetail(post.id)"
        />
        <div class="empty-state" v-if="myPosts.length === 0">
          <p>작성한 게시글이 없습니다.</p>
        </div>
      </div>

      <!-- 좋아요한 글 탭 -->
      <div v-if="activeTab === 'likes'" class="tab-content">
        <post-card 
          v-for="post in likedPosts" 
          :key="post.id" 
          :post="post"
          @click="viewPostDetail(post.id)"
        />
        <div class="empty-state" v-if="likedPosts.length === 0">
          <p>좋아요한 게시글이 없습니다.</p>
        </div>
      </div>

      <!-- 내 댓글 탭 -->
      <div v-if="activeTab === 'comments'" class="tab-content">
        <div 
          v-for="comment in myComments" 
          :key="comment.id" 
          class="comment-item"
          @click="viewPostDetail(comment.postId)"
        >
          <div class="comment-header">
            <span class="comment-post-title">{{ comment.postTitle }}</span>
            <span class="comment-date">{{ comment.date }}</span>
          </div>
          <p class="comment-text">{{ comment.text }}</p>
        </div>
        <div class="empty-state" v-if="myComments.length === 0">
          <p>작성한 댓글이 없습니다.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import PostCard from '../components/PostCard.vue';

export default {
  name: 'Profile',
  components: {
    PostCard
  },
  data() {
    return {
      activeTab: 'posts',
      profile: {
        name: 'TTUIKONG',
        avatar: 'https://via.placeholder.com/100/e8f5e9/2e7d32?text=T',
        bio: '안녕하세요! 자연과 여행을 좋아하는 TTUIKONG입니다. 일상의 작은 행복을 찾아 다니고 있어요.',
        postCount: 8,
        followerCount: 42,
        followingCount: 36
      },
      myPosts: [
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
        }
      ],
      likedPosts: [
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
        }
      ],
      myComments: [
        {
          id: 1,
          postId: 3,
          postTitle: '맛집 공유합니다!',
          text: '저도 이 맛집 가봤는데 진짜 맛있었어요! 특히 소스가 일품이었습니다.',
          date: '2일 전'
        },
        {
          id: 2,
          postId: 4,
          postTitle: '타이투를 하고싶은 좋은 웹사이트는?',
          text: 'CodePen이나 JSFiddle 같은 사이트가 연습하기 좋을 것 같아요. 예제도 많고 바로 결과를 볼 수 있어서 편리합니다.',
          date: '5일 전'
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
.profile-container {
  padding-bottom: 20px;
}

.profile-header {
  position: relative;
  background-color: white;
  border-radius: 20px;
  padding-bottom: 16px;
  margin-bottom: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.profile-cover {
  height: 120px;
  background: linear-gradient(135deg, #81c784 0%, #4caf50 100%);
}

.profile-avatar-container {
  position: relative;
  margin-top: -50px;
  display: inline-block;
}

.profile-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 4px solid white;
  object-fit: cover;
  background-color: white;
}

.profile-edit-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #4caf50;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
}

.profile-name {
  margin-top: 8px;
  font-size: a20px;
  font-weight: 700;
}

.profile-bio {
  padding: 0 20px;
  margin: 8px 0 16px;
  color: #666;
  font-size: 14px;
}

.profile-stats {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-weight: 700;
  font-size: 18px;
  color: #2e7d32;
}

.stat-label {
  font-size: 12px;
  color: #757575;
}

.section-tabs {
  display: flex;
  background-color: white;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.tab-btn {
  flex: 1;
  padding: 12px;
  text-align: center;
  background: none;
  border: none;
  cursor: pointer;
  font-weight: 500;
  color: #666;
  transition: all 0.2s ease;
}

.tab-btn.active {
  background-color: #e8f5e9;
  color: #2e7d32;
  font-weight: 600;
}

.comment-item {
  background-color: white;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.comment-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-post-title {
  font-weight: 600;
  font-size: 14px;
}

.comment-date {
  font-size: 12px;
  color: #757575;
}

.comment-text {
  font-size: 14px;
  color: #555;
}

.empty-state {
  background-color: white;
  border-radius: 12px;
  padding: 30px 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>