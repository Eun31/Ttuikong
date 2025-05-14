function initPageTimer() {
  let tracking = false;
  let startTime, endTime, path = [], watchId = null, durationTimer = null;
  const userId = "loginUser";

  kakao.maps.load(() => {
    const map = new kakao.maps.Map(document.getElementById('map'), {
      center: new kakao.maps.LatLng(37.501330, 127.039624),
      level: 4
    });

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
            map.setCenter(latlng);
            new kakao.maps.Marker({ position: latlng, map });
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

        const polyline = new kakao.maps.Polyline({
          path,
          strokeWeight: 4,
          strokeColor: '#e7b147',
          strokeOpacity: 0.8,
          strokeStyle: 'solid',
          map
        });

        const bounds = new kakao.maps.LatLngBounds();
        path.forEach(p => bounds.extend(p));
        map.setBounds(bounds);

        const distanceM = polyline.getLength();
        const distanceKm = (distanceM / 1000).toFixed(2);

        fetch('/api/runs/track-location', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            // userId,
            startTime: startTime.toISOString(),
            endTime: endTime.toISOString(),
            distance: distanceKm
          })
        });

        setTimeout(() => {
          html2canvas(document.getElementById('map')).then(canvas => {
            canvas.toBlob(blob => {
              const formData = new FormData();
              formData.append('image', blob, 'map_capture.png');
              // formData.append('userId', userId);
              formData.append('startTime', startTime.toISOString());
              formData.append('endTime', endTime.toISOString());

              fetch('/api/runs/upload-map-image', {
                method: 'POST',
                body: formData
              })
                .then(res => res.json())
                .then(data => console.log("지도 이미지 업로드 완료:", data))
                .catch(err => console.error("지도 이미지 업로드 실패:", err));
            });
          });
        }, 1000);

        document.getElementById('toggleTracking').textContent = '▶';
      }
    });
  });
}
