(function(){
  function el(id){ return document.getElementById(id); }
  async function loadSummary(){
    const sec = document.getElementById("mypage-summary");
    if(!sec) return;
    const userId = sec.getAttribute("data-user-id");
    if(!userId) return;
    try{
      const j = await window.api.mypage.summary(userId);
      if(j?.ok){
        el("sum-sales").textContent   = j.salesCount   ?? 0;
        el("sum-purchases").textContent = j.purchaseCount ?? 0;
        el("sum-wish").textContent    = j.wishlistCount?? 0;
        el("sum-avg").textContent     = Number(j.ratingAvg || 0).toFixed(1);
        el("sum-rcount").textContent  = j.ratingCount  ?? 0;
      }
    }catch(e){}
  }
  loadSummary();
})();
