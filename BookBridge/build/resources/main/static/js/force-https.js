// src/main/resources/static/js/force-https.js

(function () {
  try {
    const host = window.location.host;

    // 로컬 개발 환경(8080, 127.0.0.1 등)에서는 아무것도 안 함
    const isLocal =
      host.startsWith('localhost') ||
      host.startsWith('127.0.0.1');

    if (isLocal) {
      // console.log('force-https: local 환경, 무시');
      return;
    }

    const isHttps = window.location.protocol === 'https:';

    if (!isHttps) {
      const target =
        'https://' +
        host +
        window.location.pathname +
        window.location.search +
        window.location.hash;

      // http → https 강제 이동
      window.location.replace(target);
    }
  } catch (e) {
    console.warn('force-https.js error', e);
  }
})();
