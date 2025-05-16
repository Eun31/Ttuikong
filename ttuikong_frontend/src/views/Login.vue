<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <img :src="logo" class="logo" alt="logo" />
      </div>
      
      <div class="login-form">
        <div class="form-group">
          <label for="email">이메일</label>
          <div class="input-wrapper">
            <i class="ri-mail-line input-icon"></i>
            <input 
              type="email" 
              id="email" 
              v-model="email" 
              placeholder="이메일을 입력해주세요" 
            />
          </div>
        </div>
        
        <div class="form-group">
          <label for="password">비밀번호</label>
          <div class="input-wrapper">
            <i class="ri-lock-line input-icon"></i>
            <input 
              type="password" 
              id="password" 
              v-model="password" 
              placeholder="비밀번호를 입력해주세요" 
            />
          </div>
        </div>
        
        <button class="btn-login" @click="login">
          로그인 <i class="ri-arrow-right-line"></i>
        </button>
        
        <div class="auth-links">
          <router-link to="/forgot-password" class="forgot-link">
            <i class="ri-question-line"></i> 비밀번호를 잊으셨나요?
          </router-link>
          <router-link to="/signup" class="signup-link">
            <i class="ri-user-add-line"></i> 회원가입
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {

      logo: new URL('../assets/logo_orange.png', import.meta.url).href,
      email: '',
      password: ''
    }
  },
  methods: {
    login() {
      // 로그인 로직 구현
      console.log('로그인 시도:', this.email);
      
      // 이메일과 비밀번호가 비어있지 않은지 확인
      if (this.email && this.password) {
        // 임시 인증 토큰 저장
        localStorage.setItem('token', 'example-token-12345');
        // 홈 화면으로 리다이렉트
        this.$router.push('/');
      } else {
        alert('이메일과 비밀번호를 입력해주세요.');
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 2rem 1rem;
  background-color: #FFCDB2;
  position: relative;
}

.login-card {
  background: white;
  border-radius: 28px;
  box-shadow: 0 10px 30px rgba(255, 126, 71, 0.15);
  width: 100%;
  max-width: 400px;
  padding: 2.5rem 2rem;
  position: relative;
  z-index: 10;
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
  100% { transform: translateY(0px); }
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 2.5rem;
}

.logo {
  height: 90px;
  margin-bottom: 1rem;
  filter: drop-shadow(0 4px 6px rgba(255, 126, 71, 0.2));
  transition: transform 0.3s ease;
}

.logo:hover {
  transform: scale(1.05) rotate(-3deg);
}

.welcome-text {
  font-size: 1.6rem;
  color: #FF7E47;
  font-weight: 700;
  margin: 0.8rem 0 0;
  position: relative;
  display: inline-block;
}

.welcome-text::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #FFCDB2;
  border-radius: 2px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.4rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.95rem;
  font-weight: 600;
  color: #666;
  padding-left: 0.5rem;
  display: flex;
  align-items: center;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 15px;
  font-size: 1.2rem;
  color: #FF9D6C;
  z-index: 1;
  transition: color 0.3s ease;
}

.form-group input {
  padding: 0.9rem 1rem 0.9rem 3rem;
  border: 2px solid #FFE6D9;
  border-radius: 20px;
  font-size: 1rem;
  width: 100%;
  transition: all 0.3s ease;
  box-sizing: border-box;
  background-color: #FFFAF8;
}

.form-group input:focus {
  outline: none;
  border-color: #FFAB84;
  box-shadow: 0 0 0 4px rgba(255, 171, 132, 0.15);
  background-color: #fff;
}

.form-group input:focus + .input-icon,
.input-wrapper:hover .input-icon {
  color: #FF7E47;
}

.btn-login {
  padding: 1rem 1.5rem;
  background: linear-gradient(45deg, #FF7E47, #FF9D6C);
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 8px 15px rgba(255, 126, 71, 0.25);
}

.btn-login:hover {
  background: linear-gradient(45deg, #F26B31, #FF8E56);
  transform: translateY(-3px);
  box-shadow: 0 12px 20px rgba(255, 126, 71, 0.3);
}

.btn-login:active {
  transform: translateY(1px);
  box-shadow: 0 5px 10px rgba(255, 126, 71, 0.2);
}

.auth-links {
  display: flex;
  justify-content: space-between;
  margin-top: 1.8rem;
  font-size: 0.95rem;
}

.auth-links a {
  color: #FF7E47;
  text-decoration: none;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 4px;
}

.auth-links a:hover {
  color: #F26B31;
  transform: translateY(-2px);
}

.auth-links a i {
  font-size: 1.1rem;
}

/* 반응형 스타일 */
@media screen and (max-width: 440px) {
  .login-card {
    padding: 2rem 1.5rem;
    border-radius: 24px;
  }
  
  .logo {
    height: 80px;
  }
  
  .welcome-text {
    font-size: 1.4rem;
  }
  
  .form-group input,
  .btn-login {
    width: 100%;
  }
  
  .auth-links {
    flex-direction: column;
    gap: 1rem;
    align-items: center;
  }
}

@media screen and (min-width: 768px) {
  .form-group input {
    font-size: 1.1rem;
  }

  .btn-login {
    font-size: 1.2rem;
  }

  .form-group label {
    font-size: 1rem;
  }

  .auth-links {
    font-size: 1rem;
  }
}

</style>