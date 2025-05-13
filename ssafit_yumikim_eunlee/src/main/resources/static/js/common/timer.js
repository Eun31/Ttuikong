let tracking = false;
let startTime, endTime, path = [], watchId = null, durationTimer = null;
const userId = "loginUser";

document.getElementById('toggleTracking').addEventListener('click', () => {
  if (!tracking) {
    tracking = true;
    path = [];
    startTime = new Date();
    document.getElementById('toggleTracking').textContent = '⏹';

    fetch('/api/runs/running-status', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId, startTime: startTime.toISOString(), status: 'running' })
    });

    if (navigator.geolocation) {
      watchId = navigator.geolocation.watchPosition(pos => {
        const latlng = new kakao.maps.LatLng(pos.coords.latitude, pos.coords.longitude);
        path.push(latlng);
        window.kakaoMap?.setCenter(latlng);
        new kakao.maps.Marker({ position: latlng, map: window.kakaoMap });
      });
    }

    durationTimer = setInterval(() => {
      const now = new Date();
      const diff = new Date(now - startTime);
      const min = String(diff.getMinutes()).padStart(2, '0');
      const sec = String(diff.getSeconds()).padStart(2, '0');
      document.getElementById('duration').textContent = `⏱ ${min}:${sec}`;
    }, 1000);
  } else {
    tracking = false;
    endTime = new Date();
    clearInterval(durationTimer);
    navigator.geolocation.clearWatch(watchId);

    const polyline = new kakao.maps.Polyline({ path, strokeWeight: 4, strokeColor: '#e7b147', map: window.kakaoMap });
    const bounds = new kakao.maps.LatLngBounds();
    path.forEach(p => bounds.extend(p));
    window.kakaoMap?.setBounds(bounds);

    const distanceKm = (polyline.getLength() / 1000).toFixed(2);

    fetch('/api/runs/track-location', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId, startTime: startTime.toISOString(), endTime: endTime.toISOString(), distance: distanceKm })
    });

    document.getElementById('toggleTracking').textContent = '▶';
  }
});
