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
            <h2 class="user-nickname">{{ profileUser.nickname }}</h2>
            <p class="user-desc">{{ getActivityLevel() }}</p>
            
            <!-- 내 프로필일 경우 편집 버튼 표시 -->
            <div v-if="isMyProfile" class="edit-profile-btn" @click="editProfile">
              프로필 수정
            </div>
            
            <!-- 다른 사람 프로필일 경우 팔로우 버튼 표시 -->
            <button 
              v-else 
              :class="['follow-btn', { 'following': isFollowing }]"
              @click="toggleFollowUser"
            >
              {{ isFollowing ? '팔로잉' : '팔로우' }}
            </button>
          </div>
        </div>
        
        <!-- 팔로우 정보 -->
        <div class="follow-stats">
          <div class="follow-stat-item">
            <span class="follow-count">{{ stats.totalRuns }}</span>
            <span class="follow-label">러닝</span>
          </div>
          <div class="follow-stat-item" @click="activeTab = 2">
            <span class="follow-count">{{ followers.length }}</span>
            <span class="follow-label">팔로워</span>
          </div>
          <div class="follow-stat-item" @click="activeTab = 3">
            <span class="follow-count">{{ following.length }}</span>
            <span class="follow-label">팔로잉</span>
          </div>
        </div>
        
        <!-- 상세 정보 - 내 프로필일 때만 모든 정보 표시 -->
        <div class="detailed-info">
          <div class="info-row" v-if="isMyProfile || profileUser.age">
            <span class="info-label">나이</span>
            <span class="info-value">{{ profileUser.age }}세</span>
          </div>
          <div class="info-row" v-if="isMyProfile || profileUser.gender">
            <span class="info-label">성별</span>
            <span class="info-value">{{ profileUser.gender }}</span>
          </div>
          <div class="info-row" v-if="isMyProfile">
            <span class="info-label">키 / 몸무게</span>
            <span class="info-value">{{ profileUser.height }}cm / {{ profileUser.weight }}kg</span>
          </div>
          <div class="info-row">
            <span class="info-label">목표 활동성</span>
            <span class="info-value-goal">{{ getActivityGoal() }}</span>
          </div>
          <div class="info-row total-distance">
            <span class="info-label">총 달린 거리</span>
            <span class="info-value-highlight">{{ formatDistance(profileUser.total_distance) }}</span>
          </div>
          <div class="info-row avg-distance">
            <span class="info-label">평균 달린 거리</span>
            <span class="info-value">{{ formatDistance(profileUser.avg_distance) }}</span>
          </div>
        </div>
      </div>
  
      <!-- 탭 메뉴 - 내 프로필과 다른 사람 프로필에 따라 다르게 표시 -->
      <div class="tab-container">
        <div 
          v-for="(tab, index) in isMyProfile ? myProfileTabs : otherProfileTabs" 
          :key="index" 
          :class="['tab-item', { active: activeTab === index }]"
          @click="activeTab = index"
        >
          {{ tab }}
        </div>
      </div>
  
      <!-- 탭 콘텐츠 영역 -->
      <div class="tab-content">
        <!-- 게시글 탭 -->
        <div v-if="activeTab === 0" class="posts tab-panel">
          <div v-if="userPosts.length === 0" class="empty-state">
            <p v-if="isMyProfile">아직 작성한 게시글이 없어요 😊</p>
            <p v-else>{{ profileUser.nickname }}님이 작성한 게시글이 없어요</p>
            <button v-if="isMyProfile" class="action-btn" @click="goToNewPost">첫 게시글 작성하기</button>
          </div>
          <post-card 
            v-for="post in userPosts" 
            :key="post.id" 
            :post="post"
            @like="toggleLike"
            @comment="goToComments"
          />
        </div>
  
        <!-- 좋아요한 글 탭 (내 프로필일 때만) -->
        <div v-else-if="activeTab === 1 && isMyProfile" class="liked-posts tab-panel">
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
            <p v-if="isMyProfile">아직 팔로워가 없어요 👀</p>
            <p v-else>{{ profileUser.nickname }}님의 팔로워가 없어요</p>
            <button v-if="isMyProfile" class="action-btn" @click="goToDiscover">다른 러너 찾아보기</button>
          </div>
          <div v-else class="user-list">
            <div v-for="follower in followers" :key="follower.id" class="user-item">
              <img :src="defaultAvatar" alt="프로필" class="user-item-avatar">
              <div class="user-item-info">
                <span class="user-item-name">{{ follower.nickname }}</span>
                <span class="user-item-desc">{{ follower.activityLevel }}</span>
              </div>
              <button 
                v-if="myId !== follower.id"
                :class="['follow-btn', { 'following': isUserFollowing(follower.id) }]"
                @click="toggleFollow(follower.id)"
              >
                {{ isUserFollowing(follower.id) ? '팔로잉' : '팔로우' }}
              </button>
            </div>
          </div>
        </div>
  
        <!-- 팔로잉 탭 -->
        <div v-else-if="activeTab === 3" class="following-panel tab-panel">
          <div v-if="following.length === 0" class="empty-state">
            <p v-if="isMyProfile">아직 팔로우 중인 러너가 없어요 🏃‍♀️</p>
            <p v-else>{{ profileUser.nickname }}님이 팔로우 중인 러너가 없어요</p>
            <button v-if="isMyProfile" class="action-btn" @click="goToDiscover">러너 찾아보기</button>
          </div>
          <div v-else class="user-list">
            <div v-for="follow in following" :key="follow.id" class="user-item">
              <img :src="defaultAvatar" alt="프로필" class="user-item-avatar">
              <div class="user-item-info">
                <span class="user-item-name">{{ follow.nickname }}</span>
                <span class="user-item-desc">{{ follow.activityLevel }}</span>
              </div>
              <button 
                v-if="myId !== follow.id"
                :class="['follow-btn', 'following']" 
                @click="toggleFollow(follow.id)"
              >
                팔로잉
              </button>
            </div>
          </div>
        </div>
  
        <!-- 피드 탭 (내 프로필일 때만) -->
        <div v-else-if="activeTab === 4 && isMyProfile" class="feed-panel tab-panel">
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
    props: {
      // 프로필 ID (URL에서 받아올 수 있음)
      userId: {
        type: [Number, String],
        default: null
      }
    },
    data() {
      return {
        // 이미지 경로
        defaultAvatar: profileImg,
        
        // 내 ID (로그인한 사용자)
        myId: 1, // 실제로는 로그인 정보에서 가져옴
        
        activeTab: 0,
        myProfileTabs: ['내 게시글', '좋아요한 글', '팔로워', '팔로잉', '피드'],
        otherProfileTabs: ['게시글', '팔로워', '팔로잉'],
        
        // 현재 표시 중인 사용자 데이터 (기본값은 내 프로필)
        profileUser: {
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
        
        // 팔로우 상태 (다른 사람 프로필 조회 시)
        isFollowing: false,
        
        // 샘플 데이터
        userPosts: [
          {
            id: 1,
            author: '러닝마니아',
            authorAvatar: profileImg,
            content: '오늘도 힘차게 5km 뛰었습니다! 날씨가 좋아서 기분도 좋고 컨디션도 최고였어요 😊',
            time: '3시간 전',
            likes: 24,
            comments: 5,
            liked: false
          },
          {
            id: 2,
            author: '러닝마니아',
            authorAvatar: profileImg,
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
            authorAvatar: profileImg,
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
            authorAvatar: profileImg,
            content: '오늘 새벽 러닝 완료! 아침 공기가 상쾌해서 5km가 금방 지나갔네요.',
            time: '2시간 전',
            likes: 32,
            comments: 3,
            liked: true
          },
          {
            id: 5,
            author: '조깅중독',
            authorAvatar: profileImg,
            content: '비 오는 날씨에 러닝하는 맛이란.. 적당히 맞는 빗방울과 함께하는 7km 완료!',
            time: '어제',
            likes: 28,
            comments: 6,
            liked: false
          }
        ],
        followers: [
          { id: 101, nickname: '달림이', activityLevel: '신나는 강아지', avatar: profileImg },
          { id: 102, nickname: '조깅중독', activityLevel: '힘찬 질주 말', avatar: profileImg }
        ],
        following: [
          { id: 201, nickname: '마라톤왕', activityLevel: '전광석화 치타', avatar: profileImg },
          { id: 101, nickname: '달림이', activityLevel: '신나는 강아지', avatar: profileImg }
        ],
        // 다른 사용자 프로필 예시 (실제로는 API에서 가져옴)
        otherUsers: [
          {
            id: 201,
            nickname: '마라톤왕',
            gender: '남성',
            age: 35,
            activity_level: '전광석화 치타',
            activity_goal: '전광석화 치타',
            avg_distance: 12.5,
            total_distance: 1248.7
          }
        ]
      };
    },
    computed: {
      // 내 프로필인지 다른 사람 프로필인지 확인
      isMyProfile() {
        return !this.userId || parseInt(this.userId) === this.myId;
      }
    },
    created() {
      this.loadProfileData();
    },
    methods: {
      // 프로필 데이터 로드
      loadProfileData() {
        // 다른 사람 프로필을 보는 경우
        if (this.userId && parseInt(this.userId) !== this.myId) {
          // 실제로는 API에서 데이터를 가져옴
          const otherUser = this.otherUsers.find(u => u.id === parseInt(this.userId));
          
          if (otherUser) {
            this.profileUser = otherUser;
            // 팔로잉 여부 확인
            this.isFollowing = this.following.some(f => f.id === otherUser.id);
            
            // 해당 사용자의 게시글 가져오기 (실제로는 API 호출)
            this.userPosts = this.feedPosts.filter(p => p.author === otherUser.nickname);
            
            // 탭을 게시글 탭으로 설정
            this.activeTab = 0;
          } else {
            // 사용자를 찾을 수 없는 경우 처리
            this.$router.push('/404');
          }
        } else {
          // 내 프로필을 보는 경우
          this.profileUser = {
            id: this.myId,
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
          };
          
          // 내 게시글 로드
          this.userPosts = this.userPosts;
        }
      },
      
      calculateLevel() {
        // 총 달린 거리에 따라 레벨 계산 (예시)
        const distance = this.profileUser.total_distance;
        if (distance < 50) return 1;
        if (distance < 100) return 2;
        if (distance < 200) return 3;
        if (distance < 300) return 4;
        if (distance < 500) return 5;
        return Math.floor(distance / 100) + 1;
      },
      
      getActivityLevel() {
        return this.profileUser.activity_level;
      },
      
      getActivityGoal() {
        return this.profileUser.activity_goal;
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
        
        updateLike(this.userPosts);
        updateLike(this.likedPosts);
        updateLike(this.feedPosts);
      },
      
      // 팔로우/언팔로우 토글 (프로필 페이지 상단 버튼)
      toggleFollowUser() {
        this.isFollowing = !this.isFollowing;
        
        if (this.isFollowing) {
          // 팔로잉 목록에 추가
          this.following.push({
            id: this.profileUser.id,
            nickname: this.profileUser.nickname,
            activityLevel: this.profileUser.activity_level,
            avatar: profileImg
          });
        } else {
          // 팔로잉 목록에서 제거
          this.following = this.following.filter(f => f.id !== this.profileUser.id);
        }
        
        // 실제로는 서버에 API 요청
      },
      
      // 사용자 팔로우 여부 확인 (팔로워/팔로잉 목록에서 사용)
      isUserFollowing(userId) {
        return this.following.some(f => f.id === userId);
      },
      
      // 팔로우/언팔로우 토글 (팔로워/팔로잉 목록에서 사용)
      toggleFollow(userId) {
        if (this.isUserFollowing(userId)) {
          // 팔로잉 목록에서 제거
          this.following = this.following.filter(f => f.id !== userId);
        } else {
          // 팔로워 목록에서 해당 유저 찾기
          const userToFollow = this.followers.find(f => f.id === userId);
          if (userToFollow) {
            // 팔로잉 목록에 추가
            this.following.push(userToFollow);
          }
        }
        
        // 실제로는 서버에 API 요청
      }
    },
    watch: {
      // userId가 변경되면 프로필 데이터 다시 로드
      userId() {
        this.loadProfileData();
      }
    }
  };
  </script>

