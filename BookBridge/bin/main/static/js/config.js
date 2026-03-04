// 자동 감지: 8080이면 상대경로, 그 외(3000/5173 등)는 8080 절대경로
(function () {
  const { protocol, hostname, port, origin } = window.location;
  const needAbsolute = !(port === "8080" || port === "" || protocol === "file:");
  const absolute8080 = `${protocol}//${hostname}:8080`;

  // 끝 슬래시 제거해서 전역 노출
  window.API_BASE = (needAbsolute ? absolute8080 : "").replace(/\/+$/, "");

  // 안전 URL 생성기 (선택 사용)
  window.makeUrl = function (path) {
    return new URL(path, window.API_BASE || origin).toString();
  };

  // Kakao REST API 키 (프론트 노출 주의)
  window.KAKAO_REST_API_KEY = "90a311f2c60acb7d0df2336171942b23";
})();
