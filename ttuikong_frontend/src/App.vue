<template>
  <div id="app">
    <!-- 기존 템플릿 코드 유지 -->
    <header class="header" v-if="!isAuthPage">
      <div class="container header-container">
        <router-link to="/" class="logo">
          <img :src="logo" alt="Logo" />
        </router-link>
        <button class="menu-toggle" @click="toggleMenu" v-if="!isAuthPage">
          <i class="ri-menu-line" v-if="!menuActive"></i>
          <i class="ri-close-line" v-else></i>
        </button>
        <nav class="nav-buttons">
          <button v-if="isLoggedIn" class="nav-btn text" @click="logout"><i class="ri-logout-box-r-line"></i> <span>로그아웃</span></button>
          <template v-else>
            <router-link to="/login" class="nav-btn fill"><i class="ri-login-box-line"></i> <span>로그인</span></router-link>
            <router-link to="/signup" class="nav-btn text"><i class="ri-user-add-line"></i> <span>회원가입</span></router-link>
          </template>
          <router-link v-if="isLoggedIn" to="/profile" class="nav-btn fill"><i class="ri-user-line"></i> <span>마이페이지</span></router-link>
        </nav>
      </div>
    </header>

    <div class="mobile-menu" :class="{ active: menuActive }" v-if="!isAuthPage">
      <router-link to="/" class="menu-item" @click="closeMenu">홈</router-link>
      <router-link to="/board" class="menu-item" @click="closeMenu">게시판</router-link>
      <router-link to="/chat" class="menu-item" @click="closeMenu">채팅</router-link>
      
      <!-- 인증 관련 메뉴 항목 추가 -->
      <div class="menu-auth-section">
        <template v-if="isLoggedIn">
          <router-link to="/profile" class="menu-item" @click="closeMenu">마이페이지</router-link>
          <a href="#" class="menu-item" @click="logout(); closeMenu();">로그아웃</a>
        </template>
        <template v-else>
          <router-link to="/login" class="menu-item" @click="closeMenu">로그인</router-link>
          <router-link to="/signup" class="menu-item" @click="closeMenu">회원가입</router-link>
        </template>
      </div>
    </div>

    <main class="main-content" :class="{ 'auth-page': isAuthPage }">
      <router-view />
    </main>
  </div>
</template>

<script>
import logo from './assets/logo_orange.png'; // 이미지 임포트

export default {
  name: 'App',
  data() {
    return {
      menuActive: false,
      logo: logo // 이미지를 data로 제공
    };
  },
  computed: {
    // 로그인 상태 확인
    isLoggedIn() {
      return localStorage.getItem('token') !== null;
    },
    // 현재 페이지가 인증 관련 페이지인지 확인
    isAuthPage() {
      const authPages = ['/login', '/signup', '/signup2', '/signup3'];
      return authPages.includes(this.$route.path);
    }
  },
  methods: {
    toggleMenu() {
      this.menuActive = !this.menuActive;
    },
    closeMenu() {
      this.menuActive = false;
    },
    // 로그아웃 기능
    logout() {
      // 토큰 제거
      localStorage.removeItem('token');
      
      // 홈으로 리다이렉트
      this.$router.push('/');
    }
  },
  watch: {
    // 라우트 변경 시 모바일 메뉴 닫기
    '$route'() {
      this.closeMenu();
    }
  }
};
</script>

<style>
/* 기본 스타일 리셋 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Pretendard', 'Poppins', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  background-color: #FFFFFF;
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

/* 헤더 스타일 - 흰색 배경으로 변경 */
.header {
  background-color: #FFFFFF;
  padding: 1rem 0;
  position: relative;
  color: #333; /* 텍스트 색상을 어두운 색으로 변경 */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05); /* 살짝 그림자 추가 */
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo img {
  height: 36px;
  width: auto;
  transition: transform 0.2s;
}

.logo:hover img {
  transform: scale(1.07);
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
  border-radius: 20px;
  font-weight: 500;
  transition: all 0.3s ease;
  font-size: 14px;
}

.nav-btn i {
  font-size: 18px;
}

.nav-btn.text {
  color: #FF7E47; /* 주황색 텍스트 */
  border: 1px solid rgba(255, 126, 71, 0.5); /* 주황색 테두리 */
}

.nav-btn.text:hover {
  background-color: rgba(255, 126, 71, 0.1); /* 주황색 연한 배경 */
}

.nav-btn.fill {
  background-color: #FF7E47; /* 주황색 배경 */
  color: white;
}

.nav-btn.fill:hover {
  background-color: #F26B31; /* 더 진한 주황색으로 변경 */
}

/* 메뉴 토글 버튼 색상 변경 */
.menu-toggle {
  display: none;
  font-size: 24px;
  color: #FF7E47; /* 주황색으로 변경 */
  padding: 8px;
  border-radius: 4px;
}

/* 내비게이션 메뉴 */
.main-nav {
  background-color: white;
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
  border-radius: 20px;
  transition: all 0.3s ease;
}

.nav-item:hover, .nav-item.router-link-active {
  color: #FF7E47;
  background-color: rgba(255, 126, 71, 0.1);
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
  overflow-y: auto;
}

.mobile-menu.active {
  right: 0;
}

.menu-item {
  display: block;
  padding: 15px 10px;
  border-bottom: 1px solid #eee;
  font-weight: 500;
  color: #333;
}

.menu-auth-section {
  margin-top: 20px;
  border-top: 2px solid #FF7E47;
  padding-top: 10px;
}

/* 메인 컨텐츠 영역 */
.main-content {
  padding: 20px 0;
  max-width: 800px;
  margin: 0 auto;
}

/* 버튼 스타일 */
.btn-primary {
  background-color: #FF7E47;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 20px;
  font-weight: 500;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  display: inline-block;
  text-align: center;
  width: 100%;
}

.btn-primary:hover {
  background-color: #F26B31;
}

/* 입력 필드 */
.form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 10px;
  margin-bottom: 16px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.form-input:focus {
  border-color: #FF7E47;
  outline: none;
}

/* 카드 항목 스타일 */
.card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 16px;
  margin-bottom: 16px;
}

