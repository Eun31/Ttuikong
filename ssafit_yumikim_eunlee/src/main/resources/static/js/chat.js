 const crewId = new URLSearchParams(location.search).get('crewId');
    const chatBox = document.getElementById('chatBox');
    const chatInput = document.getElementById('chatInput');
    const sendBtn = document.getElementById('sendBtn');
    let myUserId = null;

    function getAvatar(senderId) {
      const avatars = ['🐱', '🐶', '🐰', '🦊', '🐼', '🐨', '🐯', '🐸', '🦁', '🐻'];
      const index = senderId % avatars.length;
      return avatars[index];
    }

    function addMessage(chat, myId) {
      const isMe = chat.senderId === myId;

      const div = document.createElement('div');
      div.className = 'chat-message ' + (isMe ? 'me' : 'other');

      const avatar = document.createElement('span');
      avatar.className = 'avatar';
      avatar.textContent = getAvatar(chat.senderId);

      const bubble = document.createElement('div');
      bubble.className = 'bubble';
      bubble.textContent = chat.message;

      if (isMe) {
        div.appendChild(bubble);
        div.appendChild(avatar);
      } else {
        div.appendChild(avatar);
        div.appendChild(bubble);
      }

      chatBox.appendChild(div);
      chatBox.scrollTop = chatBox.scrollHeight;
    }

    function fetchMessages() {
      fetch(`/api/chat/${crewId}`)
        .then(res => res.json())
        .then(data => {
          chatBox.innerHTML = '';
          data.forEach(chat => {
            addMessage(chat, myUserId);
          });
        });
    }

    function sendMessage() {
      const message = chatInput.value.trim();
      if (!message) return;
      fetch(`/api/chat/${crewId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message })
      }).then(() => {
        chatInput.value = '';
        fetchMessages();
      });
    }

    sendBtn.addEventListener('click', sendMessage);
    chatInput.addEventListener('keydown', e => {
      if (e.key === 'Enter') sendMessage();
    });

    fetch('/api/users/me')
      .then(res => res.json())
      .then(user => {
        myUserId = user.id;
        fetchMessages();
      });

    setInterval(fetchMessages, 5000);