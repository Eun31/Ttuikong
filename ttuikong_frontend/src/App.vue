// App.vue
<template>
  <div id="app">
    <!-- 새로운 헤더 -->
    <header class="header">
      <div class="container header-container">
        <a href="#" class="logo">
          <img :src="logo" alt="Logo" />
        </a>
        <nav class="nav-buttons">
          <button class="nav-btn text"><i class="ri-logout-box-r-line"></i> 로그아웃</button>
          <router-link to="/profile" class="nav-btn fill"><i class="ri-user-line"></i> 마이페이지</router-link>
          <!-- <button class="nav-btn fill"><i class="ri-login-box-line"></i> 로그인</button> -->
        </nav>
      </div>
    </header>


    <!-- 모바일 메뉴 (토글 가능) -->
    <div class="mobile-menu" :class="{ active: menuActive }">
      <router-link to="/" class="menu-item" @click="closeMenu">홈</router-link>
      <router-link to="/board" class="menu-item" @click="closeMenu">게시판</router-link>
      <router-link to="/chat" class="menu-item" @click="closeMenu">채팅</router-link>
      <router-link to="/profile" class="menu-item" @click="closeMenu">마이페이지</router-link>
    </div>

    <!-- 메인 컨텐츠 영역 - 라우터 뷰 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 플로팅 액션 버튼 (현재 라우트에 따라 다른 기능) -->
    <a @click.prevent="handleFloatingAction" class="floating-btn">
      <span :class="floatingBtnIcon"></span>
    </a>
  </div>
</template>

<script>
import logo from './assets/logo_1.png'; // 이미지 임포트

export default {
  name: 'App',
  data() {
    return {
      menuActive: false,
      logo: logo // 이미지를 data로 제공
    };
  },
  computed: {
    // 현재 라우트에 따라 플로팅 버튼 아이콘 변경
    floatingBtnIcon() {
      const path = this.$route.path;
      if (path.startsWith('/board')) return 'ri-pencil-line';  // 게시판 - 글쓰기
      if (path.startsWith('/chat')) return 'ri-message-2-line';  // 채팅 - 새 메시지
      if (path.startsWith('/profile')) return 'ri-edit-line';  // 프로필 - 편집
      return 'ri-add-line';  // 기본값
    }
  },
  methods: {
    toggleMenu() {
      this.menuActive = !this.menuActive;
    },
    closeMenu() {
      this.menuActive = false;
    },
    handleFloatingAction() {
      const path = this.$route.path;
      
      // 현재 라우트에 따라 다른 액션 수행
      if (path.startsWith('/board')) {
        this.$router.push('/board/write');  // 게시글 작성 페이지로 이동
      } 
      else if (path.startsWith('/chat')) {
        this.$router.push('/chat/new');  // 새 채팅 시작
      }
      else if (path.startsWith('/profile')) {
        this.$router.push('/profile/edit');  // 프로필 편집
      }
      else {
        // 기본 액션
        console.log('기본 액션 실행');
      }
    }
  }
};
</script>

<style>
/* 기본 리셋 및 공통 스타일 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Pretendard', 'Poppins', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  background-color: #F1FEE9;
  color: #333;
  line-height: 1.6;
}

a {
  text-decoration: none;
  color: inherit;
}

img {
  max-width: 100%;
}

button {
  cursor: pointer;
  border: none;
  background: none;
  font-family: inherit;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

/* 헤더 스타일 */
.header {
  background-color: #F1FEE9;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 1rem 0;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo img {
  height: 40px;
}

.nav-buttons {
  display: flex;
  gap: 12px;
}

.nav-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 4px;
  font-weight: 500;
  transition: all 0.3s ease;
  font-size: 14px;
}

.nav-btn i {
  font-size: 18px;
}

.nav-btn.text {
  color: #555;
}

.nav-btn.text:hover {
  background-color: #f5f5f5;
}

.nav-btn.fill {
  background-color: #4caf50;
  color: white;
}

.nav-btn.fill:hover {
  background-color: #43a047;
}

/* 내비게이션 메뉴 */
.main-nav {
  background-color: #f8f9fa;
  border-bottom: 1px solid #eee;
  padding: 0.5rem 0;
}

.main-nav .container {
  display: flex;
  gap: 24px;
}

.nav-item {
  font-weight: 500;
  color: #555;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.nav-item:hover, .nav-item.router-link-active {
  color: #4caf50;
  background-color: rgba(76, 175, 80, 0.1);
}

/* 모바일 메뉴 */
.mobile-menu {
  position: fixed;
  top: 0;
  right: -280px;
  width: 280px;
  height: 100vh;
  background-color: white;
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.1);
  padding: 60px 20px 20px;
  transition: right 0.3s ease;
  z-index: 99;
}

.mobile-menu.active {
  right: 0;
}

.menu-item {
  display: block;
  padding: 15px 10px;
  border-bottom: 1px solid #eee;
  font-weight: 500;
}

/* 메인 컨텐츠 영역 */
.main-content {
  padding: 20px 0;
  max-width: 800px;
  margin: 0 auto;
}

/* 플로팅 버튼 */
.floating-btn {
  position: fixed;
  right: 20px;
  bottom: 20px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #4caf50;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  z-index: 90;
  transition: transform 0.2s ease, background-color 0.2s ease;
  cursor: pointer;
}

.floating-btn:active {
  transform: scale(0.95);
  background-color: #43a047;
}

/* 반응형 스타일 */
@media (max-width: 768px) {
  .main-nav {
    display: none; /* 모바일에서는 네비게이션 숨김 */
  }
  
  .header-container {
    position: relative;
  }
  
  .nav-buttons {
    display: none; /* 추가 설정 필요 시 주석 해제 */
  }
  
  .menu-toggle {
    display: block;
    font-size: 24px;
    color: #333;
  }
}

@media (min-width: 769px) {
  .mobile-menu {
    display: none; /* PC에서는 모바일 메뉴 숨김 */
  }
}
</style>