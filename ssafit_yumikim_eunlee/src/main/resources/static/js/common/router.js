function loadPage(page) {
  fetch(`/pages/${page}.html`)
    .then(res => res.text())
    .then(html => {
      document.getElementById('main-content').innerHTML = html;
      const script = document.createElement('script');
      script.src = `/js/page/${page}.js`;
      script.onload = () => {
        if (typeof window[`initPage${page}`] === 'function') {
          window[`initPage${page}`]();
        }
      };
      document.body.appendChild(script);
    });
}

// 첫 진입 시 A 페이지
window.addEventListener('DOMContentLoaded', () => {
  loadPage('pageA');
});
