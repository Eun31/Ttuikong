<template>
  <div class="profile-edit-container">
    <div class="page-header">
      <button class="back-button" @click="goBack">
        ←
      </button>
      <h1 class="page-title">프로필 수정</h1>
      <div class="spacer"></div>
    </div>

    <div v-if="isLoading" class="loading-container">
      <div class="spinner"></div>
      <p>프로필 정보를 불러오는 중...</p>
    </div>

    <div v-else-if="error" class="error-container">
      <p class="error-message">{{ error }}</p>
      <button @click="fetchProfile" class="retry-button">다시 시도</button>
    </div>

    <div v-else class="profile-form-card">
      <form @submit.prevent="saveProfile" class="profile-form">
        <div class="form-group">
          <label for="nickname">닉네임</label>
          <input type="text" id="nickname" v-model="userProfile.nickname" class="form-input" placeholder="닉네임을 입력하세요"
            maxlength="10" :class="{ 'error': validationErrors.nickname }" @input="validateField('nickname')" required>
          <div class="input-counter">{{ (userProfile.nickname || '').length }}/10</div>
          <div v-if="validationErrors.nickname" class="error-text">{{ validationErrors.nickname }}</div>
        </div>

        <div class="form-group">
          <label for="password">새 비밀번호</label>
          <div class="password-input-container">
            <input :type="showPassword ? 'text' : 'password'" id="password" v-model="userProfile.password"
              class="form-input password-input" placeholder="새 비밀번호를 입력하세요 (변경하지 않으려면 비워두세요)"
              :class="{ 'error': validationErrors.password }" @input="validateField('password')">
            <button type="button" class="password-toggle" @click="togglePasswordVisibility">
              <svg v-if="showPassword" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                stroke-width="2">
                <path
                  d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24">
                </path>
                <line x1="1" y1="1" x2="23" y2="23"></line>
              </svg>
              <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
            </button>
          </div>
          <div v-if="validationErrors.password" class="error-text">{{ validationErrors.password }}</div>
        </div>

        <div class="form-group" v-if="userProfile.password && userProfile.password.trim()">
          <label for="passwordConfirm">새 비밀번호 확인</label>
          <div class="password-input-container">
            <input :type="showPasswordConfirm ? 'text' : 'password'" id="passwordConfirm"
              v-model="userProfile.passwordConfirm" class="form-input password-input" placeholder="새 비밀번호를 다시 입력하세요"
              :class="{ 'error': validationErrors.passwordConfirm }" @input="validateField('passwordConfirm')">
            <button type="button" class="password-toggle" @click="togglePasswordConfirmVisibility">
              <svg v-if="showPasswordConfirm" width="20" height="20" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" stroke-width="2">
                <path
                  d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24">
                </path>
                <line x1="1" y1="1" x2="23" y2="23"></line>
              </svg>
              <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                <circle cx="12" cy="12" r="3"></circle>
              </svg>
            </button>
          </div>
          <div v-if="validationErrors.passwordConfirm" class="error-text">{{ validationErrors.passwordConfirm }}</div>
        </div>

        <div class="form-row">
          <div class="form-group half">
            <label for="age">나이</label>
            <input type="number" id="age" v-model.number="userProfile.age" class="form-input" placeholder="나이" min="1"
              max="120" :class="{ 'error': validationErrors.age }" @input="validateField('age')">
            <div v-if="validationErrors.age" class="error-text">{{ validationErrors.age }}</div>
          </div>
          <div class="form-group half">
            <label>성별</label>
            <div class="gender-selector">
              <label class="gender-option" :class="{ 'selected': userProfile.gender === 'MALE' }">
                <input type="radio" v-model="userProfile.gender" value="MALE">
                <span class="gender-icon male"></span>
                <span>남성</span>
              </label>
              <label class="gender-option" :class="{ 'selected': userProfile.gender === 'FEMALE' }">
                <input type="radio" v-model="userProfile.gender" value="FEMALE">
                <span class="gender-icon female"></span>
                <span>여성</span>
              </label>
            </div>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group half">
            <label for="height">키 (cm)</label>
            <input type="number" id="height" v-model.number="userProfile.height" class="form-input" placeholder="키"
              min="100" max="250" step="0.1">
          </div>
          <div class="form-group half">
            <label for="weight">몸무게 (kg)</label>
            <input type="number" id="weight" v-model.number="userProfile.weight" class="form-input" placeholder="몸무게"
              min="30" max="200" step="0.1">
          </div>
        </div>

        <div class="form-group">
          <label>현재 활동 레벨</label>
          <div class="activity-options">
            <div v-for="(activity, index) in activityLevelOptions" :key="index"
              :class="['activity-option', { active: userProfile.activityLevel === activity.value }]"
              @click="selectActivityLevel(activity.value)">
              <div class="activity-icon">{{ activity.icon }}</div>
              <div class="activity-info">
                <div class="activity-name">{{ activity.name }}</div>
                <div class="activity-desc">{{ activity.description }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="form-group">
          <label>목표 활동성</label>
          <div class="activity-options">
            <div v-for="(activity, index) in activityGoalOptions" :key="index"
              :class="['activity-option', { active: userProfile.activityGoal === activity.value }]"
              @click="selectActivityGoal(activity.value)">
              <div class="activity-icon">{{ activity.icon }}</div>
              <div class="activity-info">
                <div class="activity-name">{{ activity.name }}</div>
                <div class="activity-desc">{{ activity.description }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="save-button-container">
          <button type="submit" class="save-button" :disabled="isSubmitting || !isFormValid">
            <span v-if="isSubmitting">저장 중...</span>
            <span v-else>저장하기</span>
          </button>
        </div>
      </form>
    </div>

    <!-- 성공 토스트 메시지 -->
    <div v-if="showSuccessToast" class="success-toast">
      프로필이 성공적으로 저장되었습니다!
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const route = useRoute();

// API 기본 설정
const API_BASE_URL = '/api';
const token = localStorage.getItem('jwt');
const authHeader = computed(() => {
  return token ? { 'Authorization': `Bearer ${token}` } : {};
});

const isLoading = ref(true);
const isSubmitting = ref(false);
const error = ref(null);
const showSuccessToast = ref(false);
const validationErrors = ref({});
const currentUserId = ref(null);
const showPassword = ref(false);
const showPasswordConfirm = ref(false);

const userProfile = ref({
  id: null,
  nickname: '',
  password: '',
  passwordConfirm: '',
  gender: '',
  age: null,
  height: null,
  weight: null,
  activityLevel: '',
  activityGoal: ''
});

const activityLevelOptions = [
  {
    value: '느긋한 코알라',
    name: '느긋한 코알라',
    icon: '🐨',
    description: '운동을 시작하려고 하는 단계'
  },
  {
    value: '산책하는 거북이',
    name: '산책하는 거북이',
    icon: '🐢',
    description: '가끔 가벼운 운동을 하는 수준'
  },
  {
    value: '신나는 강아지',
    name: '신나는 강아지',
    icon: '🐶',
    description: '규칙적으로 운동하는 습관이 있는 수준'
  },
  {
    value: '힘찬 질주 말',
    name: '힘찬 질주 말',
    icon: '🐎',
    description: '적극적으로 운동하며 체력이 좋은 수준'
  },
  {
    value: '전광석화 치타',
    name: '전광석화 치타',
    icon: '🐆',
    description: '고강도 운동을 즐기는 운동 마니아 수준'
  }
];

const activityGoalOptions = [
  {
    value: '느긋한 코알라',
    name: '느긋한 코알라',
    icon: '🐨',
    description: '주 1회 가벼운 산책이나 조깅'
  },
  {
    value: '산책하는 거북이',
    name: '산책하는 거북이',
    icon: '🐢',
    description: '주 1-2회 가벼운 조깅'
  },
  {
    value: '신나는 강아지',
    name: '신나는 강아지',
    icon: '🐶',
    description: '주 3-4회 규칙적인 조깅'
  },
  {
    value: '힘찬 질주 말',
    name: '힘찬 질주 말',
    icon: '🐎',
    description: '주 5회 이상 적극적인 조깅'
  },
  {
    value: '전광석화 치타',
    name: '전광석화 치타',
    icon: '🐆',
    description: '마라톤 준비 및 고강도 트레이닝'
  }
];

const isFormValid = computed(() => {
  const hasValidNickname = userProfile.value.nickname &&
    userProfile.value.nickname.trim().length > 0 &&
    userProfile.value.nickname.length <= 10;

  const hasValidAge = !userProfile.value.age ||
    (userProfile.value.age >= 1 && userProfile.value.age <= 120);

  const hasValidPassword = !userProfile.value.password ||
    !userProfile.value.password.trim() ||
    (userProfile.value.password.length >= 6 && userProfile.value.password.length <= 20);

  const hasValidPasswordConfirm = !userProfile.value.password ||
    !userProfile.value.password.trim() ||
    userProfile.value.password === userProfile.value.passwordConfirm;

  const hasNoErrors = Object.keys(validationErrors.value).length === 0;

  return hasValidNickname && hasValidAge && hasValidPassword && hasNoErrors && hasValidPasswordConfirm;
});

// 현재 사용자 정보 가져오기
const getCurrentUser = async () => {
  if (!token) {
    throw new Error('로그인이 필요합니다.');
  }

  try {
    const response = await axios.get(`${API_BASE_URL}/users/me`, {
      headers: authHeader.value
    });

    const userData = response.data.user;
    currentUserId.value = userData.id;

    return userData;
  } catch (err) {
    console.error('현재 사용자 정보 조회 실패:', err);

    if (err.response && (err.response.status === 401 || err.response.status === 403)) {
      localStorage.removeItem('jwt');
      router.push('/login');
    }

    throw err;
  }
};

// 프로필 정보 가져오기
const fetchProfile = async () => {
  try {
    isLoading.value = true;
    error.value = null;

    if (!token) {
      error.value = '로그인이 필요합니다.';
      router.push('/login');
      return;
    }

    await getCurrentUser();

    // 본인 프로필만 수정 가능
    const response = await axios.get(`${API_BASE_URL}/users/${currentUserId.value}/profile`, {
      headers: authHeader.value
    });

    const data = response.data;
    let genderValue = '';
    if (data.gender === '남성') {
      genderValue = 'MALE';
    } else if (data.gender === '여성') {
      genderValue = 'FEMALE';
    } else {
      genderValue = data.gender; // 이미 MALE/FEMALE인 경우
    }

    // 강제로 새로운 객체 생성하여 반응성 보장
    userProfile.value = {
      id: data.id,
      nickname: data.nickname || '',
      password: '',
      passwordConfirm: '',
      age: data.age || null,
      gender: genderValue,
      height: data.height || null,
      weight: data.weight || null,
      activityLevel: data.activityLevel || '',
      activityGoal: data.activityGoal || ''
    };
    await nextTick();
  } catch (err) {
    console.error('프로필 불러오기 실패:', err);
    error.value = err.response?.data?.message || '프로필을 불러오는데 실패했습니다.';

    if (err.response?.status === 401) {
      localStorage.removeItem('jwt');
      router.push('/login');
    }
  } finally {
    isLoading.value = false;
  }
};

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};

const togglePasswordConfirmVisibility = () => {
  showPasswordConfirm.value = !showPasswordConfirm.value;
};

const goBack = () => {
  router.push('/profile');
};

const saveProfile = async () => {
  if (!validateForm()) {
    console.log('Form validation failed:', validationErrors.value);
    return;
  }

  try {
    isSubmitting.value = true;

    let genderForServer = userProfile.value.gender;
    if (userProfile.value.gender === 'MALE') {
      genderForServer = '남성';
    } else if (userProfile.value.gender === 'FEMALE') {
      genderForServer = '여성';
    }

    const updateData = {
      id: currentUserId.value,
      nickname: userProfile.value.nickname,
      gender: genderForServer, // 서버 형식에 맞게 전송
      age: userProfile.value.age,
      height: userProfile.value.height,
      weight: userProfile.value.weight,
      activityLevel: userProfile.value.activityLevel,
      activityGoal: userProfile.value.activityGoal
    };

    // 비밀번호 변경이 있을 때만 포함
    if (userProfile.value.password && userProfile.value.password.trim()) {
      updateData.password = userProfile.value.password;
    }

    const response = await axios.put(`${API_BASE_URL}/users/${currentUserId.value}/profile`, updateData, {
      headers: {
        ...authHeader.value,
        'Content-Type': 'application/json'
      }
    });

    if (response.data.token) {
      localStorage.setItem('jwt', response.data.token);
    }

    try {
      const checkResponse = await axios.get(`${API_BASE_URL}/users/${currentUserId.value}/profile`, {
        headers: response.data.token ? { 'Authorization': `Bearer ${response.data.token}` } : authHeader.value
      });
      const serverData = checkResponse.data;
      let genderValue = '';
      if (serverData.gender === '남성') {
        genderValue = 'MALE';
      } else if (serverData.gender === '여성') {
        genderValue = 'FEMALE';
      } else {
        genderValue = serverData.gender;
      }

      userProfile.value = {
        id: serverData.id,
        nickname: serverData.nickname || '',
        password: '',
        passwordConfirm: '',
        age: serverData.age || null,
        gender: genderValue,
        height: serverData.height || null,
        weight: serverData.weight || null,
        activityLevel: serverData.activityLevel || '',
        activityGoal: serverData.activityGoal || ''
      };

    } catch (checkErr) {
      console.error('업데이트 후 프로필 조회 실패:', checkErr);
    }

    showSuccessToast.value = true;

    setTimeout(() => {
      showSuccessToast.value = false;
      router.push('/profile');
    }, 2000);

  } catch (err) {

    let errorMessage = '프로필 저장에 실패했습니다.';

    if (err.response?.data?.message) {
      errorMessage = err.response.data.message;
    } else if (err.response?.status === 400) {
      errorMessage = '입력 정보를 다시 확인해주세요.';
    } else if (err.response?.status === 401) {
      errorMessage = '로그인이 만료되었습니다. 다시 로그인해 주세요.';
      localStorage.removeItem('jwt');
      router.push('/login');
      return;
    }

    alert(errorMessage);
  } finally {
    isSubmitting.value = false;
  }
};

const validateForm = () => {
  const newErrors = {};

  // 닉네임 검증
  if (!userProfile.value.nickname || userProfile.value.nickname.trim().length === 0) {
    newErrors.nickname = '닉네임을 입력해주세요.';
  } else if (userProfile.value.nickname.length > 10) {
    newErrors.nickname = '닉네임은 10자 이하로 입력해주세요.';
  }

  // 나이 검증
  if (userProfile.value.age && (userProfile.value.age < 1 || userProfile.value.age > 120)) {
    newErrors.age = '올바른 나이를 입력해주세요.';
  }

  // 비밀번호 검증
  if (userProfile.value.password && userProfile.value.password.trim()) {
    if (userProfile.value.password.length < 6) {
      newErrors.password = '비밀번호는 6자 이상이어야 합니다.';
    } else if (userProfile.value.password.length > 20) {
      newErrors.password = '비밀번호는 20자 이하여야 합니다.';
    }

    // 비밀번호 확인 검증
    if (userProfile.value.password !== userProfile.value.passwordConfirm) {
      newErrors.passwordConfirm = '비밀번호가 일치하지 않습니다.';
    }
  }

  validationErrors.value = newErrors;
  return Object.keys(newErrors).length === 0;
};

const validateField = (fieldName) => {
  const newErrors = { ...validationErrors.value };

  if (fieldName === 'nickname') {
    delete newErrors.nickname;
    if (!userProfile.value.nickname || userProfile.value.nickname.trim().length === 0) {
      newErrors.nickname = '닉네임을 입력해주세요.';
    } else if (userProfile.value.nickname.length > 10) {
      newErrors.nickname = '닉네임은 10자 이하로 입력해주세요.';
    }
  }

  if (fieldName === 'age') {
    delete newErrors.age;
    if (userProfile.value.age && (userProfile.value.age < 1 || userProfile.value.age > 120)) {
      newErrors.age = '올바른 나이를 입력해주세요.';
    }
  }

  if (fieldName === 'password') {
    delete newErrors.password;
    delete newErrors.passwordConfirm;

    if (userProfile.value.password && userProfile.value.password.trim()) {
      if (userProfile.value.password.length < 6) {
        newErrors.password = '비밀번호는 6자 이상이어야 합니다.';
      } else if (userProfile.value.password.length > 20) {
        newErrors.password = '비밀번호는 20자 이하여야 합니다.';
      }

      // 비밀번호 확인도 같이 검증
      if (userProfile.value.passwordConfirm && userProfile.value.password !== userProfile.value.passwordConfirm) {
        newErrors.passwordConfirm = '비밀번호가 일치하지 않습니다.';
      }
    } else {
      // 비밀번호가 비어있으면 확인도 초기화
      userProfile.value.passwordConfirm = '';
    }
  }

  if (fieldName === 'passwordConfirm') {
    delete newErrors.passwordConfirm;

    if (userProfile.value.password && userProfile.value.password.trim()) {
      if (userProfile.value.password !== userProfile.value.passwordConfirm) {
        newErrors.passwordConfirm = '비밀번호가 일치하지 않습니다.';
      }
    }
  }

  validationErrors.value = newErrors;
};

const selectActivityLevel = (activityValue) => {
  userProfile.value.activityLevel = activityValue;
};

const selectActivityGoal = (activityValue) => {
  userProfile.value.activityGoal = activityValue;
};

onMounted(async () => {
  await fetchProfile();
});
</script>

<style scoped>
.profile-edit-container {
  background-color: #FFF8F2;
  min-height: 100vh;
  padding-bottom: 40px;
  font-family: 'Noto Sans KR', sans-serif;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  position: sticky;
  top: 0;
  background-color: #FFF8F2;
  z-index: 10;
}

.back-button {
  background: none;
  border: none;
  cursor: pointer;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 18px;
  font-weight: bold;
}

.back-button:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  text-align: center;
  flex-grow: 1;
  margin: 0;
}

