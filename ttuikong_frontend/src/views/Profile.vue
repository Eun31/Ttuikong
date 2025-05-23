<template>
  <div class="profile-container">
    <PasswordConfirmModal :showModal="showPasswordModal" @close="closePasswordModal" @confirm="handlePasswordConfirm"
      ref="passwordModal" />
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>프로필을 불러오는 중...</p>
    </div>

    <div v-else-if="error" class="error-container">
      <p>{{ error }}</p>
      <button @click="loadProfileData" class="retry-btn">다시 시도</button>
    </div>

    <div v-else>
      <div class="user-profile-card">
        <div class="profile-header">
          <div class="profile-avatar">
            <img :src="defaultAvatar" class="avatar-img">
          </div>
          <div class="profile-basic-info">
            <h2 class="user-nickname">{{ profileUser.nickname }}</h2>
            <p class="user-desc">{{ getActivityLevel() }}</p>

            <div v-if="isMyProfile" class="edit-profile-btn" @click="showPasswordConfirm">
              프로필 수정
            </div>

            <button v-else :class="['follow-btn', { 'following': isFollowing }]" @click="toggleFollowUser"
              :disabled="followLoading">
              {{ followLoading ? '처리중...' : (isFollowing ? '팔로잉' : '팔로우') }}
            </button>
          </div>
        </div>

        <div class="follow-stats">
          <div class="follow-stat-item">
            <span class="follow-count">{{ stats.totalRuns }}</span>
            <span class="follow-label">러닝</span>
          </div>
          <div class="follow-stat-item" @click="activeTab = isMyProfile ? 2 : 1">
            <span class="follow-count">{{ stats.followerCount }}</span>
            <span class="follow-label">팔로워</span>
          </div>
          <div class="follow-stat-item" @click="activeTab = isMyProfile ? 3 : 2">
            <span class="follow-count">{{ stats.followingCount }}</span>
            <span class="follow-label">팔로잉</span>
          </div>
        </div>

        <div class="detailed-info">
          <div class="info-row" v-if="isMyProfile || profileUser.age">
            <span class="info-label">나이</span>
            <span class="info-value">{{ profileUser.age }}세</span>
          </div>
          <div class="info-row" v-if="isMyProfile || profileUser.gender">
            <span class="info-label">성별</span>
            <span class="info-value">{{ profileUser.gender }}</span>
          </div>
          <div class="info-row" v-if="isMyProfile && profileUser.height && profileUser.weight">
            <span class="info-label">키 / 몸무게</span>
            <span class="info-value">{{ profileUser.height }}cm / {{ profileUser.weight }}kg</span>
          </div>
          <div class="info-row">
            <span class="info-label">목표 활동성</span>
            <span class="info-value-goal">{{ getActivityGoal() }}</span>
          </div>
          <div class="info-row total-distance">
            <span class="info-label">총 달린 거리</span>
            <span class="info-value-highlight">{{ formatDistance(profileUser.totalDistance) }}</span>
          </div>
          <!-- <div class="info-row avg-distance">
            <span class="info-label">평균 달린 거리</span>
            <span class="info-value">{{ formatDistance(profileUser.avgDistance) }}</span>
          </div> -->
        </div>
      </div>

      <div class="tab-container">
        <div v-for="(tab, index) in currentTabs" :key="index" :class="['tab-item', { active: activeTab === index }]"
          @click="changeTab(index)">
          {{ tab }}
        </div>
      </div>

      <div class="tab-content">
        <div v-if="activeTab === 0" class="posts tab-panel">
          <div v-if="postsLoading" class="loading-posts">
            <div class="loading-spinner small"></div>
            <p>게시글을 불러오는 중...</p>
          </div>
          <div v-else-if="userPosts.length === 0" class="empty-state">
            <p v-if="isMyProfile">아직 작성한 게시글이 없어요 😊</p>
            <p v-else>{{ profileUser.nickname }}님이 작성한 게시글이 없어요</p>
            <button v-if="isMyProfile" class="action-btn" @click="goToNewPost">첫 게시글 작성하기</button>
          </div>
          <post-card v-else v-for="post in userPosts" :key="post.id" :post="post" @click="goToPostDetail(post.id)" />
        </div>

        <div v-else-if="activeTab === 1 && isMyProfile" class="liked-posts tab-panel">
          <div v-if="likedPostsLoading" class="loading-posts">
            <div class="loading-spinner small"></div>
            <p>좋아요한 게시글을 불러오는 중...</p>
          </div>
          <div v-else-if="likedPosts.length === 0" class="empty-state">
            <p>아직 좋아요한 게시글이 없어요 💖</p>
            <button class="action-btn" @click="goToBoard">게시판 둘러보기</button>
          </div>
          <post-card v-else v-for="post in likedPosts" :key="post.id" :post="post" @click="goToPostDetail(post.id)" />
        </div>

        <div v-else-if="(isMyProfile && activeTab === 2) || (!isMyProfile && activeTab === 1)"
          class="followers-panel tab-panel">
          <div v-if="followersLoading" class="loading-posts">
            <div class="loading-spinner small"></div>
            <p>팔로워 목록을 불러오는 중...</p>
          </div>
          <div v-else-if="followers.length === 0" class="empty-state">
            <p v-if="isMyProfile">아직 팔로워가 없어요 👀</p>
            <p v-else>{{ profileUser.nickname }}님의 팔로워가 없어요</p>
            <button v-if="isMyProfile" class="action-btn" @click="goToDiscover">다른 러너 찾아보기</button>
          </div>
          <div v-else class="user-list">
            <div v-for="follower in followers" :key="follower.id" class="user-item">
              <div class="user-clickable-area" @click.stop="goToUserProfile(follower.id)">
                <img :src="defaultAvatar" class="user-item-avatar">
                <div class="user-item-info">
                  <span class="user-item-name">{{ follower.nickname }}</span>
                  <span class="user-item-desc">{{ follower.activityLevel }}</span>
                </div>
              </div>
              <button v-if="currentUserId !== follower.id"
                :class="['follow-btn', { 'following': isUserFollowing(follower.id) }]"
                @click.stop="toggleFollow(follower.id)" :disabled="follower.loading">
                {{ follower.loading ? '처리중...' : (isUserFollowing(follower.id) ? '팔로잉' : '팔로우') }}
              </button>
            </div>
          </div>
        </div>

        <div v-else-if="(isMyProfile && activeTab === 3) || (!isMyProfile && activeTab === 2)"
          class="following-panel tab-panel">
          <div v-if="followingLoading" class="loading-posts">
            <div class="loading-spinner small"></div>
            <p>팔로잉 목록을 불러오는 중...</p>
          </div>
          <div v-else-if="following.length === 0" class="empty-state">
            <p v-if="isMyProfile">아직 팔로우 중인 러너가 없어요 🏃‍♀️</p>
            <p v-else>{{ profileUser.nickname }}님이 팔로우 중인 러너가 없어요</p>
            <button v-if="isMyProfile" class="action-btn" @click="goToDiscover">러너 찾아보기</button>
          </div>
          <div v-else class="user-list">
            <div v-for="follow in following" :key="follow.id" class="user-item">
              <div class="user-clickable-area" @click.stop="goToUserProfile(follow.id)">
                <img :src="defaultAvatar" class="user-item-avatar">
                <div class="user-item-info">
                  <span class="user-item-name">{{ follow.nickname }}</span>
                  <span class="user-item-desc">{{ follow.activityLevel }}</span>
                </div>
              </div>
              <button v-if="currentUserId !== follow.id" :class="['follow-btn', 'following']"
                @click.stop="toggleFollow(follow.id)" :disabled="follow.loading">
                {{ follow.loading ? '처리중...' : '팔로잉' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div class="delete-container">
    <button @click="deleteUser" class="delete-btn">회원 탈퇴</button>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import profileImg from '../assets/profile.png';
import PostCard from '../components/PostCard.vue';
import PasswordConfirmModal from '../components/PasswordConfirmModal.vue';

const props = defineProps({
  userId: {
    type: [Number, String],
    default: null
  }
});

const router = useRouter();

// API 설정
const API_URL = 'http://localhost:8080/api';

// 인증 헤더 생성
const authHeader = computed(() => {
  return token ? { 'Authorization': `Bearer ${token}` } : {};
});

// 인증 토큰 및 기본값
const token = localStorage.getItem('jwt');
const defaultAvatar = profileImg;
const currentUserId = ref(null);
const activeTab = ref(0);
const loading = ref(true);
const error = ref(null);
const postsLoading = ref(false);
const likedPostsLoading = ref(false);
const followersLoading = ref(false);
const followingLoading = ref(false);
const followLoading = ref(false);

const showPasswordModal = ref(false);
const passwordModal = ref(null);

// 데이터
const profileUser = reactive({
  id: null,
  email: '',
  nickname: '',
  gender: '',
  age: null,
  height: null,
  weight: null,
  activityLevel: '',
  activityGoal: '',
  avgDistance: 0,
  totalDistance: 0,
  profileImage: 'profileImage',
  role: 'USER'
});

const stats = reactive({
  totalRuns: 0,
  followerCount: 0,
  followingCount: 0
});

const isFollowing = ref(false);
const userPosts = ref([]);
const likedPosts = ref([]);
const feedPosts = ref([]);
const followers = ref([]);
const following = ref([]);

// 탭 설정
const myProfileTabs = ['내 게시글', '좋아요한 글', '팔로워', '팔로잉'];
const otherProfileTabs = ['게시글', '팔로워', '팔로잉'];

const currentTabs = computed(() => {
  return isMyProfile.value ? myProfileTabs : otherProfileTabs;
});

const isMyProfile = computed(() => {
  return !props.userId || parseInt(props.userId) === currentUserId.value;
});

// 에러 처리 함수
const handleApiError = (err) => {
  console.error('API Error:', err);

  if (err.response) {
    const { status, data } = err.response;

    switch (status) {
      case 400:
        return data.message || '잘못된 요청입니다.';
      case 401:
        return '인증이 필요합니다. 다시 로그인해주세요.';
      case 403:
        return '접근 권한이 없습니다.';
      case 404:
        return '요청한 정보를 찾을 수 없습니다.';
      case 409:
        return data.message || '이미 존재하는 데이터입니다.';
      case 422:
        return data.message || '입력 데이터가 올바르지 않습니다.';
      case 500:
        return '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
      default:
        return data.message || '알 수 없는 오류가 발생했습니다.';
    }
  }

  if (err.request) {
    return '네트워크 연결을 확인해주세요.';
  }

  return err.message || '알 수 없는 오류가 발생했습니다.';
};

const getCurrentUser = async () => {
  if (!token) {
    console.log('로그인되지 않은 사용자입니다.');
    return null;
  }

  try {
    const response = await axios.get(`${API_URL}/users/me`, {
      headers: authHeader.value
    });
    const userData = response.data.user;
    currentUserId.value = userData.id;
    return userData;
  } catch (err) {
    console.error('현재 사용자 정보 조회 실패:', err);

    if (err.response && (err.response.status === 401 || err.response.status === 403)) {
      console.log('토큰이 만료되었거나 유효하지 않음');
      localStorage.removeItem('jwt');
      currentUserId.value = null;
    }

    throw err;
  }
};

const getProfile = async (userId) => {

  const response = await axios.get(`${API_URL}/users/${userId}/profile`, {
    headers: authHeader.value
  });
  return response.data;
};

const getFollowers = async (userId) => {
  if (!token || currentUserId.value !== userId) {
    return [];
  }

  const response = await axios.get(`${API_URL}/users/${userId}/followers`, {
    headers: authHeader.value
  });

  const followerIds = response.data;
  const followerUsers = [];

  for (const followerId of followerIds) {
    try {
      const userResponse = await axios.get(`${API_URL}/users/${followerId}/profile`, {
        headers: authHeader.value
      });
      followerUsers.push({
        id: followerId,
        nickname: userResponse.data.nickname,
        activityLevel: userResponse.data.activityLevel
      });
    } catch (err) {
      console.warn(`팔로워 ${followerId} 정보 조회 실패:`, err);
    }
  }

  return followerUsers;
};

const getFollowing = async (userId) => {
  if (!token || currentUserId.value !== userId) {
    return [];
  }

  const response = await axios.get(`${API_URL}/users/${userId}/followings`, {
    headers: authHeader.value
  });

  const followingIds = response.data;
  const followingUsers = [];

  for (const followingId of followingIds) {
    try {
      const userResponse = await axios.get(`${API_URL}/users/${followingId}/profile`, {
        headers: authHeader.value
      });
      followingUsers.push({
        id: followingId,
        nickname: userResponse.data.nickname,
        activityLevel: userResponse.data.activityLevel
      });
    } catch (err) {
      console.warn(`팔로잉 ${followingId} 정보 조회 실패:`, err);
    }
  }

  return followingUsers;
};

const followUser = async (targetId) => {
  if (!token) {
    alert('로그인이 필요한 기능입니다.');
    throw new Error('로그인이 필요합니다.');
  }

  const response = await axios.post(`${API_URL}/users/${currentUserId.value}/follow/${targetId}`, {}, {
    headers: authHeader.value
  });
  return response.data;
};

const unfollowUser = async (targetId) => {
  if (!token) {
    alert('로그인이 필요한 기능입니다.');
    throw new Error('로그인이 필요합니다.');
  }

  const response = await axios.delete(`${API_URL}/users/${currentUserId.value}/follow/${targetId}`, {
    headers: authHeader.value
  });
  return response.data;
};

const getUserPosts = async (userId) => {
  try {
    const response = await axios.get(`${API_URL}/board/user/${userId}`, {
      headers: authHeader.value
    });
    if (typeof response.data === 'string') {
      return [];
    }

    if (!Array.isArray(response.data)) {
      return [];
    }

    const postsWithUserInfo = response.data.map(post => ({
      ...post,
      id: post.postId,
      user: {
        id: post.userId,
        name: post.userNickname,
        avatar: defaultAvatar
      }
    }));

    return postsWithUserInfo;
  } catch (err) {
    console.error('사용자 게시글 조회 실패:', err);
    return [];
  }
};

const getLikedPosts = async () => {
  if (!token) return [];

  try {
    // 1. 사용자가 좋아요한 게시글 ID 목록 가져오기
    const likedResponse = await axios.get(`${API_URL}/board/user/${currentUserId.value}/likes`, {
      headers: authHeader.value
    });

    const likedBoardIds = likedResponse.data || [];

    // 2. 각 게시글 상세 정보 가져오기
    const likedPosts = [];
    for (const likeData of likedBoardIds) {
      try {
        // postId 필드에서 실제 게시글 ID 추출
        const actualPostId = likeData.postId;
        const postResponse = await axios.get(`${API_URL}/board/${actualPostId}`, {
          headers: authHeader.value
        });

        if (postResponse.data) {
          likedPosts.push({
            ...postResponse.data,
            id: postResponse.data.postId,
            liked: true,
            user: {
              id: postResponse.data.userId,
              name: postResponse.data.userNickname,
              avatar: defaultAvatar
            }
          });
        }
      } catch (err) {
        console.warn(`게시글 ${likeData.postId} 조회 실패:`, err);
      }
    }
    return likedPosts;
  } catch (err) {
    console.error('좋아요한 게시글 조회 실패:', err);
    return [];
  }
};

const getFollowStatus = async (targetId) => {
  if (!token) return { isFollowing: false };

  try {
    const followingList = await getFollowing(currentUserId.value);
    const isFollowing = followingList.some(user => user.id === targetId);
    return { isFollowing };
  } catch (err) {
    console.warn('팔로우 상태 확인 실패:', err);
    return { isFollowing: false };
  }
};

const loadProfileData = async () => {
  try {
    loading.value = true;
    error.value = null;

    // 현재 사용자 정보 가져오기 
    if (token) {
      try {
        await getCurrentUser();
      } catch (err) {
        console.warn('현재 사용자 정보 조회 실패, 비로그인 상태로 처리');
      }
    }

    let targetUserId;
    if (props.userId) {
      targetUserId = parseInt(props.userId);

      // 다른 사용자 프로필 조회
      try {
        const profileData = await getProfile(targetUserId);
        Object.assign(profileUser, profileData);

        // 팔로우 상태 확인
        if (currentUserId.value) {
          const followStatus = await getFollowStatus(targetUserId);
          isFollowing.value = followStatus.isFollowing;
        }

        stats.followerCount = 0;
        stats.followingCount = 0;
        stats.totalRuns = 0;

        await loadTabData(0);

      } catch (err) {
        console.error('다른 사용자 프로필 조회 실패:', err);
        if (err.response?.status === 403 || err.response?.status === 404) {
          error.value = '존재하지 않는 사용자이거나 프로필 조회 권한이 없습니다.';
        } else {
          error.value = handleApiError(err);
        }
        return;
      }
    } else {
      if (!currentUserId.value) {
        error.value = '로그인이 필요합니다.';
        return;
      }
      targetUserId = currentUserId.value;

      const profileData = await getProfile(targetUserId);
      Object.assign(profileUser, profileData);

      const [followersData, followingData] = await Promise.all([
        getFollowers(targetUserId),
        getFollowing(targetUserId)
      ]);

      followers.value = followersData.map(user => ({ ...user, loading: false }));
      following.value = followingData.map(user => ({ ...user, loading: false }));

      stats.followerCount = followersData.length;
      stats.followingCount = followingData.length;
      stats.totalRuns = 0;

      await loadTabData(0);
    }

  } catch (err) {
    console.error('프로필 데이터 로드 실패:', err);
    error.value = handleApiError(err);
  } finally {
    loading.value = false;
  }
};

const loadTabData = async (tabIndex) => {
  const targetUserId = profileUser.id;

  try {
    if (tabIndex === 0) {
      // 게시글 탭
      postsLoading.value = true;
      const postsData = await getUserPosts(targetUserId);
      userPosts.value = postsData;
    } else if (tabIndex === 1 && isMyProfile.value) {
      // 좋아요한 글 탭
      likedPostsLoading.value = true;
      const likedData = await getLikedPosts();
      likedPosts.value = likedData;
    } else if (tabIndex === 2) {
      // 팔로워 탭 - 이미 데이터가 있으면 로딩하지 않음
      if (followers.value.length === 0) {
        followersLoading.value = true;
        const followersData = await getFollowers(targetUserId);
        followers.value = followersData.map(user => ({ ...user, loading: false }));
      }
    } else if (tabIndex === 3) {
      // 팔로잉 탭 - 이미 데이터가 있으면 로딩하지 않음
      if (following.value.length === 0) {
        followingLoading.value = true;
        const followingData = await getFollowing(targetUserId);
        following.value = followingData.map(user => ({ ...user, loading: false }));
      }
    }
  } catch (err) {
    console.error('탭 데이터 로드 실패:', err);
  } finally {
    postsLoading.value = false;
    likedPostsLoading.value = false;
    followersLoading.value = false;
    followingLoading.value = false;
  }
};

const changeTab = (index) => {
  activeTab.value = index;
  loadTabData(index);
};

const getActivityLevel = () => profileUser.activityLevel;
const getActivityGoal = () => profileUser.activityGoal;
const formatDistance = (distance) => distance ? distance.toFixed(1) + 'km' : '0km';

const showPasswordConfirm = () => {
  showPasswordModal.value = true;
};

const closePasswordModal = () => {
  showPasswordModal.value = false;
};

const handlePasswordConfirm = async (password) => {
  try {
    const response = await axios.post(`${API_URL}/users/verify`, {
      password: password
    }, {
      headers: authHeader.value
    });

    if (response.data.success) {
      showPasswordModal.value = false;
      router.push('/profile/edit');
    }
    else {
      passwordModal.value?.setError('비밀번호가 일치하지 않습니다.');
    }
  } catch (err) {
    passwordModal.value.setError('오류 발생');
  }
  finally {
    passwordModal.value?.setLoading(false);
  }
};

const goToNewPost = () => router.push('/board/write');
const goToBoard = () => router.push('/board');
const goToDiscover = () => router.push('/discover');

const goToPostDetail = (postId) => {
  router.push(`/board/${postId}`);
};

const goToUserProfile = (userId) => {
  if (userId === currentUserId.value) {
    router.push('/profile');
  } else {
    router.push(`/profile/${userId}`);
  }
};

const toggleFollowUser = async () => {
  if (!token) {
    alert('로그인이 필요한 기능입니다.');
    router.push('/login');
    return;
  }

  try {
    followLoading.value = true;

    if (isFollowing.value) {
      await unfollowUser(profileUser.id);
      isFollowing.value = false;
      stats.followerCount--;
    } else {
      await followUser(profileUser.id);
      isFollowing.value = true;
      stats.followerCount++;
    }
  } catch (err) {
    console.error('팔로우 처리 실패:', err);

    if (err.response && err.response.status === 401) {
      alert('로그인이 만료되었습니다. 다시 로그인해 주세요.');
      localStorage.removeItem('jwt');
      router.push('/login');
    } else if (err.response && err.response.status === 403) {
      alert('접근 권한이 없습니다.');
    } else {
      alert('팔로우 처리에 실패했습니다. 다시 시도해 주세요.');
    }
  } finally {
    followLoading.value = false;
  }
};

const isUserFollowing = (userId) => {
  return following.value.some(f => f.id === userId);
};

const toggleFollow = async (userId) => {
  if (!token) {
    alert('로그인이 필요한 기능입니다.');
    router.push('/login');
    return;
  }

  try {
    const followerUser = followers.value.find(f => f.id === userId);
    const followingUser = following.value.find(f => f.id === userId);

    if (followerUser) followerUser.loading = true;
    if (followingUser) followingUser.loading = true;

    if (isUserFollowing(userId)) {
      await unfollowUser(userId);
      following.value = following.value.filter(f => f.id !== userId);
      stats.followingCount--;
    } else {
      await followUser(userId);
      if (followerUser) {
        following.value.push({ ...followerUser, loading: false });
        stats.followingCount++;
      }
    }
  } catch (err) {
    console.error('팔로우 처리 실패:', err);

    if (err.response && err.response.status === 401) {
      alert('로그인이 만료되었습니다. 다시 로그인해 주세요.');
      localStorage.removeItem('jwt');
      router.push('/login');
    } else if (err.response && err.response.status === 403) {
      alert('접근 권한이 없습니다.');
    } else {
      alert('팔로우 처리에 실패했습니다. 다시 시도해 주세요.');
    }
  } finally {
    const followerUser = followers.value.find(f => f.id === userId);
    const followingUser = following.value.find(f => f.id === userId);

    if (followerUser) followerUser.loading = false;
    if (followingUser) followingUser.loading = false;
  }
};


watch(() => props.userId, (newUserId, oldUserId) => {
  resetComponentState();
  loadProfileData();
}, { immediate: false });


watch(() => router.currentRoute.value.fullPath, (newPath, oldPath) => {
  if (newPath.startsWith('/profile') && newPath !== oldPath) {
    resetComponentState();
    loadProfileData();
  }
}, { immediate: false });

// 컴포넌트 상태 초기화 함수
const resetComponentState = () => {
  loading.value = true;
  error.value = null;
  postsLoading.value = false;
  likedPostsLoading.value = false;
  followersLoading.value = false;
  followingLoading.value = false;
  followLoading.value = false;

  // 탭 초기화
  activeTab.value = 0;

  // 데이터 초기화
  Object.assign(profileUser, {
    id: null,
    email: '',
    nickname: '',
    gender: '',
    age: null,
    height: null,
    weight: null,
    activityLevel: '',
    activityGoal: '',
    avgDistance: 0,
    totalDistance: 0,
    profileImage: 'profileImage',
    role: 'USER'
  });

  Object.assign(stats, {
    totalRuns: 0,
    followerCount: 0,
    followingCount: 0
  });

  isFollowing.value = false;
  userPosts.value = [];
  likedPosts.value = [];
  feedPosts.value = [];
  followers.value = [];
  following.value = [];
};

const deleteUser = async () => {
  const userId = profileUser.id;
  const token = localStorage.getItem('jwt');

  try {
    await fetch(`http://localhost:8080/api/users/${userId}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`
      }
    });

    localStorage.clear();
    alert('회원 탈퇴가 완료되었습니다.');
    router.push('/');

  } catch (error) {
    console.error(error);
    alert('회원 탈퇴에 실패했습니다.');
  }
};


