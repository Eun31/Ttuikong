import { createRouter, createWebHistory } from 'vue-router'

// 페이지 컴포넌트 가져오기
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import Board from '../views/Board.vue'
import Chat from '../views/Chat.vue'
import Profile from '../views/Profile.vue'
import SignUp from '../views/SignUp.vue'
import SignUp2 from '../views/SignUp2.vue'
import SignUp3 from '../views/SignUp3.vue'
import Run from '../views/RunHome.vue'
import PostWrite from '../views/PostWrite.vue' // 추가된 게시글 작성 컴포넌트

// 임시 컴포넌트 정의 (아직 파일이 없는 경우)
const PostDetail = {
  template: '<div class="temp-page"><h2>게시글 상세 페이지</h2><p>이 페이지는 준비 중입니다.</p></div>',
  name: 'PostDetail'
}

const NewChat = {
  template: '<div class="temp-page"><h2>새 채팅 페이지</h2><p>이 페이지는 준비 중입니다.</p></div>',
  name: 'NewChat'
}

const ProfileEdit = {
  template: '<div class="temp-page"><h2>프로필 편집 페이지</h2><p>이 페이지는 준비 중입니다.</p></div>',
  name: 'ProfileEdit'
}

// 라우트 설정
const routes = [
  {
    path: '/',
    name: 'home',
    component: Home
  },
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: { allowAnonymous: true }
  },
  {
    path: '/signup',
    name: 'signup',
    component: SignUp,
    meta: { allowAnonymous: true }
  },
  {
    path: '/signup2',  
    name: 'signup2',
    component: SignUp2,
    meta: { allowAnonymous: true }
  },
  {
    path: '/signup3',  
    name: 'signup3',
    component: SignUp3,
    meta: { allowAnonymous: true }
  },
  {
    path: '/board',
    name: 'board',
    component: Board
  },
  {
    path: '/board/write',  // 구체적인 경로를 먼저 배치
    name: 'post-write',
    component: PostWrite  // 실제 컴포넌트 사용
  },
  {
    path: '/board/:id',    // 동적 경로를 나중에 배치
    name: 'post-detail',
    component: PostDetail,
    props: true
  },
  {
    path: '/chat',
    name: 'chat',
    component: Chat
  },
  {
    path: '/chat/new',
    name: 'new-chat',
    component: NewChat
  },
  {
    path: '/profile',
    name: 'profile',
    component: Profile
  },
  {
    path: '/profile/edit',
    name: 'profile-edit',
    component: ProfileEdit
  },
  // 러닝 관련 라우트
  {
    path: '/run',
    name: 'run-home',
    component: Run
  },
  {
    path: '/run/:view',
    name: 'run-with-view',
    component: Run,
    props: true
  },
  // 홈 컴포넌트에서 사용하는 라우트들을 임시 컴포넌트로 리다이렉트
  {
    path: '/challenge',
    name: 'challenge',
    redirect: '/'
  },
  {
    path: '/calendar',
    name: 'calendar',
    redirect: '/'
  },
  {
    path: '/routes',
    name: 'routes',
    redirect: '/'
  }
]

// 라우터 생성
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 로그인 필요한 페이지에 대한 네비게이션 가드 설정
router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('token')
  
  // 로그인 없이 접근 가능한 페이지가 아니면서 인증이 안된 경우
  if (!to.meta.allowAnonymous && !isAuthenticated) {
    next({ name: 'login' })
  } else {
    next()
  }
})
export default router