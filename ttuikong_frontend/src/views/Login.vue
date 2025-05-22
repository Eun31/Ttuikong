<template>
  <div class="signup-container">
    <div class="signup-content">
      <div class="signup-form-wrapper">
        <div class="signup-header">
          <img :src="logo" class="logo animated" alt="logo" />
        </div>

        <div class="signup-form">
          <div class="form-group">
            <label for="email">이메일</label>
            <div class="input-wrapper">
              <i class="icon-email"></i>
              <input type="email" id="email" v-model="email" placeholder="이메일을 입력해주세요" />
            </div>
          </div>

          <div class="form-group">
            <label for="password">비밀번호</label>
            <div class="input-wrapper">
              <i class="icon-lock"></i>
              <input type="password" id="password" v-model="password" placeholder="비밀번호를 입력해주세요" />
            </div>
          </div>

          <button class="btn btn-primary" @click="login">
            로그인 <i class="icon-arrow-right"></i>
          </button>

          <div class="login-links">
            <div class="login-divider"></div>

            <span class="signup-text">아직 회원이 아니신가요?
              <router-link to="/signup" class="signup-link">회원가입</router-link>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const logo = new URL('../assets/logo_orange.png', import.meta.url).href;
const email = ref('');
const password = ref('');
const router = useRouter();
const userId = ref('');

const login = async () => {
  console.log('로그인 시도:', email.value);

  if (email.value && password.value) {
    try {
      const response = await fetch('http://localhost:8080/api/users/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          email: email.value,
          password: password.value
        })
      });

      if (!response.ok) {
        alert('이메일 또는 비밀번호가 올바르지 않습니다.');
        return;
      }

      const data = await response.json();
      localStorage.setItem('jwt', data.token);
      localStorage.setItem('userId', data.user.id);
      console.log(data.token);
      console.log(data.user.id);

      router.push('/');
    } catch (error) {
      console.error('로그인 오류:', error);
      alert('서버와의 연결에 문제가 발생했습니다.');
    }
  } else {
    alert('이메일과 비밀번호를 입력해주세요.');
  }
};
</script>

<style scoped>
/* 중앙 정렬을 위한 컨테이너 스타일 */
.signup-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: var(--background-color);
  padding: 1rem;
}

.signup-content {
  width: 100%;
  max-width: 450px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.signup-form-wrapper {
  width: 100%;
}

/* 헤더 스타일 */
.signup-header {
  text-align: center;
  margin-bottom: 2rem;
}

.logo {
  height: 70px;
  margin-bottom: 1rem;
}

/* 로고 애니메이션 */
.logo.animated {
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }

  50% {
    transform: translateY(-10px);
  }

  100% {
    transform: translateY(0px);
  }
}

/* 폼 스타일 */
.signup-form {
  background-color: var(--card-color);
  border-radius: var(--border-radius);
  padding: 2rem;
  box-shadow: var(--shadow-md);
  width: 100%;
  animation: fadeIn 0.4s ease-out;
}

/* 폼 요소 스타일 */
.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  font-weight: 500;
  margin-bottom: 0.5rem;
  color: var(--dark-text);
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  border: 1px solid var(--border-color);
  border-radius: var(--input-radius);
  background-color: white;
  transition: var(--transition);
}

.input-wrapper:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(255, 87, 34, 0.1);
}

.input-wrapper i {
  color: var(--light-text);
  margin-left: 12px;
  font-size: 1.25rem;
}

input {
  width: 100%;
  padding: 12px 16px;
  border: none;
  background: transparent;
  font-size: 1rem;
  color: var(--dark-text);
  font-family: inherit;
}

input:focus {
  outline: none;
}

input::placeholder {
  color: var(--lightest-text);
}

/* 로그인 버튼 스타일 - 색상 조정 */
.btn-primary {
  background-color: #FF8A65;
  /* 더 부드러운 색상으로 변경 */
  color: white;
  font-weight: 500;
  /* 두께 줄임 */
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 0.8rem 1.5rem;
  border-radius: var(--input-radius);
  cursor: pointer;
  transition: var(--transition);
  border: none;
  outline: none;
  font-size: 1rem;
  margin-top: 1rem;
  margin-bottom: 1.5rem;
}

.btn-primary:hover {
  background-color: #FF7043;
  /* 호버 시 약간 더 진한 색상 */
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

/* 세로 정렬된 링크 스타일 */
.login-links {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.8rem;
  margin-top: 0.5rem;
}

.link-text {
  color: var(--medium-text);
  text-decoration: none;
  font-size: 0.95rem;
  transition: var(--transition);
}

.link-text:hover {
  color: var(--primary-color);
}

.login-divider {
  width: 40px;
  height: 1px;
  background-color: var(--border-color);
}

.signup-text {
  font-size: 0.95rem;
  color: var(--medium-text);
}

.signup-link {
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 500;
  transition: var(--transition);
  font-size: 1rem;
}

.signup-link:hover {
  text-decoration: underline;
}

/* 애니메이션 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 반응형 스타일 */
@media (max-width: 480px) {
  .signup-form {
    padding: 1.5rem;
  }
}
</style>