onMounted(async () => {
  await loadProfileData();
});
</script>

<style scoped>
/* 로딩 컨테이너 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 50vh;
  padding: 40px 20px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #FF7E36;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.loading-spinner.small {
  width: 20px;
  height: 20px;
  border-width: 2px;
  margin-bottom: 8px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

.loading-posts {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 16px;
}

.loading-posts p {
  color: #666;
  margin: 0;
}

/* 에러 컨테이너 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 50vh;
  padding: 40px 20px;
  background-color: #ffffff;
  border-radius: 12px;
  margin: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.error-container p {
  color: #666;
  margin-bottom: 20px;
  text-align: center;
}

.retry-btn {
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

.retry-btn:hover {
  background-color: #EF6926;
}

.profile-container {
  background-color: #FFF8F2;
  min-height: 100vh;
  padding-bottom: 20px;
  font-family: 'Noto Sans KR', sans-serif;
  padding-top: 20px;
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

.user-clickable-area {
  display: flex;
  align-items: center;
  flex: 1;
  cursor: pointer;
  transition: background-color 0.2s;
  padding: 4px;
  border-radius: 8px;
  margin: -4px;
  min-height: 48px;
}

.user-clickable-area:hover {
  background-color: #f8f9fa;
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

.delete-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.delete-btn {
  padding: 8px 16px;
  font-size: 14px;
  color: #999;
  background-color: transparent;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.delete-btn:hover {
  color: #fff;
  background-color: #e5a173;
  border-color: #e5a173;
}
</style>