<style scoped>
/* 기본 프로필 컨테이너 스타일 */
.profile-container {
  background-color: #FFF8F2;
  min-height: 100vh;
  padding-bottom: 60px;
  font-family: 'Pretendard', 'Noto Sans KR', sans-serif;
  padding-top: 20px; /* 상단에 여백 추가 */
}

/* 사용자 프로필 카드 스타일 */
.user-profile-card {
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
  margin: 0 16px;
}

/* 프로필 헤더 스타일 */
.profile-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
}

/* 프로필 아바타 스타일 */
.profile-avatar {
  position: relative;
  margin-right: 20px;
}

.avatar-img {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #FF7E36;
}

.level-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  background-color: #FF7E36;
  color: white;
  font-size: 12px;
  font-weight: bold;
  padding: 3px 8px;
  border-radius: 10px;
}

/* 프로필 기본 정보 스타일 */
.profile-basic-info {
  flex: 1;
}

.user-nickname {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  margin: 0 0 5px 0;
}

.user-desc {
  color: #666;
  font-size: 14px;
  margin: 0 0 10px 0;
}

/* 프로필 편집 버튼 */
.edit-profile-btn {
  display: inline-block;
  background-color: #f1f1f1;
  color: #333;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.edit-profile-btn:hover {
  background-color: #e5e5e5;
}

