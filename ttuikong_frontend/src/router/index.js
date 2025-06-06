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
import PostWrite from '../views/PostWrite.vue' // 게시글 작성 컴포넌트
import PostDetail from '../views/PostDetail.vue' // 게시글 상세 컴포넌트 추가
import RouteFind from '../views/RouteFind.vue' // 루트 찾기 컴포넌트 추가
import Calendar from '../views/Calendar.vue'
import ProfileEdit from '../views/ProfileEdit.vue'
const NewChat = {
  template: '<div class="temp-page"><h2>새 채팅 페이지</h2><p>이 페이지는 준비 중입니다.</p></div>',
  name: 'NewChat'
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
    path: '/board/write',
    name: 'post-write',
    component: PostWrite
  },
  {
    path: '/board/edit/:id', 
    name: 'post-edit',
    component: PostWrite,
    props: true
  },
  {
    path: '/board/:id',
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
    path: '/chat/:crewId',
    name: 'chat-room',
    component: Chat,
    props: true
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
  {
    path:'/profile/:userId',
    name:'user-profile',
    component: Profile,
    props: true
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
  // 루트 찾기 페이지 추가
  {
    path: '/routes',
    name: 'routes',
    component: RouteFind
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
    component: Calendar
  }
]

// 라우터 생성
const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  }
})

// 로그인 필요한 페이지에 대한 네비게이션 가드 설정
router.beforeEach((to, from) => {
  const isAuthenticated = localStorage.getItem('jwt')

  // 로그인 없이 접근 가능한 페이지가 아니면서 인증이 안된 경우
  if (!to.meta.allowAnonymous && !isAuthenticated) {
    return { name: 'login' }
  } else {
    return true
  }
})

export default router