.card-image {
  width: 60px;
  height: 60px;
  background-color: #f5f5f5;
  border-radius: 50%;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-title {
  font-weight: 600;
  font-size: 18px;
  margin-bottom: 8px;
  color: #333;
}

.card-text {
  color: #666;
  font-size: 14px;
}

/* 선택 그리드 */
.selection-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin: 20px 0;
}

.selection-item {
  background: white;
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.selection-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.selection-icon {
  width: 40px;
  height: 40px;
  margin: 0 auto 8px;
}

.selection-label {
  font-size: 14px;
  font-weight: 500;
}

/* 원형 선택 메뉴 */
.circle-selection {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 16px;
  margin: 20px 0;
}

.circle-item {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 1px solid #FF7E47;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FF7E47;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.circle-item:hover, .circle-item.active {
  background-color: #FF7E47;
  color: white;
}

/* 반응형 스타일 - 모바일 우선 */
@media (max-width: 768px) {
  .main-nav {
    display: none; /* 모바일에서는 네비게이션 숨김 */
  }
  
  .header-container {
    position: relative;
    align-items: center; /* 모바일에서도 중앙 정렬 유지 */
  }
  
  .menu-toggle {
    display: block;
    position: absolute;
    right: 1rem;
    top: 50%;
    transform: translateY(-50%);
    z-index: 100;
  }

  .logo {
    margin-top: 3px; /* 모바일에서 상단 마진 조정 */
  }
  
  .logo img {
    height: 32px; /* 모바일에서 로고 크기 줄임 */
  }

  /* 네비게이션 버튼은 유지하되 크기 조정 */
  .nav-buttons {
    display: flex;
    gap: 6px; /* 버튼 간격 줄임 */
    align-items: center; /* 모바일에서도 중앙 정렬 유지 */
    margin-bottom: 3px; /* 모바일에서 하단 마진 조정 */
  }
  
  .nav-btn {
    padding: 6px 10px; /* 패딩 축소 */
    font-size: 12px; /* 글꼴 크기 줄임 */
    height: 32px; /* 높이 조정 */
  }
  
  .nav-btn i {
    font-size: 14px; /* 아이콘 크기 줄임 */
  }
  
  .main-content {
    padding: 15px 10px; /* 모바일에서 컨텐츠 영역 패딩 조정 */
  }

  /* 모바일 메뉴 크기 조정 */
  .mobile-menu {
    width: 250px;
    right: -250px;
  }
  
  .menu-item {
    padding: 12px 8px;
    font-size: 16px;
  }
  
  .menu-auth-section {
    margin-top: 15px;
    padding-top: 8px;
  }
  
  /* 선택 그리드 조정 */
  .selection-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .selection-item {
    padding: 16px;
  }
  
  /* 원형 선택 메뉴 조정 */
  .circle-item {
    width: 70px;
    height: 70px;
    font-size: 13px;
  }
}

/* 매우 작은 모바일 기기용 추가 스타일 */
@media (max-width: 480px) {
  .header {
    padding: 0.8rem 0;
  }
  
  .logo {
    margin-top: 2px; /* 더 작은 화면에서 마진 조정 */
  }
  
  .logo img {
    height: 28px; /* 더 작은 기기에서 로고 크기 더 줄임 */
  }
  
  .nav-buttons {
    margin-bottom: 2px; /* 더 작은 화면에서 마진 조정 */
  }
  
  /* 더 작은 기기에서 버튼 텍스트 숨기고 아이콘만 표시 */
  .nav-btn {
    padding: 6px 8px;
    min-width: 0;
    height: 30px; /* 높이 추가 조정 */
  }
  
  .nav-btn span {
    display: none; /* 텍스트 숨김 */
  }
  
  .nav-btn i {
    font-size: 16px; /* 아이콘 크기 유지 */
    margin: 0; /* 마진 제거 */
  }
  
  /* 작은 모바일 메뉴 조정 */
  .mobile-menu {
    width: 220px;
    right: -220px;
    padding-top: 50px;
  }
  
  .menu-item {
    padding: 10px 8px;
    font-size: 15px;
  }
  
  .menu-auth-section {
    margin-top: 12px;
    padding-top: 6px;
  }
}

/* 큰 화면에서는 모바일 메뉴 토글 버튼 숨김 */
@media (min-width: 769px) {
  .mobile-menu {
    display: none; /* PC에서는 모바일 메뉴 숨김 */
  }
  
  .menu-toggle {
    display: none; /* PC에서는 메뉴 토글 버튼 숨김 */
  }
}

/* 입력 필드, 버튼 등 양식 요소 반응형 스타일 */
input, select, textarea, button {
  font-size: 16px; /* 모바일에서 입력 요소가 자동으로 확대되는 것 방지 */
}

@media (max-width: 768px) {
  input, select, textarea {
    padding: 10px; /* 터치 기기에서 더 큰 터치 영역 */
  }
  
  button {
    padding: 10px 15px; /* 버튼 영역 확대 */
  }
}

/* 뷰포트 설정 - head에 추가해야 함 */
/* 
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"> 
*/
</style>