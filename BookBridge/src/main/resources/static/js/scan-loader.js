// scan-loader.js
(function(){
  let loading = null;

  async function loadScript(src){
    return new Promise((resolve, reject)=>{
      const s = document.createElement('script');
      s.src = src;
      s.async = true;
      s.onload = resolve;
      s.onerror = reject;
      document.head.appendChild(s);
    });
  }

  window.ensureScanLibLoaded = async function(){
    if (window.Html5Qrcode) return;
    if (!loading){
      // html5-qrcode 최신 안정 버전만 로드 (중복 경고 제거)
      loading = (async ()=>{
        await loadScript("https://unpkg.com/html5-qrcode@2.3.8/html5-qrcode.min.js");
      })().catch(e=>{ console.error("scan lib load failed", e); throw e; });
    }
    return loading;
  };
})();
