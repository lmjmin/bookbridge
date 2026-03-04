// src/main/resources/static/js/auth-adapter.js
// 네 기존 auth.js를 건드리지 않고, 모든 페이지에서 로그인 상태/헤더가 일관되게 보이도록 보완.
// - 다양한 로컬스토리지 키를 관대하게 읽어서 "로그인 여부" 판별
// - 헤더 버튼(모바일/PC, 마이페이지/로그아웃) 렌더 통일
// - 기존 window.Auth가 있으면 우선 위임, 없으면 이 어댑터가 대체

(function () {
  function $(sel, root=document){ return root.querySelector(sel); }

  // 다양한 저장 키에서 유저 정보를 "최대한" 끌어오기
  function probeUser() {
    // 1) 기존 auth.js가 제공하면 우선 사용
    const native = window.Auth;
    if (native && typeof native.getUser === "function") {
      try { const u = native.getUser(); if (u) return u; } catch {}
    }
    // 2) 흔히 쓰는 키들을 폭넓게 스캔
    try {
      const obj = JSON.parse(localStorage.getItem("user") || "null");
      if (obj && (obj.email || obj.username || obj.name || obj.id)) return obj;
    } catch {}
    const isLogged =
      localStorage.getItem("isLoggedIn") === "true" ||
      !!localStorage.getItem("accessToken") ||
      !!localStorage.getItem("token") ||
      !!localStorage.getItem("jwt");
    const idLike =
      localStorage.getItem("userId") ||
      localStorage.getItem("username") ||
      localStorage.getItem("email") || "";
    if (isLogged || idLike) {
      return {
        id: idLike || "me",
        name: localStorage.getItem("name") || localStorage.getItem("nickname") || idLike || "사용자",
        email: localStorage.getItem("email") || ""
      };
    }
    return null;
  }

  function hardLogout() {
    // 가능한 모든 키 정리 (프로젝트 혼재 대비)
    const keys = ["user","isLoggedIn","userId","username","name","email","accessToken","token","jwt"];
    for (const k of keys) try { localStorage.removeItem(k); } catch {}
  }

  function renderHeader() {
    // 기존 Auth.render가 있으면 먼저 호출(페이지 고유 렌더 존중)
    try { if (window.Auth && typeof window.Auth.render === "function") window.Auth.render(); } catch {}

    const u = probeUser();

    const mobileAuthBtn = $("#mobile-auth-btn");
    const pcAuthBtns    = $("#pc-auth-btns");
    const mypageLink    = $("#mypage-link");

    if (u) {
      if (mobileAuthBtn) {
        mobileAuthBtn.textContent = "마이페이지";
        mobileAuthBtn.onclick = () => (location.href = "mypage.html");
      }
      if (pcAuthBtns) {
        pcAuthBtns.innerHTML = `
          <div class="user-info">
            <span class="user-id">${(u.name || u.id)}님</span>
            <span class="logout-btn" id="__logout_btn">로그아웃</span>
          </div>
          <button class="mypage-btn" id="__mypage_btn">마이페이지</button>
        `;
        pcAuthBtns.querySelector("#__logout_btn")?.addEventListener("click", () => {
          try { if (window.Auth && typeof window.Auth.logout === "function") window.Auth.logout(); } catch {}
          hardLogout();
          alert("로그아웃 되었습니다.");
          renderHeader();
        });
        pcAuthBtns.querySelector("#__mypage_btn")?.addEventListener("click", () => {
          location.href = "mypage.html";
        });
      }
      if (mypageLink) {
        mypageLink.onclick = (e) => { e.preventDefault(); location.href = "mypage.html"; };
      }
    } else {
      if (mobileAuthBtn) {
        mobileAuthBtn.textContent = "로그인/회원가입";
        mobileAuthBtn.onclick = () => (location.href = "login.html");
      }
      if (pcAuthBtns) {
        pcAuthBtns.innerHTML = `<button class="login-btn" id="__login_btn">로그인/회원가입</button>`;
        pcAuthBtns.querySelector("#__login_btn")?.addEventListener("click", () => {
          location.href = "login.html";
        });
      }
      if (mypageLink) {
        mypageLink.onclick = (e) => {
          e.preventDefault();
          alert("로그인이 필요한 서비스입니다.");
          location.href = "login.html";
        };
      }
    }
  }

  // 최종 노출: 기존 Auth를 보존하며 보완
  window.Auth = {
    ...(window.Auth || {}),
    getUser: (window.Auth && window.Auth.getUser) || probeUser,
    render : renderHeader,
  };

  document.addEventListener("DOMContentLoaded", renderHeader);
})();