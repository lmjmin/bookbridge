// 프로토콜/포트 혼합 접속 안정화: HTTPS 우선, 아니면 localhost:8080
(function () {
  const { protocol, hostname, port } = window.location;

  // 1) API 서버 기준값 계산
  // - https로 접속 중이면 그대로 사용
  // - http인데 localhost면 그대로 8080 사용(개발용)
  // - 그 외(http로 원격 접속)는 https로 승격 시도
  const isLocal = hostname === "localhost" || hostname === "127.0.0.1";
  const wantsHttps = (protocol === "https:") || (!isLocal); // 원격은 https 강제
  const baseProto = wantsHttps ? "https:" : "http:";
  const baseHost = `${hostname}:8080`;

  // 2) API_BASE 설정
  window.API_BASE = `${baseProto}//${baseHost}`.replace(/\/+$/, "");

  // 3) 절대 URL 생성 유틸
  window.makeUrl = function (path) {
    return new URL(path, window.API_BASE).toString();
  };

  // 4) (선택) 혼합접속 자동 리디렉션: http 원격으로 열었으면 같은 호스트 https로 재진입
  try {
    if (!isLocal && window.location.protocol === "http:") {
      const httpsUrl = "https://" + window.location.host + window.location.pathname + window.location.search + window.location.hash;
      // 정적 파일도 https로 다시 열어 브라우저 보안 컨텍스트 충족
      window.location.replace(httpsUrl);
    }
  } catch (_) {}
})();
