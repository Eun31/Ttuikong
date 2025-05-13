function initPageB() {
  const userId = "loginUser";
  const crewId = window.location.pathname.split('/').pop() || "1";

  fetch(`/api/runs/crew/${crewId}`)
    .then(res => res.json())
    .then(members => {
      const list = document.getElementById('crewMemberList');
      list.innerHTML = '';
      members.forEach(m => {
        const div = document.createElement('div');
        div.className = 'user-card';
        div.innerHTML = `<strong>${m.nickname}</strong><span>${m.duration}</span>`;
        list.appendChild(div);
      });
    });

  document.getElementById('goToChat').onclick = () => {
    window.location.href = `/chat/crew/${crewId}`;
  };

  document.getElementById('growthBar').style.width = "40%";
}
