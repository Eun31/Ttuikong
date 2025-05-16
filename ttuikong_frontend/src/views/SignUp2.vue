
<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <img :src="logo" class="logo" alt="logo">
        <h2>회원가입</h2>
      </div>
      <div class="auth-form">
        <div class="form-question">
          <h3>너의 평소 운동 스타일을 알려줘!</h3>
          <select v-model="currentStyle" class="style-select">
            <option>🐨 느긋한 코알라</option>
            <option>🐢 산책하는 거북이</option>
            <option>🐶 신나는 강아지</option>
            <option>🐎 힘찬 질주 말</option>
            <option>🐆 전광석화 치타</option>
          </select>
        </div>
        <div class="form-buttons">
          <button class="btn btn-secondary" @click="goToPrevStep">이전</button>
          <button class="btn btn-primary" @click="goToNextStep">다음</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import logoSrc from '@/assets/logo_1.png'

export default {
  name: 'SignUp2',
  data() {
    return {
      logo: logoSrc, // 가져온 이미지를 data 속성으로 설정
      currentStyle: '🐨 느긋한 코알라',
      formData: {}
    }
  },
  methods: {
    goToPrevStep() {
      this.$router.push('/signup')
    },
    goToNextStep() {
      // 기존 데이터 불러오기
      this.formData = JSON.parse(localStorage.getItem('signupData') || '{}')
      
      // 현재 선택 추가
      this.formData.currentStyle = this.currentStyle
      
      // 업데이트된 데이터 저장
      localStorage.setItem('signupData', JSON.stringify(this.formData))
      
      // 다음 단계로 이동
      this.$router.push('/signup3')
    }
  },
  created() {
    // 기존 데이터 불러오기
    const savedData = localStorage.getItem('signupData')
    if (savedData) {
      this.formData = JSON.parse(savedData)
      
      // 이전에 선택한 스타일이 있으면 불러오기
      if (this.formData.currentStyle) {
        this.currentStyle = this.formData.currentStyle
      }
    } else {
      // 이전 단계 데이터가 없으면 회원가입 첫 페이지로 돌아가기
      this.$router.push('/signup')
    }
  }
}
</script>