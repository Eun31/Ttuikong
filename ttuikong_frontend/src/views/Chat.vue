<template>
  <div class="chat-container">
    <div class="chat-header">
      <button class="back-button" @click="goBack">←</button>
      <h2 id="crewTitle">크루 채팅방</h2>
    </div>
    <div class="chat-box" ref="chatBox">
      <div
        v-for="(chat, index) in messages"
        :key="index"
        :class="['chat-message', chat.senderId === myUserId ? 'me' : 'other']"
      >
        <span class="avatar">{{ getAvatar(chat.senderId) }}</span>
        <div class="bubble">{{ chat.message }}</div>
      </div>
    </div>
    <div class="chat-input-area">
      <input
        type="text"
        v-model="chatInput"
        id="chatInput"
        placeholder="메시지를 입력하세요..."
        @keydown.enter="sendMessage"
      />
      <button id="sendBtn" @click="sendMessage">전송</button>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      crewId: null,
      messages: [
        { senderId: 1, message: "안녕하세요! " },
        { senderId: 2, message: "안녕하세요, 반갑습니다 " },
        { senderId: 1, message: "오늘 러닝 몇 시에 시작하실래요?" },
        { senderId: 2, message: "저는 7시쯤 괜찮을 것 같아요!" }
      ],
      chatInput: '',
      myUserId: 2,
      fetchInterval: null,
    };
  },
  mounted() {
    this.crewId = new URLSearchParams(window.location.search).get('crewId');
    this.fetchUserId().then(() => {
      this.fetchMessages();
      this.fetchInterval = setInterval(this.fetchMessages, 5000);
    });
  },
  beforeUnmount() {
    clearInterval(this.fetchInterval);
  },
  methods: {
    goBack() {
      // 이전 페이지로 돌아가기
      this.$router.go(-1);
    },
    getAvatar(senderId) {
      const avatars = ['🐱', '🐶', '🐰', '🦊', '🐼', '🐨', '🐯', '🐸', '🦁', '🐻'];
      const index = senderId % avatars.length;
      return avatars[index];
    },
    fetchUserId() {
      return fetch('/api/users/me')
        .then(res => res.json())
        .then(user => {
          this.myUserId = user.id;
        });
    },
    fetchMessages() {
      fetch(`/api/chat/${this.crewId}`)
        .then(res => res.json())
        .then(data => {
          this.messages = data;
          this.$nextTick(() => {
            const chatBox = this.$refs.chatBox;
            if (chatBox) chatBox.scrollTop = chatBox.scrollHeight;
          });
        });
    },
    sendMessage() {
      const message = this.chatInput.trim();
      if (!message) return;

      fetch(`/api/chat/${this.crewId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message })
      }).then(() => {
        this.chatInput = '';
        this.fetchMessages();
      });
    }
  }
};
</script>

<style scoped>
/* 전체 페이지 배경 */
:root {
  --main-bg-color: #e8f5e2;
  --header-bg-color: #66bb6a;
  --send-button-color: #4caf50;
  --send-button-hover: #3d8b40;
  --my-bubble-color: #bbdefb;
  --other-bubble-color: #e1f5fe;
  --chat-box-bg: #f5fff7;
  --border-radius: 20px;
  --shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  --border-color: #c8e6c9;
}

body {
  background-color: var(--main-bg-color);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-image: 
    radial-gradient(circle at 50% 50%, rgba(144, 238, 144, 0.15) 10%, transparent 10.5%),
    radial-gradient(circle at 70% 20%, rgba(144, 238, 144, 0.1) 15%, transparent 15.5%),
    radial-gradient(circle at 20% 80%, rgba(144, 238, 144, 0.15) 8%, transparent 8.5%);
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
  color: #4caf50;
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

.chat-header h2:before, .chat-header h2:after {
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
  background: linear-gradient(to bottom, rgba(0,0,0,0.05), transparent);
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
  color: #4caf50;
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
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
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
  border: 1px solid rgba(187, 222, 251, 0.5);
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
  border-right: 1px solid rgba(187, 222, 251, 0.5);
  border-bottom: 1px solid rgba(187, 222, 251, 0.5);
}

.other .bubble {
  background-color: var(--other-bubble-color);
  border-bottom-left-radius: 4px;
  color: #333;
  border: 1px solid rgba(225, 245, 254, 0.5);
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
  border-left: 1px solid rgba(225, 245, 254, 0.5);
  border-bottom: 1px solid rgba(225, 245, 254, 0.5);
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
  background: linear-gradient(to top, rgba(0,0,0,0.05), transparent);
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
  background-color: #f9fff9;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.03);
}

#chatInput:focus {
  border-color: var(--header-bg-color);
  box-shadow: 0 0 0 3px rgba(102, 187, 106, 0.2);
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
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 24px;
  padding: 0 22px;
  cursor: pointer;
  font-weight: bold;
  font-size: 0.95rem;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(76, 175, 80, 0.3);
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
  background: linear-gradient(
    120deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  transition: all 0.6s;
}

#sendBtn:hover {
  background-color: var(--send-button-hover);
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(76, 175, 80, 0.4);
}

#sendBtn:hover:before {
  left: 100%;
}

#sendBtn:active {
  transform: translateY(1px);
  box-shadow: 0 2px 4px rgba(76, 175, 80, 0.4);
}

/* 미디어 쿼리 - 모바일 화면 대응 */
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

/* 추가적인 귀여운 요소들 */
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
  background: rgba(0,0,0,0.03);
  border-radius: 4px;
}

.chat-box::-webkit-scrollbar-thumb {
  background-color: rgba(76, 175, 80, 0.2);
  border-radius: 4px;
  transition: background-color 0.3s;
}

.chat-box::-webkit-scrollbar-thumb:hover {
  background-color: rgba(76, 175, 80, 0.4);
}
</style>