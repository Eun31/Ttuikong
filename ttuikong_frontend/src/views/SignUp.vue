
<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <img :src="logo" class="logo" alt="logo" />
        <h2>회원가입</h2>
      </div>
      <form class="auth-form" @submit.prevent="goToNextStep">
        <div class="form-group">
          <label>이메일</label>
          <input 
            type="email" 
            v-model="formData.email" 
            required 
            placeholder="이메일을 입력하세요"
          >
        </div>
        <div class="form-group">
          <label>비밀번호</label>
          <input 
            type="password" 
            v-model="formData.password" 
            required 
            placeholder="비밀번호를 입력하세요"
          >
        </div>
        <div class="form-group">
          <label>성별</label>
          <div class="radio-group">
            <label class="radio-label">
              <input type="radio" v-model="formData.gender" value="남성" required>
              <span>남성</span>
            </label>
            <label class="radio-label">
              <input type="radio" v-model="formData.gender" value="여성">
              <span>여성</span>
            </label>
          </div>
        </div>
        <div class="form-group">
          <label>닉네임</label>
          <input 
            type="text" 
            v-model="formData.nickname" 
            required 
            placeholder="닉네임을 입력하세요"
          >
        </div>
        <div class="form-group">
          <label>나이</label>
          <input 
            type="number" 
            v-model="formData.age" 
            required 
            placeholder="나이를 입력하세요"
          >
        </div>
        <div class="form-group">
          <label>키 (cm)</label>
          <input 
            type="number" 
            v-model="formData.height" 
            required 
            placeholder="키를 입력하세요"
          >
        </div>
        <div class="form-group">
          <label>몸무게 (kg)</label>
          <input 
            type="number" 
            v-model="formData.weight" 
            required 
            placeholder="몸무게를 입력하세요"
          >
        </div>
        <div class="form-buttons">
          <router-link to="/login" class="btn btn-secondary">취소</router-link>
          <button type="submit" class="btn btn-primary">다음</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
// 이미지 import 방식
import logoSrc from '../assets/logo_1.png'

export default {
  name: 'SignUp',
  data() {
    return {
      logo: logoSrc, // 로고를 별도의 데이터 속성으로 정의
      formData: {
        email: '',
        password: '',
        gender: '',
        nickname: '',
        age: null,
        height: null,
        weight: null
      }
    }
  },
  created() {
    // formData 객체를 생성하고 초기화
    if (!this.formData) {
      this.formData = {
        email: '',
        password: '',
        gender: '',
        nickname: '',
        age: null,
        height: null,
        weight: null
      };
    }
    
    // 이전에 입력한 데이터가 있는지 확인
    const savedData = localStorage.getItem('signupData');
    if (savedData) {
      try {
        const parsedData = JSON.parse(savedData);
        // 기존 formData에 저장된 데이터 병합
        this.formData = { ...this.formData, ...parsedData };
      } catch (error) {
        console.error('Error parsing saved data:', error);
      }
    }
  },
  methods: {
    goToNextStep() {
      // 폼 검증 (HTML5 기본 검증을 사용하므로 추가 코드 불필요)
      
      // formData를 localStorage에 저장
      localStorage.setItem('signupData', JSON.stringify(this.formData))
      
      // 다음 단계로 이동
      this.$router.push('/signup2')
    }
  }
}
</script>