/* 팔로우 버튼 스타일 */
.follow-btn {
  background-color: #FF7E36;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.follow-btn.following {
  background-color: #f1f1f1;
  color: #333;
}

.follow-btn:hover {
  opacity: 0.9;
}

/* 팔로우 통계 스타일 */
.follow-stats {
  display: flex;
  justify-content: space-around;
  margin: 20px 0;
  padding: 16px 0;
  border-top: 1px solid #f1f1f1;
  border-bottom: 1px solid #f1f1f1;
}

.follow-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.follow-count {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.follow-label {
  font-size: 13px;
  color: #777;
  margin-top: 4px;
}

/* 상세 정보 스타일 */
.detailed-info {
  margin-top: 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.info-label {
  font-size: 14px;
  color: #777;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.info-value-goal {
  font-size: 14px;
  color: #FF7E36;
  font-weight: 600;
}

.info-value-highlight {
  font-size: 16px;
  color: #FF7E36;
  font-weight: 700;
}

.total-distance {
  margin-top: 10px;
}

.avg-distance {
  margin-bottom: 6px;
}

/* 탭 컨테이너 스타일 */
.tab-container {
  display: flex;
  background-color: #ffffff;
  margin: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow-x: auto;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 16px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  white-space: nowrap;
  transition: color 0.2s, border-bottom 0.2s;
  border-bottom: 2px solid transparent;
}

.tab-item.active {
  color: #FF7E36;
  border-bottom: 2px solid #FF7E36;
  font-weight: 600;
}

/* 탭 콘텐츠 영역 스타일 */
.tab-content {
  padding: 0 16px;
}

.tab-panel {
  background-color: transparent;
  min-height: 200px;
}

/* 빈 상태 스타일 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
}

.empty-state p {
  color: #666;
  margin-bottom: 20px;
}

.action-btn {
  background-color: #FF7E36;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.action-btn:hover {
  background-color: #EF6926;
}

/* 게시글 카드 스타일 */
.post-card {
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 16px;
  margin-bottom: 16px;
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
  margin-right: 12px;
}

.post-author {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-right: 8px;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-content {
  margin: 0 0 16px 0;
  font-size: 14px;
  line-height: 1.5;
  color: #333;
}

.post-footer {
  display: flex;
  border-top: 1px solid #f1f1f1;
  padding-top: 12px;
}

.post-action {
  display: flex;
  align-items: center;
  margin-right: 24px;
  cursor: pointer;
}

.action-icon {
  margin-right: 6px;
  font-size: 16px;
  opacity: 0.6;
}

.action-icon.active {
  opacity: 1;
}

.action-count {
  font-size: 14px;
  color: #666;
}

/* 유저 목록 스타일 */
.user-list {
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.user-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f1f1f1;
}

.user-item:last-child {
  border-bottom: none;
}

.user-item-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 16px;
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

/* 반응형 스타일 */
@media (max-width: 480px) {
  .profile-header {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .profile-avatar {
    margin-right: 0;
    margin-bottom: 16px;
  }
  
  .tab-item {
    padding: 14px 12px;
    font-size: 13px;
  }
  
  .detailed-info {
    margin-top: 8px;
  }
  
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    padding: 8px 0;
    border-bottom: 1px solid #f3f3f3;
  }
  
  .info-row:last-child {
    border-bottom: none;
  }
  
  .info-label {
    margin-bottom: 4px;
    color: #888;
    font-size: 12px;
  }
}
</style>