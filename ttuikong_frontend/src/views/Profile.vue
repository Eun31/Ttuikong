<template>
    <div class="profile-container">
      <!-- 상단 유저 정보 카드 -->
      <div class="user-profile-card">
        <div class="profile-header">
          <div class="profile-avatar">
            <img :src="defaultAvatar" alt="프로필 이미지" class="avatar-img">
            <div class="level-badge">Lv.{{ calculateLevel() }}</div>
          </div>
          <div class="profile-basic-info">
            <h2 class="user-nickname">{{ user.nickname }}</h2>
            <p class="user-desc">{{ getActivityLevel() }}</p>
            <div class="edit-profile-btn" @click="editProfile">프로필 수정</div>
          </div>
        </div>
        
        <!-- 팔로우 정보 -->
        <div class="follow-stats">
          <div class="follow-stat-item">
            <span class="follow-count">{{ stats.totalRuns }}</span>
            <span class="follow-label">러닝</span>
          </div>
          <div class="follow-stat-item">
            <span class="follow-count">{{ followers.length }}</span>
            <span class="follow-label">팔로워</span>
          </div>
          <div class="follow-stat-item">
            <span class="follow-count">{{ following.length }}</span>
            <span class="follow-label">팔로잉</span>
          </div>
        </div>
        
        <!-- 상세 정보 -->
        <div class="detailed-info">
          <div class="info-row">
            <span class="info-label">나이</span>
            <span class="info-value">{{ user.age }}세</span>
          </div>
          <div class="info-row">
            <span class="info-label">성별</span>
            <span class="info-value">{{ user.gender }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">키 / 몸무게</span>
            <span class="info-value">{{ user.height }}cm / {{ user.weight }}kg</span>
          </div>
          <div class="info-row">
            <span class="info-label">목표 활동성</span>
            <span class="info-value-goal">{{ getActivityGoal() }}</span>
          </div>
          <div class="info-row total-distance">
            <span class="info-label">총 달린 거리</span>
            <span class="info-value-highlight">{{ formatDistance(user.total_distance) }}</span>
          </div>
          <div class="info-row avg-distance">
            <span class="info-label">평균 달린 거리</span>
            <span class="info-value">{{ formatDistance(user.avg_distance) }}</span>
          </div>
        </div>
      </div>
  
      <!-- 탭 메뉴 -->
      <div class="tab-container">
        <div 
          v-for="(tab, index) in tabs" 
          :key="index" 
          :class="['tab-item', { active: activeTab === index }]"
          @click="activeTab = index"
        >
          {{ tab }}
        </div>
      </div>
  
      <!-- 탭 콘텐츠 영역 -->
      <div class="tab-content">
        <!-- 내 게시글 탭 -->
        <div v-if="activeTab === 0" class="my-posts tab-panel">
          <div v-if="myPosts.length === 0" class="empty-state">
            <p>아직 작성한 게시글이 없어요 😊</p>
            <button class="action-btn" @click="goToNewPost">첫 게시글 작성하기</button>
          </div>
          <post-card 
            v-for="post in myPosts" 
            :key="post.id" 
            :post="post"
            @like="toggleLike"
            @comment="goToComments"
          />
        </div>
  
        <!-- 좋아요한 글 탭 -->
        <div v-else-if="activeTab === 1" class="liked-posts tab-panel">
          <div v-if="likedPosts.length === 0" class="empty-state">
            <p>아직 좋아요한 게시글이 없어요 💖</p>
            <button class="action-btn" @click="goToBoard">게시판 둘러보기</button>
          </div>
          <post-card 
            v-for="post in likedPosts" 
            :key="post.id" 
            :post="post"
            @like="toggleLike"
            @comment="goToComments"
          />
        </div>
  
        <!-- 팔로워 탭 -->
        <div v-else-if="activeTab === 2" class="followers-panel tab-panel">
          <div v-if="followers.length === 0" class="empty-state">
            <p>아직 팔로워가 없어요 👀</p>
            <button class="action-btn" @click="goToDiscover">다른 러너 찾아보기</button>
          </div>
          <div v-else class="user-list">
            <div v-for="follower in followers" :key="follower.id" class="user-item">
              <img :src="defaultAvatar" alt="프로필" class="user-item-avatar">
              <div class="user-item-info">
                <span class="user-item-name">{{ follower.nickname }}</span>
                <span class="user-item-desc">{{ follower.activityLevel }}</span>
              </div>
              <button 
                :class="['follow-btn', { 'following': isFollowing(follower.id) }]"
                @click="toggleFollow(follower.id)"
              >
                {{ isFollowing(follower.id) ? '팔로잉' : '팔로우' }}
              </button>
            </div>
          </div>
        </div>
  
        <!-- 팔로잉 탭 -->
        <div v-else-if="activeTab === 3" class="following-panel tab-panel">
          <div v-if="following.length === 0" class="empty-state">
            <p>아직 팔로우 중인 러너가 없어요 🏃‍♀️</p>
            <button class="action-btn" @click="goToDiscover">러너 찾아보기</button>
          </div>
          <div v-else class="user-list">
            <div v-for="follow in following" :key="follow.id" class="user-item">
              <img :src="defaultAvatar" alt="프로필" class="user-item-avatar">
              <div class="user-item-info">
                <span class="user-item-name">{{ follow.nickname }}</span>
                <span class="user-item-desc">{{ follow.activityLevel }}</span>
              </div>
              <button class="follow-btn following" @click="toggleFollow(follow.id)">
                팔로잉
              </button>
            </div>
          </div>
        </div>
  
        <!-- 피드 탭 -->
        <div v-else-if="activeTab === 4" class="feed-panel tab-panel">
          <div v-if="feedPosts.length === 0" class="empty-state">
            <p>팔로우 중인 러너들의 게시글이 없어요 📝</p>
            <button class="action-btn" @click="goToDiscover">러너 찾아보기</button>
          </div>
          <post-card 
            v-for="post in feedPosts" 
            :key="post.id" 
            :post="post"
            @like="toggleLike"
            @comment="goToComments"
          />
        </div>
      </div>
    </div>
  </template>
  
  <script>
  // 프로필 이미지 import
  import profileImg from '../assets/profile.png'
  
  export default {
    name: 'ProfilePage',
    components: {
      // 가정: PostCard 컴포넌트가 별도로 구현되어 있음
      PostCard: {
        props: ['post'],
        template: `
          <div class="post-card">
            <div class="post-header">
              <img :src="post.authorAvatar" class="post-avatar">
              <span class="post-author">{{ post.author }}</span>
              <span class="post-time">{{ post.time }}</span>
            </div>
            <p class="post-content">{{ post.content }}</p>
            <div class="post-footer">
              <div class="post-action" @click="$emit('like', post.id)">
                <span :class="['action-icon', { active: post.liked }]">❤️</span>
                <span class="action-count">{{ post.likes }}</span>
              </div>
              <div class="post-action" @click="$emit('comment', post.id)">
                <span class="action-icon">💬</span>
                <span class="action-count">{{ post.comments }}</span>
              </div>
            </div>
          </div>
        `
      }
    },
    data() {
      return {
        // 이미지 경로
        defaultAvatar: profileImg,
        
        activeTab: 0,
        tabs: ['내 게시글', '좋아요한 글', '팔로워', '팔로잉', '피드'],
        // 사용자 데이터 - 실제로는 API에서 가져옴
        user: {
          id: 1,
          email: 'user@example.com',
          nickname: '러닝마니아',
          gender: '남성',
          age: 28,
          height: 175.0,
          weight: 68.5,
          activity_level: '신나는 강아지',
          activity_goal: '힘찬 질주 말',
          avg_distance: 5.2,
          total_distance: 324.8,
          role: 'USER'
        },
        stats: {
          totalRuns: 42
        },
        // 샘플 데이터 - 실제로는 API에서 가져옴
        myPosts: [
          {
            id: 1,
            author: '러닝마니아',
            authorAvatar: profileImg, // 이미지 경로 변경
            content: '오늘도 힘차게 5km 뛰었습니다! 날씨가 좋아서 기분도 좋고 컨디션도 최고였어요 😊',
            time: '3시간 전',
            likes: 24,
            comments: 5,
            liked: false
          },
          {
            id: 2,
            author: '러닝마니아',
            authorAvatar: profileImg, // 이미지 경로 변경
            content: '혼자 뛰기 심심해서 러닝 메이트 구합니다. 한강 잠실 쪽에서 주 3회 뛰어요!',
            time: '어제',
            likes: 15,
            comments: 8,
            liked: false
          }
        ],
        likedPosts: [
          {
            id: 3,
            author: '마라톤왕',
            authorAvatar: profileImg, // 이미지 경로 변경
            content: '처음으로 하프 마라톤 완주했습니다! 21km 완주 인증! 다음 목표는 풀 마라톤 도전!',
            time: '1일 전',
            likes: 76,
            comments: 12,
            liked: true
          }
        ],
        feedPosts: [
          {
            id: 4,
            author: '달림이',
            authorAvatar: profileImg, // 이미지 경로 변경
            content: '오늘 새벽 러닝 완료! 아침 공기가 상쾌해서 5km가 금방 지나갔네요.',
            time: '2시간 전',
            likes: 32,
            comments: 3,
            liked: true
          },
          {
            id: 5,
            author: '조깅중독',
            authorAvatar: profileImg, // 이미지 경로 변경
            content: '비 오는 날씨에 러닝하는 맛이란.. 적당히 맞는 빗방울과 함께하는 7km 완료!',
            time: '어제',
            likes: 28,
            comments: 6,
            liked: false
          }
        ],
        followers: [
          { id: 101, nickname: '달림이', activityLevel: '신나는 강아지', avatar: profileImg }, // 이미지 경로 변경
          { id: 102, nickname: '조깅중독', activityLevel: '힘찬 질주 말', avatar: profileImg } // 이미지 경로 변경
        ],
        following: [
          { id: 201, nickname: '마라톤왕', activityLevel: '전광석화 치타', avatar: profileImg }, // 이미지 경로 변경
          { id: 101, nickname: '달림이', activityLevel: '신나는 강아지', avatar: profileImg } // 이미지 경로 변경
        ]
      };
    },
    methods: {
      calculateLevel() {
        // 총 달린 거리에 따라 레벨 계산 (예시)
        const distance = this.user.total_distance;
        if (distance < 50) return 1;
        if (distance < 100) return 2;
        if (distance < 200) return 3;
        if (distance < 300) return 4;
        if (distance < 500) return 5;
        return Math.floor(distance / 100) + 1;
      },
      getActivityLevel() {
        return this.user.activity_level;
      },
      getActivityGoal() {
        return this.user.activity_goal;
      },
      formatDistance(distance) {
        if (distance === null || distance === undefined) return '0km';
        return distance.toFixed(1) + 'km';
      },
      editProfile() {
        // 프로필 수정 페이지로 이동
        this.$router.push('/profile/edit');
      },
      goToNewPost() {
        // 새 게시글 작성 페이지로 이동
        this.$router.push('/board/write');
      },
      goToBoard() {
        // 게시판으로 이동
        this.$router.push('/board');
      },
      goToDiscover() {
        // 유저 검색 페이지로 이동
        this.$router.push('/discover');
      },
      goToComments(postId) {
        // 댓글 페이지로 이동
        this.$router.push(`/board/${postId}`);
      },
      toggleLike(postId) {
        // 모든 게시글 배열에서 해당 게시글 찾아 좋아요 상태 토글
        const updateLike = (posts) => {
          const post = posts.find(p => p.id === postId);
          if (post) {
            post.liked = !post.liked;
            post.likes += post.liked ? 1 : -1;
          }
        };
        
        updateLike(this.myPosts);
        updateLike(this.likedPosts);
        updateLike(this.feedPosts);
      },
      toggleFollow(userId) {
        // 팔로우/언팔로우 처리
        if (this.isFollowing(userId)) {
          this.following = this.following.filter(f => f.id !== userId);
        } else {
          // 팔로워 목록에서 해당 유저 찾기
          const userToFollow = this.followers.find(f => f.id === userId);
          if (userToFollow) {
            this.following.push(userToFollow);
          }
        }
      },
      isFollowing(userId) {
        return this.following.some(f => f.id === userId);
      }
    }
  };
  </script>
  
  <style scoped>
  .profile-container {
    background-color: #FFF8F2;
    min-height: 100vh;
    padding-bottom: 60px;
    font-family: 'Pretendard', 'Noto Sans KR', sans-serif;
  }
  
  .user-profile-card {
    background-color: white;
    border-radius: 16px;
    margin: 20px 16px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  }
  
  .profile-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
  }
  
  .profile-avatar {
    position: relative;
    margin-right: 16px;
  }
  
  .avatar-img {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    object-fit: cover;
    border: 2px solid #FF7043;
  }
  
  .level-badge {
    position: absolute;
    bottom: 0;
    right: 0;
    background-color: #FF7043;
    color: white;
    border-radius: 12px;
    font-size: 12px;
    padding: 2px 8px;
    font-weight: 600;
  }
  
  .profile-basic-info {
    flex: 1;
  }
  
  .user-nickname {
    font-size: 22px;
    font-weight: 700;
    margin: 0 0 4px;
    color: #333;
  }
  
  .user-desc {
    font-size: 14px;
    color: #666;
    margin: 0 0 10px;
  }
  
  .edit-profile-btn {
    display: inline-block;
    background-color: #F0F0F0;
    color: #555;
    font-size: 13px;
    padding: 6px 12px;
    border-radius: 16px;
    cursor: pointer;
  }
  
  .edit-profile-btn:hover {
    background-color: #E0E0E0;
  }
  
  .follow-stats {
    display: flex;
    justify-content: space-around;
    padding: 12px 0;
    border-top: 1px solid #f2f2f2;
    border-bottom: 1px solid #f2f2f2;
    margin: 0 -8px 20px;
  }
  
  .follow-stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  
  .follow-count {
    font-size: 18px;
    font-weight: 700;
    color: #333;
  }
  
  .follow-label {
    font-size: 13px;
    color: #888;
  }
  
  .detailed-info {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .info-label {
    font-size: 14px;
    color: #555;
  }
  
  .info-value {
    font-size: 14px;
    color: #333;
    font-weight: 500;
  }
  
  .info-value-goal {
    font-size: 14px;
    color: #FF7043;
    font-weight: 600;
  }
  
  .info-value-highlight {
    font-size: 16px;
    color: #FF7043;
    font-weight: 700;
  }
  
  .total-distance {
    margin-top: 4px;
  }
  
  /* 탭 메뉴 */
  .tab-container {
    display: flex;
    overflow-x: auto;
    background-color: white;
    box-shadow: 0 2px 4px rgba(0,0,0,0.04);
    margin-bottom: 12px;
    white-space: nowrap;
    -ms-overflow-style: none;  /* IE and Edge */
    scrollbar-width: none;  /* Firefox */
  }
  
  .tab-container::-webkit-scrollbar {
    display: none; /* Chrome, Safari, Opera */
  }
  
  .tab-item {
    padding: 14px 16px;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    position: relative;
    flex-shrink: 0;
  }
  
  .tab-item.active {
    color: #FF7043;
    font-weight: 600;
  }
  
  .tab-item.active::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 2px;
    background-color: #FF7043;
  }
  
  /* 탭 콘텐츠 */
  .tab-content {
    margin: 0 16px;
  }
  
  .tab-panel {
    margin-bottom: 20px;
  }
  
  .empty-state {
    text-align: center;
    padding: 40px 0;
    color: #888;
  }
  
  .empty-state p {
    margin-bottom: 16px;
  }
  
  .action-btn {
    background-color: #FF7043;
    color: white;
    border: none;
    border-radius: 24px;
    padding: 10px 20px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
  }
  
  /* 게시글 카드 */
  .post-card {
    background-color: white;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 16px;
    box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  }
  
  .post-header {
    display: flex;
    align-items: center;
    margin-bottom: 12px;
  }
  
  .post-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    margin-right: 10px;
  }
  
  .post-author {
    font-weight: 600;
    color: #333;
    margin-right: 8px;
  }
  
  .post-time {
    font-size: 12px;
    color: #999;
  }
  
  .post-content {
    font-size: 14px;
    line-height: 1.5;
    margin-bottom: 12px;
    color: #333;
  }
  
  .post-footer {
    display: flex;
    gap: 16px;
  }
  
  .post-action {
    display: flex;
    align-items: center;
    gap: 4px;
    cursor: pointer;
  }
  
  .action-icon {
    font-size: 16px;
    opacity: 0.7;
  }
  
  .action-icon.active {
    opacity: 1;
  }
  
  .action-count {
    font-size: 14px;
    color: #666;
  }
  
  /* 유저 리스트 */
  .user-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .user-item {
    display: flex;
    align-items: center;
    background-color: white;
    border-radius: 12px;
    padding: 12px;
    box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  }
  
  .user-item-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    margin-right: 12px;
  }
  
  .user-item-info {
    flex: 1;
  }
  
  .user-item-name {
    font-size: 15px;
    font-weight: 600;
    color: #333;
    display: block;
    margin-bottom: 2px;
  }
  
  .user-item-desc {
    font-size: 13px;
    color: #777;
  }
  
  .follow-btn {
    background-color: #FF7043;
    color: white;
    border: none;
    border-radius: 20px;
    padding: 8px 16px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
  }
  
  .follow-btn.following {
    background-color: #F0F0F0;
    color: #555;
  }
  </style>