<!-- SignUp3.vue -->
<template>
  <div class="signup-container">
    <div class="signup-content">
      <div class="signup-header">
        <img :src="logo" alt="RunBuddy Logo" class="logo">
        <div class="progress-bar">
          <div class="progress-step completed"></div>
          <div class="progress-line completed"></div>
          <div class="progress-step completed"></div>
          <div class="progress-line active"></div>
          <div class="progress-step active"></div>
        </div>
        <p class="step-title">목표 설정 (3/3)</p>
      </div>

      <div class="signup-form goal-setting">
        <h2 class="form-question">당신은 어떤 러너가 되고 싶나요?</h2>
        <p class="form-subtext">앞으로의 러닝 목표를 선택해주세요</p>
        
        <div class="style-selection">
          <div class="style-option" 
               v-for="(style, index) in goalStyles" 
               :key="index"
               :class="{ 'selected': activityGoal === style.value }"
               @click="selectStyle(style.value)">
            <div class="style-icon">
              <span class="emoji">{{ style.emoji }}</span>
            </div>
            <div class="style-info">
              <h3>{{ style.title }}</h3>
              <p>{{ style.description }}</p>
            </div>
          </div>
        </div>
        
        <div class="extra-settings">
          <h3>추가 선택 사항</h3>
          <div class="checkbox-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="formData.notifications">
              <span class="checkmark"></span>
              <span>러닝 알림을 받을래요</span>
            </label>
            
            <label class="checkbox-label">
              <input type="checkbox" v-model="formData.groupActivities">
              <span class="checkmark"></span>
              <span>그룹 활동에 참여하고 싶어요</span>
            </label>
            
            <label class="checkbox-label">
              <input type="checkbox" v-model="formData.acceptTerms" required>
              <span class="checkmark"></span>
              <span>
                <a href="#" @click.prevent="showTerms">이용약관</a>과 
                <a href="#" @click.prevent="showPrivacy">개인정보 처리방침</a>에 동의합니다
              </span>
            </label>
          </div>
        </div>

        <div class="form-actions">
          <button class="btn btn-outline" @click="goToPrevStep">
            <i class="icon-arrow-left"></i>
            이전 단계
          </button>
          <button class="btn btn-success" @click="submitForm" :disabled="!formData.acceptTerms">
            가입 완료
            <i class="icon-check"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import logoSrc from '../assets/logo_orange.png';

const router = useRouter();
const logo = ref(logoSrc);
const activityGoal = ref('🐨 느긋한 코알라');
const formData = ref({
  notifications: true,
  groupActivities: false,
  acceptTerms: false
});

const goalStyles = [
  {
    emoji: '🐨',
    value: '🐨 느긋한 코알라',
    title: '편안한 생활 러너',
    description: '즐겁게 달리며 일상에 활력을 더하고 싶어요.'
  },
  {
    emoji: '🐢',
    value: '🐢 산책하는 거북이',
    title: '건강한 습관 러너',
    description: '꾸준한 러닝으로 건강한 습관을 만들고 싶어요.'
  },
  {
    emoji: '🐶',
    value: '🐶 신나는 강아지',
    title: '활력 넘치는 러너',
    description: '러닝으로 스트레스를 해소하고 에너지를 충전하고 싶어요.'
  },
  {
    emoji: '🐎',
    value: '🐎 힘찬 질주 말',
    title: '도전하는 러너',
    description: '5K, 10K, 하프 마라톤 등 나만의 목표를 달성하고 싶어요.'
  },
  {
    emoji: '🐆',
    value: '🐆 전광석화 치타',
    title: '퍼포먼스 러너',
    description: '내 기록을 경신하고 마라톤 완주를 목표로 해요.'
  }
];

function selectStyle(style) {
  activityGoal.value = style;
}

function goToPrevStep() {
  router.push('/signup2');
}

function showTerms() {
  alert('이용약관 내용이 표시됩니다.');
}

function showPrivacy() {
  alert('개인정보 처리방침 내용이 표시됩니다.');
}

function submitForm() {
  if (!formData.value.acceptTerms) {
    alert('이용약관과 개인정보 처리방침에 동의해주세요.');
    return;
  }
  
  // 기존 데이터 불러오기
  const savedData = JSON.parse(localStorage.getItem('signupData') || '{}');
  
  // 현재 선택과 추가 데이터 병합
  const completeData = { 
    ...savedData, 
    activityGoal: activityGoal.value,
    notifications: formData.value.notifications,
    groupActivities: formData.value.groupActivities
  };
  
  // API 호출 (실제 구현에서는 여기에 회원가입 API 호출 코드가 들어갑니다)
  console.log('회원가입 완료:', completeData);
  
  // 가입 완료 후 로그인 페이지로 리다이렉트
  router.push('/login');
  
  // 임시 저장 데이터 삭제
  localStorage.removeItem('signupData');
}

onMounted(() => {
  // 기존 데이터 불러오기
  const savedData = localStorage.getItem('signupData');
  if (savedData) {
    const parsedData = JSON.parse(savedData);
    
    // 이전에 선택한 스타일이 있으면 불러오기
    if (parsedData.activityGoal) {
      activityGoal.value = parsedData.activityGoal;
    }
    
    // 현재 단계의 폼 데이터와 합치기
    if (parsedData.notifications !== undefined) {
      formData.value.notifications = parsedData.notifications;
    }
    if (parsedData.groupActivities !== undefined) {
      formData.value.groupActivities = parsedData.groupActivities;
    }
    if (parsedData.acceptTerms !== undefined) {
      formData.value.acceptTerms = parsedData.acceptTerms;
    }
  } else {
    // 이전 단계 데이터가 없으면 회원가입 첫 페이지로 돌아가기
    router.push('/signup');
  }
});
</script>

<style src="../assets/css/auth.css"></style>