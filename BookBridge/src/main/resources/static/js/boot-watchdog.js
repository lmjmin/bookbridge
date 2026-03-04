// 부팅 워치독: 초기 스크립트/네트워크가 느려도 웰컴 오버레이 강제 해제
(function () {
  var hidden = false;

  function hideWelcome() {
    if (hidden) return;
    hidden = true;
    try {
      var ws = document.getElementById('welcome-screen');
      if (ws) {
        ws.style.transition = 'opacity .35s ease';
        ws.style.opacity = '0';
        setTimeout(function () {
          try { ws.style.display = 'none'; } catch (_) {}
        }, 400);
      }
      // 혹시 오버레이가 포인터 막으면 해제
      document.body.style.pointerEvents = 'auto';
    } catch (_) {}
  }

  // 2.5초 후 강제 해제 (네트워크/스크립트 지연 방지)
  setTimeout(hideWelcome, 2500);

  // DOM 준비되면 한 번 더 시도
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', hideWelcome, { once: true });
  } else {
    // 이미 로드됨
    setTimeout(hideWelcome, 0);
  }

  // 앱이 정상 로드됐을 때에도 안전하게 해제
  window.addEventListener('load', function () {
    setTimeout(hideWelcome, 0);
  });
})();