.spacer {
  width: 40px;
}

/* 로딩 및 에러 상태 스타일 */
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #FF7E36;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
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
  color: #d32f2f;
  margin-bottom: 16px;
}

.retry-button {
  background-color: #FF7E36;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  cursor: pointer;
}

.profile-form-card {
  background-color: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin: 0 16px;
  overflow: hidden;
}

.profile-form {
  padding: 20px 16px;
}

.form-group {
  margin-bottom: 20px;
  position: relative;
}

.form-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.form-group.half {
  flex: 1;
}

label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #555;
}

.form-input,
.form-select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-input:focus,
.form-select:focus {
  border-color: #FF7E36;
}

.form-input.error,
.form-select.error {
  border-color: #d32f2f;
}

.input-counter {
  position: absolute;
  right: 8px;
  bottom: 8px;
  font-size: 12px;
  color: #888;
}

.error-text {
  color: #d32f2f;
  font-size: 12px;
  margin-top: 4px;
}

/* 성별 선택 스타일 */
.gender-selector {
  display: flex;
  gap: 0.75rem;
  width: 100%;
}

.gender-option {
  flex: 1;
  position: relative;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  padding: 0.75rem 0.5rem;
  border: 2px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  min-height: 50px;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.gender-option:hover {
  border-color: #FFB8A3;
}

.gender-option.selected {
  border-color: #FF7E36;
  background-color: rgba(255, 126, 54, 0.05);
}

.gender-option input {
  position: absolute;
  opacity: 0;
}

.gender-icon {
  margin-right: 0.5rem;
  font-size: 1.2rem;
  line-height: 1;
}

.gender-icon.male::before {
  content: "👨";
}

.gender-icon.female::before {
  content: "👩";
}

.activity-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-option {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.activity-option.active {
  border-color: #FF7E36;
  background-color: #FFF3ED;
}

.activity-icon {
  font-size: 24px;
  margin-right: 16px;
}

.activity-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.activity-desc {
  font-size: 13px;
  color: #777;
}

.save-button-container {
  margin-top: 32px;
}

.save-button {
  width: 100%;
  background-color: #FF7E36;
  color: white;
  border: none;
  border-radius: 12px;
  padding: 16px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.save-button:hover:not(:disabled) {
  background-color: #e66a1e;
}

.save-button:disabled {
  background-color: #FFB8A3;
  cursor: not-allowed;
}

.password-input-container {
  position: relative;
}

.password-input {
  padding-right: 48px !important;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: color 0.2s;
}

.password-toggle:hover {
  color: #FF7E36;
}

.success-toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: #4CAF50;
  color: white;
  padding: 16px 24px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  font-weight: 500;
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.9);
  }

  to {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}

@media (max-width: 480px) {
  .form-row {
    flex-direction: column;
    gap: 16px;
  }

  .form-group.half {
    margin-bottom: 0;
  }

  .gender-selector {
    gap: 0.5rem;
  }

  .gender-option {
    padding: 0.6rem 0.4rem;
    font-size: 13px;
    min-height: 45px;
  }

  .gender-icon {
    font-size: 1.1rem;
    margin-right: 0.4rem;
  }
}
</style>