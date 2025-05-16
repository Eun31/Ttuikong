function loadTimerSection() {
fetch("../html/runTimer.html")
  .then(res => res.text())
  .then(html => {
    document.getElementById("runTimerSection").innerHTML = html;

    requestAnimationFrame(() => {
      const checkKakaoReady = () => {

        if (window.kakao && window.kakao.maps && typeof kakao.maps.Map === "function") {
          console.log("✅ kakao.maps.Map 로딩 완료");

          if (typeof initPageTimer === "function") {
            console.log("initPageTimer 실행!");
            initPageTimer();
          } else {
            console.error("initPageTimer 없음");
          }
        } else {
          console.log("⌛ Kakao SDK 로딩 대기 중...");
          setTimeout(checkKakaoReady, 100);
        }
      };

      checkKakaoReady();
    });
  });

}



function loadHTML(targetId, url, callback) {
  fetch(url)
    .then(res => res.text())
    .then(html => {
      document.getElementById(targetId).innerHTML = html;
      if (callback) callback();
    });
}

window.addEventListener("DOMContentLoaded", () => {
  loadTimerSection();     // 타이머는 항상
  loadRunWithRank();      // 초기 화면은 랭크 페이지
});


function loadRunWithRank() {
  loadHTML("runContentSection", "../html/runWithRank.html", () => {
    document.querySelectorAll(".crewBtn").forEach(btn => {
      btn.addEventListener("click", () => {
        const crewName = btn.dataset.name;
        loadRunWithCrew(crewName);
      });
    });
  });
}

function loadRunWithCrew(crewName) {
  loadHTML("runContentSection", "../html/runWithCrew.html", () => {
    document.getElementById("crewName").textContent = crewName;
    document.getElementById("cancelBtn").addEventListener("click", () => {
      loadRunWithRank();
    });
  });
}