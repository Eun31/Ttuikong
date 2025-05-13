function initPageA() {
  const userId = "loginUser";

  const map = new kakao.maps.Map(document.getElementById('map'), {
    center: new kakao.maps.LatLng(37.501330, 127.039624),
    level: 4
  });
  window.kakaoMap = map;

  fetch('/api/runs/rank')
    .then(res => res.json())
    .then(users => {
      const list = document.getElementById('rankList');
      list.innerHTML = '';
      users.forEach(user => {
        const div = document.createElement('div');
        div.className = 'user-card';
        div.innerHTML = `<strong>${user.nickname}</strong><span>${user.duration}</span>`;
        list.appendChild(div);
      });
    });

  fetch('/api/crews/my')
    .then(res => res.json())
    .then(crews => {
      const list = document.getElementById('myGroupList');
      crews.forEach(crew => {
        const div = document.createElement('div');
        div.className = 'group-card';
        div.innerHTML = `<strong>${crew.room_name}</strong>`;
        div.onclick = () => loadPage('pageB'); // 동적 이동
        list.appendChild(div);
      });
    });
}
