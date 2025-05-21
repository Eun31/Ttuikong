<template>
  <div class="chat-container">
    <div class="chat-header">
      <button class="back-button" @click="goBack">←</button>
      <h2 id="crewTitle">크루 채팅방</h2>
    </div>
    <div class="chat-box" ref="chatBox">
      <div v-for="(chat, index) in messages" :key="index"
        :class="['chat-message', chat.senderId === myUserId ? 'me' : 'other']">
        <span class="avatar">{{ getAvatar(chat.senderId) }}</span>
        <div class="bubble">{{ chat.message }}</div>
      </div>
    </div>
    <div class="chat-input-area">
      <input type="text" v-model="chatInput" id="chatInput" placeholder="메시지를 입력하세요..." @keydown.enter="sendMessage" />
      <button id="sendBtn" @click="sendMessage">전송</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const crewId = ref(route.params.crewId);

const messages = ref([]);
const chatInput = ref('');
const myUserId = ref(1);
let fetchInterval = null;
const chatBox = ref(null);

function goBack() {
  router.go(-1);
}

function getAvatar(senderId) {
  const avatars = ['🐱', '🐶', '🐰', '🦊', '🐼', '🐨', '🐯', '🐸', '🦁', '🐻'];
  return avatars[senderId % avatars.length];
}

// API 기본 URL 설정
const API_BASE_URL = 'http://localhost:8080';

async function fetchUserId() {
  try {
    const token = localStorage.getItem('jwt');
    console.log('JWT Token:', token); // 디버깅용

    if (!token) {
      console.error('JWT 토큰이 없습니다.');
      router.push('/login');
      return;
    }

    const res = await fetch(`${API_BASE_URL}/api/users/me`, {
      headers: { Authorization: `Bearer ${token}` }
    });

    if (!res.ok) {
      console.error('사용자 정보 조회 실패:', res.status);
      if (res.status === 401) {
        localStorage.removeItem('jwt');
        router.push('/login');
        return;
      }
    }

    const data = await res.json();
    myUserId.value = data.user?.id ?? data.id;
    console.log('현재 사용자 ID:', myUserId.value);
  } catch (error) {
    console.error('사용자 정보 조회 중 오류:', error);
  }
}

async function fetchMessages() {
  try {
    const token = localStorage.getItem('jwt');

    if (!token) {
      console.error('JWT 토큰이 없습니다.');
      router.push('/login');
      return;
    }

    const res = await fetch(`${API_BASE_URL}/api/chat/${crewId.value}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    const contentType = res.headers.get('content-type');
    if (!contentType || !contentType.includes('application/json')) {
      console.error('JSON이 아닌 응답을 받았습니다:', contentType);
      console.error('응답 텍스트:', await res.text());

      if (res.status === 401) {
        localStorage.removeItem('jwt');
        router.push('/login');
      }
      return;
    }

    if (!res.ok) {
      console.error('메시지 조회 실패:', res.status);
      if (res.status === 401) {
        localStorage.removeItem('jwt');
        router.push('/login');
        return;
      }
    }

    const data = await res.json();
    messages.value = data;

    nextTick(() => {
      if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight;
    });
  } catch (error) {
    console.error('메시지 조회 중 오류:', error);
  }
}

function sendMessage() {
  const message = chatInput.value.trim();
  if (!message) return;

  const token = localStorage.getItem('jwt');

  if (!token) {
    console.error('JWT 토큰이 없습니다.');
    router.push('/login');
    return;
  }

  fetch(`${API_BASE_URL}/api/chat/${crewId.value}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ message })
  }).then(async (res) => {
    if (!res.ok) {
      console.error('메시지 전송 실패:', res.status);
      if (res.status === 401) {
        localStorage.removeItem('jwt');
        router.push('/login');
        return;
      }
    }

    chatInput.value = '';
    await fetchMessages();
  }).catch(error => {
    console.error('메시지 전송 중 오류:', error);
  });
}

onMounted(async () => {
  console.log("현재 crewId:", crewId.value);
  await fetchUserId();
  await fetchMessages();
  fetchInterval = setInterval(fetchMessages, 5000);
});

onBeforeUnmount(() => {
  clearInterval(fetchInterval);
});
</script>

