(function(){
  function el(id){ return document.getElementById(id); }
  async function loadRating(){
    const box = document.getElementById("profile-rating");
    if(!box) return;
    const userId = box.getAttribute("data-user-id");
    if(!userId) return;
    try{
      const j = await window.api.profile.rating(userId);
      if(j?.ok){
        el("rating-avg").textContent = Number(j.avg || 0).toFixed(1);
        el("rating-count").textContent = j.count ?? 0;
      }
    }catch(e){}
  }
  loadRating();
})();
