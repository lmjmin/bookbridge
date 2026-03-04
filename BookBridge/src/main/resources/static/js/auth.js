// 로그인 상태에 따라 헤더 버튼/텍스트를 동기화
// - 이 파일은 모듈로 불러도 되고, 일반 스크립트로 불러도 됨.

(function () {
  const $  = (s,r=document)=>r.querySelector(s);

  function getUser(){
    try{ return JSON.parse(localStorage.getItem('user')||'null'); }catch{ return null; }
  }

  function isLogged(){
    const u = getUser();
    return !!(u && (u.email || u.username || u.name || u.id));
  }

  function renderHeader(){
    const pcAuthBtns = $("#pc-auth-btns");
    const mobileAuthBtn = $("#mobile-auth-btn");
    const mypageLink = $("#mypage-link");

    if (isLogged()) {
      const u = getUser() || {};
      const idLike = u.name || u.username || u.email || "사용자";
      if (mobileAuthBtn) {
        mobileAuthBtn.textContent = "마이페이지";
        mobileAuthBtn.onclick = ()=> location.href="mypage.html";
      }
      if (pcAuthBtns) {
        pcAuthBtns.innerHTML = `
          <div class="user-info">
            <span class="user-id">${idLike}님</span>
            <span class="logout-btn" id="__logout_btn">로그아웃</span>
          </div>
          <button class="mypage-btn" id="__mypage_btn">마이페이지</button>
        `;
        pcAuthBtns.querySelector("#__logout_btn")?.addEventListener("click", logout);
        pcAuthBtns.querySelector("#__mypage_btn")?.addEventListener("click", ()=>location.href="mypage.html");
      }
      if (mypageLink) {
        mypageLink.onclick = (e)=>{ e.preventDefault(); location.href="mypage.html"; };
      }
    } else {
      if (mobileAuthBtn) {
        mobileAuthBtn.textContent = "로그인/회원가입";
        mobileAuthBtn.onclick = ()=> location.href="login.html";
      }
      if (pcAuthBtns) {
        pcAuthBtns.innerHTML = `<button class="login-btn" id="__login_btn">로그인/회원가입</button>`;
        pcAuthBtns.querySelector("#__login_btn")?.addEventListener("click", ()=>location.href="login.html");
      }
      if (mypageLink) {
        mypageLink.onclick = (e)=>{ e.preventDefault(); alert("로그인이 필요한 서비스입니다."); location.href="login.html"; };
      }
    }
  }

  function logout(){
    const keys = [
      'user','isLoggedIn','userId','username','name','email',
      'accessToken','token','jwt','Authorization'
    ];
    keys.forEach(k => localStorage.removeItem(k));
    alert("로그아웃 되었습니다.");
    location.href = "first.html";
  }

  document.addEventListener("DOMContentLoaded", renderHeader);
  window.AuthUI = { renderHeader, logout };
})();