<style scoped>
/* 전체 페이지 배경 */
:root {
  --main-bg-color: #fffbf9;
  --header-bg-color: #FF7043;
  --send-button-color: #FF7043;
  --send-button-hover: #FF7043;
  --my-bubble-color: #fbd8bb;
  --other-bubble-color: #feeee1;
  --chat-box-bg: #fffbf9;
  --border-radius: 20px;
  --shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  --border-color: #e6d4c8;
}

body {
  background-color: var(--main-bg-color);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-image:
    radial-gradient(circle at 50% 50%, rgba(238, 189, 144, 0.15) 10%, transparent 10.5%),
    radial-gradient(circle at 70% 20%, rgba(238, 189, 144, 0.15) 15%, transparent 15.5%),
    radial-gradient(circle at 20% 80%, rgba(238, 189, 144, 0.15) 8%, transparent 8.5%);
  background-size: 60px 60px, 80px 80px, 70px 70px;
  background-position: 0 0, 30px 40px, 50px 20px;
}

.chat-container {
  max-width: 600px;
  margin: 20px auto;
  border-radius: var(--border-radius);
  background-color: #fff;
  display: flex;
  flex-direction: column;
  height: 90vh;
  box-shadow: var(--shadow);
  border: 4px solid var(--border-color);
  overflow: hidden;
  position: relative;
}

.chat-container:before {
  content: '';
  position: absolute;
  top: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 20px;
  background-color: var(--header-bg-color);
  border-radius: 0 0 15px 15px;
  z-index: 5;
}

.chat-header {
  background-color: white;
  color: rgba(255, 139, 44, 0.884);
  padding: 1rem;
  text-align: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: relative;
}

.chat-header:after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  right: 0;
  height: 8px;
  background-image:
    linear-gradient(45deg, transparent 50%, var(--header-bg-color) 50%),
    linear-gradient(-45deg, transparent 50%, var(--header-bg-color) 50%);
  background-size: 16px 16px;
  background-position: 0 0, 8px 0;
}

