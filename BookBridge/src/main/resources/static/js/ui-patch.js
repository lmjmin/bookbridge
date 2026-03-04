// 로그인 버튼/찜 토글 통일 + 찜 아이템 로컬 보관(마이페이지 렌더용)
(function(){
  const $  = (s,r=document)=>r.querySelector(s);
  const $$ = (s,r=document)=>Array.from(r.querySelectorAll(s));

  /* ---------- 로그인 헤더 ---------- */
  function syncLoginKeys(){
    try{
      const u = JSON.parse(localStorage.getItem('user') || 'null');
      if (u) {
        const idLike = u.username || u.name || u.email || u.id || 'me';
        localStorage.setItem('isLoggedIn','true');
        localStorage.setItem('userId', String(idLike));
        if (u.email) localStorage.setItem('email', u.email);
        if (u.name ) localStorage.setItem('name',  u.name );
      }
    }catch{}
  }
  function isLogged(){
    try{ const u=JSON.parse(localStorage.getItem('user')||'null'); if(u&&(u.email||u.username||u.name||u.id)) return true; }catch{}
    return localStorage.getItem("isLoggedIn")==="true" || !!localStorage.getItem("userId");
  }
  function renderHeader(){
    const mobileAuthBtn = $("#mobile-auth-btn");
    const pcAuthBtns    = $("#pc-auth-btns");
    const mypageLink    = $("#mypage-link");

    if (isLogged()) {
      const idLike = localStorage.getItem("name") || localStorage.getItem("userId") || "사용자";
      if (mobileAuthBtn) { mobileAuthBtn.textContent="마이페이지"; mobileAuthBtn.onclick=()=>location.href="mypage.html"; }
      if (pcAuthBtns) {
        pcAuthBtns.innerHTML = `
          <div class="user-info">
            <span class="user-id">${idLike}님</span>
            <span class="logout-btn" id="__logout_btn">로그아웃</span>
          </div>
          <button class="mypage-btn" id="__mypage_btn">마이페이지</button>
        `;
        pcAuthBtns.querySelector("#__logout_btn")?.addEventListener("click", ()=>{
          ["user","isLoggedIn","userId","username","name","email","accessToken","token","jwt"].forEach(k=>localStorage.removeItem(k));
          alert("로그아웃 되었습니다.");
          renderHeader();
        });
        pcAuthBtns.querySelector("#__mypage_btn")?.addEventListener("click", ()=>location.href="mypage.html");
      }
      if (mypageLink) mypageLink.onclick = (e)=>{ e.preventDefault(); location.href="mypage.html"; };
    } else {
      if (mobileAuthBtn) { mobileAuthBtn.textContent="로그인/회원가입"; mobileAuthBtn.onclick=()=>location.href="login.html"; }
      if (pcAuthBtns) { pcAuthBtns.innerHTML=`<button class="login-btn" id="__login_btn">로그인/회원가입</button>`; pcAuthBtns.querySelector("#__login_btn")?.addEventListener("click", ()=>location.href="login.html"); }
      if (mypageLink) mypageLink.onclick = (e)=>{ e.preventDefault(); alert("로그인이 필요한 서비스입니다."); location.href="login.html"; };
    }
  }

  /* ---------- 찜 스토리지: 아이템 배열 ---------- */
  const LS_KEY = "bb_wishlist_items";

  function readWishItems(){
    try { const arr = JSON.parse(localStorage.getItem(LS_KEY) || "[]"); return Array.isArray(arr) ? arr : []; }
    catch { return []; }
  }
  function saveWishItems(arr){ localStorage.setItem(LS_KEY, JSON.stringify(arr||[])); }
  function hasWish(id){ return readWishItems().some(x => String(x.id) === String(id)); }

  function extractCardItem(btn){
    const card = btn.closest(".reco-card,[data-book-card]") || document;
    const a    = card.querySelector("a.cover-link") || card.querySelector("a[href*='detail.html']");
    const img  = card.querySelector("img");
    const titleEl = card.querySelector(".title") || card.querySelector("[data-title]");
    const priceEl = card.querySelector(".price");

    let id = btn.getAttribute("data-book-id") || btn.getAttribute("data-wish-id") || "";
    if (!id && a) { try { id = new URL(a.href, location.href).searchParams.get("id") || ""; } catch {} }

    return {
      id: String(id || ""),
      title: (titleEl?.textContent || "").trim(),
      price: (priceEl?.textContent || "").trim(),
      imageUrl: img?.src || "",
      href: a?.getAttribute("href") || (a ? a.href : "")
    };
  }

  function markButtons(){
    const items = readWishItems();
    const idset = new Set(items.map(x => String(x.id)));
    $$(".wish-btn, [data-wish-btn]").forEach(btn=>{
      const id = btn.getAttribute("data-book-id") || btn.getAttribute("data-wish-id") || extractCardItem(btn).id;
      const on = id && idset.has(String(id));
      btn.classList.toggle("wished", on);
      btn.setAttribute("aria-pressed", on ? "true" : "false");
      if (!btn.dataset.keepText) btn.textContent = on ? "♥" : "♡";
      if (id) btn.setAttribute("data-book-id", String(id));
      btn.setAttribute("data-wish-btn","");
      btn.title = on ? "찜 해제" : "찜하기";
    });
  }

  function toggleWish(btn){
    const items = readWishItems();
    const item = extractCardItem(btn);
    if (!item.id) return;

    const idx = items.findIndex(x => String(x.id) === String(item.id));
    if (idx >= 0) { items.splice(idx,1); } else { items.unshift(item); }
    saveWishItems(items);
    markButtons();
    document.dispatchEvent(new CustomEvent("wishlist:updated",{detail:{items}}));
  }

  function bindWish(){
    document.body.addEventListener("click",(e)=>{
      const btn = e.target.closest(".wish-btn, [data-wish-btn]");
      if (!btn) return;
      e.preventDefault();
      toggleWish(btn);
    });
  }

  document.addEventListener("DOMContentLoaded", ()=>{
    syncLoginKeys();
    renderHeader();
    bindWish();
    markButtons();
    document.addEventListener("wishlist:invalidate", markButtons);
  });
})();
