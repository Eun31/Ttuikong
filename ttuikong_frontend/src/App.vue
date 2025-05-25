<template>
  <div id="app">
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
          <button v-if="isLoggedIn" class="nav-btn text" @click="logout"><i class="ri-logout-box-r-line"></i>
            <span>로그아웃</span></button>
          <template v-else>
            <router-link to="/login" class="nav-btn fill"><i class="ri-login-box-line"></i>
              <span>로그인</span></router-link>
            <router-link to="/signup" class="nav-btn text"><i class="ri-user-add-line"></i>
              <span>회원가입</span></router-link>
          </template>
          <router-link v-if="isLoggedIn" to="/profile" class="nav-btn fill"><i class="ri-user-line"></i>
            <span>마이페이지</span></router-link>
        </nav>
      </div>
    </header>

    <div class="mobile-menu" :class="{ active: menuActive }" v-if="!isAuthPage">
      <router-link to="/" class="menu-item" @click="closeMenu">홈</router-link>
      <router-link to="/board" class="menu-item" @click="closeMenu">게시판</router-link>
      <router-link to="/chat" class="menu-item" @click="closeMenu">채팅</router-link>

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
      <router-view :key="$route.path" />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import logoImg from './assets/logo_orange.png';

const route = useRoute();
const router = useRouter();

const menuActive = ref(false);
const logo = logoImg;

const isLoggedIn = computed(() => {
  return localStorage.getItem('jwt') !== null;
});

const isAuthPage = computed(() => {
  const authPages = ['/login', '/signup', '/signup2', '/signup3'];
  return authPages.includes(route.path);
});

const toggleMenu = () => {
  menuActive.value = !menuActive.value;
};

const closeMenu = () => {
  menuActive.value = false;
};

const logout = () => {
  localStorage.removeItem('jwt');
  localStorage.removeItem('token');
  localStorage.removeItem('userId');
  localStorage.removeItem('nickname');
  router.push('/login');
};

watch(route, () => {
  closeMenu();
});
</script>


<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* 웹 화면 뷰 조절 */
#app {
  width: 100%;
  max-width: 1240px;
  margin-left: auto;
  margin-right: auto;
}

body {
  font-family: 'Poppins', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
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

.header {
  background-color: #FFFFFF;
  padding: 0;
  /* 패딩 줄임 */
  position: relative;
  color: #333;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-container {
  display: flex;
  justify-content: space-between;
  position: relative;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

/* 로고 위치 유지 */
.logo {
  display: inline-block;
}

.logo img {
  margin-top: 20px;
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
  margin-top: 25px;
}

.nav-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 14px;
  /* 패딩 줄임 (상하 패딩 5px로 감소) */
  border-radius: 20px;
  font-weight: 500;
  transition: all 0.3s ease;
  font-size: 14px;
  height: 28px;
  /* 높이 직접 지정해서 줄임 */
  line-height: 1;
  /* 줄 높이 조정 */
}

.nav-btn i {
  font-size: 16px;
  /* 아이콘 크기 줄임 */
}

.nav-btn.text {
  color: #FF7E47;
  /* 주황색 텍스트 */
  border: 1px solid rgba(255, 126, 71, 0.5);
  /* 주황색 테두리 */
}

.nav-btn.text:hover {
  background-color: rgba(255, 126, 71, 0.1);
  /* 주황색 연한 배경 */
}

.nav-btn.fill {
  background-color: #FF7E47;
  /* 주황색 배경 */
  color: white;
}

.nav-btn.fill:hover {
  background-color: #F26B31;
  /* 더 진한 주황색으로 변경 */
}

/* 메뉴 토글 버튼 색상 변경 */
.menu-toggle {
  display: none;
  font-size: 24px;
  color: #FF7E47;
  padding: 8px;
  border-radius: 4px;
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

/* 반응형 스타일 - 모바일 우선 */
@media (max-width: 768px) {
  .main-nav {
    display: none;
    /* 모바일에서는 네비게이션 숨김 */
  }

  .menu-toggle {
    display: block;
    position: absolute;
    right: 1rem;
    top: 50%;
    transform: translateY(-50%);
    z-index: 100;
  }

  .logo img {
    margin-top: 20px;
    height: 32px;
    /* 모바일에서 로고 크기 줄임 */
  }

  /* 네비게이션 버튼은 유지하되 크기 조정 */
  .nav-buttons {
    display: flex;
    gap: 6px;
    /* 버튼 간격 줄임 */
    margin-top: 25px;
    /* 위쪽으로 이동 조정 */
  }

  .nav-btn {
    padding: 4px 8px;
    /* 패딩 더 줄임 */
    font-size: 12px;
    /* 글꼴 크기 줄임 */
    height: 26px;
    /* 높이 줄임 */
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-btn i {
    font-size: 14px;
    /* 아이콘 크기 줄임 */
  }

  /* 텍스트를 숨기지 않도록 수정 */
  .nav-btn span {
    display: inline-block;
    /* 텍스트 표시 */
    font-size: 11px;
    /* 텍스트 크기 더 줄임 */
  }

  .main-content {
    padding: 0px;
    /* 모바일에서 컨텐츠 영역 패딩 조정 */
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
}

/* 매우 작은 모바일 기기용 추가 스타일 */
@media (max-width: 480px) {
  .header {
    padding: 0.5rem 0;
    /* 패딩 더 줄임 */
  }

  .logo img {
    margin-top: 20px;
    height: 28px;
    /* 더 작은 기기에서 로고 크기 더 줄임 */
  }

  .nav-buttons {
    margin-top: 25px;
    /* 작은 화면에서 위쪽 이동 조정 */
    gap: 4px;
    /* 버튼 간격 더 줄임 */
  }

  /* 작은 화면에서도 텍스트 유지하도록 수정 */
  .nav-btn {
    padding: 3px 6px;
    /* 패딩 더 줄임 */
    min-width: 0;
    height: 24px;
    /* 높이 더 줄임 */
  }

  .nav-btn span {
    display: inline-block;
    /* 텍스트 표시 유지 */
    font-size: 10px;
    /* 텍스트 크기 더 줄임 */
  }

  .nav-btn i {
    font-size: 14px;
    /* 아이콘 크기 줄임 */
    margin-right: 1px;
    /* 아이콘과 텍스트 간격 줄임 */
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
    display: none;
    /* PC에서는 모바일 메뉴 숨김 */
  }

  .menu-toggle {
    display: none;
    /* PC에서는 메뉴 토글 버튼 숨김 */
  }
}
</style>