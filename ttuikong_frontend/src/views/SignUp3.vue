<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <img :src="logo" class="logo" alt="logo">
        <h2>회원가입</h2>
      </div>
      <div class="auth-form">
        <div class="form-question">
          <h3>네가 되고 싶은 모습은 어떤 거야?</h3>
          <select v-model="desiredStyle" class="style-select">
            <option>🐨 느긋한 코알라</option>
            <option>🐢 산책하는 거북이</option>
            <option>🐶 신나는 강아지</option>
            <option>🐎 힘찬 질주 말</option>
            <option>🐆 전광석화 치타</option>
          </select>
        </div>
        <div class="form-buttons">
          <button class="btn btn-secondary" @click="goToPrevStep">이전</button>
          <button class="btn btn-primary" @click="submitForm">제출</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import logoSrc from '@/assets/logo_1.png'

export default {
  name: 'SignUp3',
  data() {
    return {
      logo: logoSrc,
      desiredStyle: '🐨 느긋한 코알라',
      formData: {}
    }
  },
  methods: {
    goToPrevStep() {
      this.$router.push('/signup2')
    },
    submitForm() {
      // 기존 데이터 불러오기
      this.formData = JSON.parse(localStorage.getItem('signupData') || '{}')
      
      // 현재 선택 추가
      this.formData.desiredStyle = this.desiredStyle
      
      // 회원가입 API 호출 (예시)
      console.log('회원가입 완료:', this.formData)
      
      // 실제 API 연동 후 아래 코드로 대체
      localStorage.setItem('isLoggedIn', 'true')
      localStorage.setItem('userEmail', this.formData.email)
      
      // 회원가입 데이터 삭제 (이미 서버로 전송됨)
      localStorage.removeItem('signupData')
      
      // 로그인 페이지로 이동
      this.$router.push('/login')
    }
  },
  created() {
    // 기존 데이터 불러오기
    const savedData = localStorage.getItem('signupData')
    if (savedData) {
      this.formData = JSON.parse(savedData)
      
      // 이전에 선택한 스타일이 있으면 불러오기
      if (this.formData.desiredStyle) {
        this.desiredStyle = this.formData.desiredStyle
      }
    } else {
      // 이전 단계 데이터가 없으면 회원가입 첫 페이지로 돌아가기
      this.$router.push('/signup')
    }
  }
}
</script>