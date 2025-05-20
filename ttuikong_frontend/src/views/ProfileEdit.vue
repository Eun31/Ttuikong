<template>
  <div class="profile-edit-container">
    <div class="page-header">
      <button class="back-button" @click="goBack">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M15 18L9 12L15 6" stroke="#333333" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
      </button>
      <h1 class="page-title">프로필 수정</h1>
      <div class="spacer"></div>
    </div>

    <div class="profile-form-card">
      <!-- 프로필 이미지 수정 -->
      <div class="profile-image-section">
        <div class="profile-image-container">
          <img :src="profileImage || defaultProfileImage" alt="프로필 이미지" class="profile-image">
          <div class="profile-level">Lv.{{ userProfile.level }}</div>
        </div>
        <button class="edit-image-btn" @click="triggerImageUpload">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M12 15.5H7.5C6.10444 15.5 5.40665 15.5 4.83886 15.6722C3.56045 16.06 2.56004 17.0605 2.17224 18.3389C2 18.9067 2 19.6044 2 21M19 21V15M16 18H22M14.5 7.5C14.5 9.98528 12.4853 12 10 12C7.51472 12 5.5 9.98528 5.5 7.5C5.5 5.01472 7.51472 3 10 3C12.4853 3 14.5 5.01472 14.5 7.5Z"
              stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          사진 변경
        </button>
        <input type="file" ref="fileInput" @change="handleImageUpload" accept="image/*" style="display: none;">
      </div>

      <!-- 프로필 폼 -->
      <form @submit.prevent="saveProfile" class="profile-form">
        <div class="form-group">
          <label for="nickname">닉네임</label>
          <input type="text" id="nickname" v-model="userProfile.nickname" class="form-input" placeholder="닉네임을 입력하세요"
            maxlength="10">
          <div class="input-counter">{{ userProfile.nickname.length }}/10</div>
        </div>

        <div class="form-group">
          <label for="description">한줄 소개</label>
          <input type="text" id="description" v-model="userProfile.description" class="form-input"
            placeholder="프로필 소개를 입력하세요" maxlength="30">
          <div class="input-counter">{{ userProfile.description.length }}/30</div>
        </div>

        <div class="form-row">
          <div class="form-group half">
            <label for="age">나이</label>
            <input type="number" id="age" v-model="userProfile.age" class="form-input" placeholder="나이" min="1"
              max="120">
          </div>
          <div class="form-group half">
            <label for="gender">성별</label>
            <select id="gender" v-model="userProfile.gender" class="form-select">
              <option value="">선택 안함</option>
              <option value="남성">남성</option>
              <option value="여성">여성</option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group half">
            <label for="height">키 (cm)</label>
            <input type="number" id="height" v-model="userProfile.height" class="form-input" placeholder="키" min="100"
              max="250" step="0.1">
          </div>
          <div class="form-group half">
            <label for="weight">몸무게 (kg)</label>
            <input type="number" id="weight" v-model="userProfile.weight" class="form-input" placeholder="몸무게" min="30"
              max="200" step="0.1">
          </div>
        </div>

        <div class="form-group">
          <label>목표 활동성</label>
          <div class="activity-options">
            <div v-for="(activity, index) in activityOptions" :key="index"
              :class="['activity-option', { active: userProfile.activityGoal === activity.value }]"
              @click="selectActivity(activity.value)">
              <div class="activity-icon">{{ activity.icon }}</div>
              <div class="activity-info">
                <div class="activity-name">{{ activity.name }}</div>
                <div class="activity-desc">{{ activity.description }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="save-button-container">
          <button type="submit" class="save-button" :disabled="isSubmitting">
            <span v-if="isSubmitting">저장 중...</span>
            <span v-else>저장하기</span>
          </button>
        </div>
      </form>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import defaultProfileImg from '@/assets/profile.png';

const router = useRouter();

const defaultProfileImage = defaultProfileImg;
const profileImage = ref(null);
const isSubmitting = ref(false);
const fileInput = ref(null);

const userProfile = ref({
  id: 1,
  nickname: '러닝마니아',
  description: '신나는 강아지',
  age: 28,
  gender: '남성',
  height: 175.0,
  weight: 68.5,
  level: 5,
  activityGoal: '힘찬 질주 말',
  totalDistance: 324.8,
  avgDistance: 5.2
});

const activityOptions = [
  {
    value: '느긋한 거북이',
    name: '느긋한 거북이',
    icon: '🐢',
    description: '주 1-2회 가벼운 조깅'
  },
  {
    value: '신나는 강아지',
    name: '신나는 강아지',
    icon: '🐶',
    description: '주 3-4회 조깅'
  },
  {
    value: '힘찬 질주 말',
    name: '힘찬 질주 말',
    icon: '🐎',
    description: '주 5회 이상 조깅'
  },
  {
    value: '전광석화 치타',
    name: '전광석화 치타',
    icon: '🐆',
    description: '마라톤 준비 중'
  }
];

const goBack = () => {
  router.push('/profile');
};

const triggerImageUpload = () => {
  fileInput.value?.click();
};

const handleImageUpload = (event) => {
  const file = event.target.files[0];
  if (file && file.type.match('image.*')) {
    const reader = new FileReader();
    reader.onload = (e) => {
      profileImage.value = e.target.result;
    };
    reader.readAsDataURL(file);
  }
};

const selectActivity = (activityValue) => {
  userProfile.value.activityGoal = activityValue;
};

const saveProfile = () => {
  isSubmitting.value = true;

  setTimeout(() => {
    isSubmitting.value = false;
    alert('프로필이 성공적으로 업데이트되었습니다!');
    router.push('/profile');
  }, 1000);
};
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

.profile-form-card {
  background-color: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin: 0 16px;
  overflow: hidden;
}

.profile-image-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 16px;
  background-color: #FFF3ED;
  position: relative;
}

.profile-image-container {
  position: relative;
  margin-bottom: 16px;
}

.profile-image {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #FF7E36;
}

.profile-level {
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

.edit-image-btn {
  background-color: #FF7E36;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-image-btn:hover {
  background-color: #e66a1e;
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
}

.form-input:focus,
.form-select:focus {
  border-color: #FF7E36;
}

.input-counter {
  position: absolute;
  right: 8px;
  bottom: 8px;
  font-size: 12px;
  color: #888;
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

@media (max-width: 480px) {
  .form-row {
    flex-direction: column;
    gap: 16px;
  }

  .form-group.half {
    margin-bottom: 0;
  }
}
</style>