.chat-header h2 {
  margin: 0;
  font-size: 1.6rem;
  letter-spacing: 1px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-header h2:before,
.chat-header h2:after {
  content: '🏃‍➡️';
  margin: 0 10px;
  font-size: 1.2rem;
}

.chat-box {
  flex: 1;
  padding: 1.2rem;
  overflow-y: auto;
  background-color: var(--chat-box-bg);
  display: flex;
  flex-direction: column;
  background-image: url("data:image/svg+xml,%3Csvg width='52' height='26' viewBox='0 0 52 26' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%239fd996' fill-opacity='0.15'%3E%3Cpath d='M10 10c0-2.21-1.79-4-4-4-3.314 0-6-2.686-6-6h2c0 2.21 1.79 4 4 4 3.314 0 6 2.686 6 6 0 2.21 1.79 4 4 4 3.314 0 6 2.686 6 6 0 2.21 1.79 4 4 4v2c-3.314 0-6-2.686-6-6 0-2.21-1.79-4-4-4-3.314 0-6-2.686-6-6zm25.464-1.95l8.486 8.486-1.414 1.414-8.486-8.486 1.414-1.414z' /%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  position: relative;
}

.chat-box:before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 8px;
  background: linear-gradient(to bottom, rgba(0, 0, 0, 0.05), transparent);
  z-index: 1;
}

.chat-message {
  margin: 12px 0;
  padding: 0;
  border-radius: 18px;
  max-width: 70%;
  word-break: break-word;
  display: flex;
  align-items: center;
  position: relative;
  animation: fadeIn 0.3s ease-out;
}

.back-button {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.5rem;
  background: none;
  border: none;
  color: #FF7043;
  cursor: pointer;
  z-index: 10;
  padding: 0;
  line-height: 1;
  transition: transform 0.2s;
}

.back-button:hover {
  transform: translateY(-50%) scale(1.1);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-message.me {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.chat-message.other {
  align-self: flex-start;
}

.avatar {
  margin: 0 8px;
  font-size: 1.6em;
  background: linear-gradient(135deg, #ffffff 0%, #f0f0f0 100%);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
  border: 2px solid white;
  position: relative;
  z-index: 2;
}

.me .avatar {
  background: linear-gradient(135deg, #e1f5fe 0%, #bbdefb 100%);
}

.other .avatar {
  background: linear-gradient(135deg, #f0f4c3 0%, #dcedc8 100%);
}

.bubble {
  padding: 12px 16px;
  border-radius: 20px;
  position: relative;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  line-height: 1.4;
  transition: transform 0.2s;
}

.bubble:hover {
  transform: translateY(-2px);
}

.me .bubble {
  background-color: var(--my-bubble-color);
  border-bottom-right-radius: 4px;
  color: #333;
  border: 1px solid rgba(251, 208, 187, 0.5);
}

.me .bubble:after {
  content: '';
  position: absolute;
  bottom: 0;
  right: -10px;
  width: 20px;
  height: 20px;
  background-color: var(--my-bubble-color);
  border-bottom-left-radius: 16px;
  z-index: -1;
  border-right: 1px solid rgba(251, 207, 187, 0.5);
  border-bottom: 1px solid rgba(251, 207, 187, 0.5);
}

.other .bubble {
  background-color: var(--other-bubble-color);
  border-bottom-left-radius: 4px;
  color: #333;
  border: 1px solid rgba(251, 207, 187, 0.5);
}

.other .bubble:after {
  content: '';
  position: absolute;
  bottom: 0;
  left: -10px;
  width: 20px;
  height: 20px;
  background-color: var(--other-bubble-color);
  border-bottom-right-radius: 16px;
  z-index: -1;
  border-left: 1px solid rgba(251, 207, 187, 0.5);
  border-bottom: 1px solid rgba(251, 207, 187, 0.5);
}

.chat-input-area {
  display: flex;
  padding: 12px;
  background-color: #fff;
  border-top: 2px dashed var(--border-color);
  position: relative;
  z-index: 10;
}

.chat-input-area:before {
  content: '';
  position: absolute;
  top: -2px;
  left: 0;
  right: 0;
  height: 6px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.05), transparent);
}

#chatInput {
  flex: 1;
  padding: 14px 18px;
  border: 2px solid var(--border-color);
  border-radius: 24px;
  outline: none;
  font-size: 1rem;
  margin-right: 10px;
  transition: all 0.3s;
  background-color: #fffbf9;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.03);
}

#chatInput:focus {
  border-color: var(--header-bg-color);
  box-shadow: 0 0 0 3px rgba(255, 139, 44, 0.884);
  background-color: #fff;
}

#chatInput::placeholder {
  color: #aaa;
  transition: opacity 0.3s;
}

#chatInput:focus::placeholder {
  opacity: 0.5;
}

#sendBtn {
  background-color: #FF7043;
  color: white;
  border: none;
  border-radius: 24px;
  padding: 0 22px;
  cursor: pointer;
  font-weight: bold;
  font-size: 0.95rem;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(255, 139, 44, 0.884);
  position: relative;
  overflow: hidden;
}

#sendBtn:before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(120deg,
      transparent,
      rgba(255, 255, 255, 0.3),
      transparent);
  transition: all 0.6s;
}

#sendBtn:hover {
  background-color: var(--send-button-hover);
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(255, 139, 44, 0.884);
}

#sendBtn:hover:before {
  left: 100%;
}

#sendBtn:active {
  transform: translateY(1px);
  box-shadow: 0 2px 4px rgba(255, 139, 44, 0.884);
}

/* 모바일 화면 대응 */
@media (max-width: 640px) {
  .chat-container {
    margin: 0;
    height: 85vh;
    border-radius: 0;
    border-left: none;
    border-right: none;
  }

  .chat-header:after {
    display: none;
  }

  .chat-message {
    max-width: 85%;
  }
}

/* 꾸밈 요소 */
.chat-container:after {
  content: '🍀';
  position: absolute;
  bottom: -15px;
  right: 20px;
  font-size: 24px;
  transform: rotate(15deg);
}

.chat-message:nth-child(odd) {
  animation-duration: 0.4s;
}

/* 스크롤바 스타일링 */
.chat-box::-webkit-scrollbar {
  width: 8px;
}

.chat-box::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.03);
  border-radius: 4px;
}

.chat-box::-webkit-scrollbar-thumb {
  background-color: rgba(255, 139, 44, 0.884);
  border-radius: 4px;
  transition: background-color 0.3s;
}

.chat-box::-webkit-scrollbar-thumb:hover {
  background-color: rgba(255, 139, 44, 0.884);
}
</style>