<!-- SignUp2.vue -->
<template>
  <div class="signup-container">
    <div class="signup-content">
      <div class="signup-header">
        <img :src="logo" alt="RunBuddy Logo" class="logo">
        <div class="progress-bar">
          <div class="progress-step completed"></div>
          <div class="progress-line active"></div>
          <div class="progress-step active"></div>
          <div class="progress-line"></div>
          <div class="progress-step"></div>
        </div>
        <p class="step-title">운동 스타일 (2/3)</p>
      </div>

      <div class="signup-form exercise-style">
        <h2 class="form-question">당신의 평소 운동 스타일은 어떤가요?</h2>
        <p class="form-subtext">가장 본인과 비슷한 운동 패턴을 선택해주세요</p>
        
        <div class="style-selection">
          <div class="style-option" 
              v-for="(style, index) in exerciseStyles" 
              :key="index"
              :class="{ 'selected': activityLevel === style.value }"
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

        <div class="form-actions">
          <button class="btn btn-outline" @click="goToPrevStep">
            <i class="icon-arrow-left"></i>
            이전 단계
          </button>
          <button class="btn btn-primary" @click="goToNextStep">
            다음 단계
            <i class="icon-arrow-right"></i>
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
const activityLevel = ref('🐨 느긋한 코알라');
const formData = ref({});

const exerciseStyles = [
  {
    emoji: '🐨',
    value: '🐨 느긋한 코알라',
    title: '느긋한 코알라',
    description: '편안하게 운동을 즐기며, 부담 없이 나만의 페이스로 달려요.'
  },
  {
    emoji: '🐢',
    value: '🐢 산책하는 거북이',
    title: '산책하는 거북이',
    description: '꾸준함이 장점이에요. 천천히 가더라도 멈추지 않아요.'
  },
  {
    emoji: '🐶',
    value: '🐶 신나는 강아지',
    title: '신나는 강아지',
    description: '에너지가 넘치고 운동을 통해 스트레스를 해소해요.'
  },
  {
    emoji: '🐎',
    value: '🐎 힘찬 질주 말',
    title: '힘찬 질주 말',
    description: '체계적인 운동으로 나의 한계를 뛰어넘고 싶어요.'
  },
  {
    emoji: '🐆',
    value: '🐆 전광석화 치타',
    title: '전광석화 치타',
    description: '빠른 속도와 강도 높은 운동으로 퍼포먼스를 높여요.'
  }
];

function selectStyle(style) {
  activityLevel.value = style;
}

function goToPrevStep() {
  router.push('/signup');
}

function goToNextStep() {
  // 기존 데이터 불러오기
  formData.value = JSON.parse(localStorage.getItem('signupData') || '{}');
  
  // 현재 선택 추가
  formData.value.activityLevel = activityLevel.value;
  
  // 업데이트된 데이터 저장
  localStorage.setItem('signupData', JSON.stringify(formData.value));
  
  // 다음 단계로 이동
  router.push('/signup3');
}

onMounted(() => {
  // 기존 데이터 불러오기
  const savedData = localStorage.getItem('signupData');
  if (savedData) {
    formData.value = JSON.parse(savedData);
    
    // 이전에 선택한 스타일이 있으면 불러오기
    if (formData.value.activityLevel) {
      activityLevel.value = formData.value.activityLevel;
    }
  } else {
    // 이전 단계 데이터가 없으면 회원가입 첫 페이지로 돌아가기
    router.push('/signup');
  }
});
</script>

<style src="../assets/css/auth.css"></style>