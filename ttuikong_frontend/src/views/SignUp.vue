<!-- SignUp.vue -->
<template>
  <div class="signup-container">
    <div class="signup-content">
      <div class="signup-header">
        <img :src="logo" alt="RunBuddy Logo" class="logo">
        <div class="progress-bar">
          <div class="progress-step active"></div>
          <div class="progress-line"></div>
          <div class="progress-step"></div>
          <div class="progress-line"></div>
          <div class="progress-step"></div>
        </div>
        <p class="step-title">기본 정보 입력 (1/3)</p>
      </div>

      <form @submit.prevent="goToNextStep" class="signup-form">
        <div class="form-row">
          <div class="form-group">
            <label for="email">이메일</label>
            <div class="input-wrapper">
              <i class="icon-email"></i>
              <input 
                id="email"
                type="email" 
                v-model="formData.email" 
                required 
                placeholder="이메일을 입력하세요"
              >
            </div>
          </div>
          
          <div class="form-group">
            <label for="password">비밀번호</label>
            <div class="input-wrapper">
              <i class="icon-lock"></i>
              <input 
                id="password"
                type="password" 
                v-model="formData.password" 
                minlength="6"
                required 
                placeholder="비밀번호를 입력하세요"
              >
            </div>
            <small class="form-hint">6자 이상</small>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="nickname">닉네임</label>
            <div class="input-wrapper">
              <i class="icon-user"></i>
              <input 
                id="nickname"
                type="text" 
                v-model="formData.nickname" 
                required 
                placeholder="닉네임을 입력하세요"
              >
            </div>
          </div>

          <div class="form-group">
            <label>성별</label>
            <div class="gender-selector">
              <label class="gender-option" :class="{ 'selected': formData.gender === '남성' }">
                <input type="radio" v-model="formData.gender" value="남성" required>
                <span class="gender-icon male"></span>
                <span>남성</span>
              </label>
              <label class="gender-option" :class="{ 'selected': formData.gender === '여성' }">
                <input type="radio" v-model="formData.gender" value="여성">
                <span class="gender-icon female"></span>
                <span>여성</span>
              </label>
            </div>
          </div>
        </div>

        <div class="form-row three-col">
          <div class="form-group">
            <label for="age">나이</label>
            <div class="input-wrapper">
              <input 
                id="age"
                type="number" 
                v-model="formData.age" 
                required 
                min="13"
                max="100"
                placeholder="나이"
              >
              <span class="input-suffix">세</span>
            </div>
          </div>

          <div class="form-group">
            <label for="height">키</label>
            <div class="input-wrapper">
              <input 
                id="height"
                type="number" 
                v-model="formData.height" 
                required 
                min="100"
                max="250"
                placeholder="키"
              >
              <span class="input-suffix">cm</span>
            </div>
          </div>

          <div class="form-group">
            <label for="weight">몸무게</label>
            <div class="input-wrapper">
              <input 
                id="weight"
                type="number" 
                v-model="formData.weight" 
                required 
                min="30"
                max="200"
                placeholder="몸무게"
              >
              <span class="input-suffix">kg</span>
            </div>
          </div>
        </div>

        <div class="form-actions">
          <router-link to="/login" class="btn btn-text">이미 계정이 있으신가요?</router-link>
          <div>
            <button type="submit" class="btn btn-primary">
              다음 단계
              <i class="icon-arrow-right"></i>
            </button>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import logoSrc from '../assets/logo_orange.png';

const router = useRouter();
const logo = ref(logoSrc);

const formData = ref({
  email: '',
  password: '',
  gender: '',
  nickname: '',
  age: null,
  height: null,
  weight: null
});

onMounted(() => {
  // 이전에 입력한 데이터가 있는지 확인
  const savedData = localStorage.getItem('signupData');
  if (savedData) {
    try {
      const parsedData = JSON.parse(savedData);
      // 기존 formData에 저장된 데이터 병합
      formData.value = { ...formData.value, ...parsedData };
    } catch (error) {
      console.error('Error parsing saved data:', error);
    }
  }
});

// 메서드
function goToNextStep() {
  // 폼 데이터 저장
  localStorage.setItem('signupData', JSON.stringify(formData.value));
  
  // 다음 단계로 이동
  router.push('/signup2');
}
</script>

<style src="../assets/css/auth.css"></style>