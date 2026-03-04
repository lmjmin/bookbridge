/* =========================================================================
   session.js  —  로그인 상태 관리(로컬스토리지) + 헤더 UI 토글 + 로그아웃
   -------------------------------------------------------------------------
   - UI 구조는 절대 변경하지 않음. "있으면" 토글, "없으면" 무시 (안전)
   - localStorage.user 가 존재하면 로그인 상태로 간주
   - 자동 인식 대상(있으면만 동작):
       #btnLoginLink, #btnSignupLink, #btnLogout, #userGreeting, #userMenu,
       [data-auth="guest"], [data-auth="user"]
   - 보호 라우팅: body나 main에 data-require-auth="true" 있으면
     로그인 없을 때 login.html로 보냄
   ========================================================================= */

(function(){
  const getUser = () => {
    try { return JSON.parse(localStorage.getItem('user') || 'null'); }
    catch { return null; }
  };

  const setText = (el, text) => { if (el) el.textContent = text; };
  const show = (el) => { if (el) el.style.display = ''; };
  const hide = (el) => { if (el) el.style.display = 'none'; };

  function toggleAuthUI(){
    const u = getUser();

    const btnLogin   = document.querySelector('#btnLoginLink');
    const btnSignup  = document.querySelector('#btnSignupLink');
    const btnLogout  = document.querySelector('#btnLogout');
    const userGreet  = document.querySelector('#userGreeting');
    const userMenu   = document.querySelector('#userMenu');

    const guestOnly = document.querySelectorAll('[data-auth="guest"]');
    const userOnly  = document.querySelectorAll('[data-auth="user"]');

    if (u) {
      guestOnly.forEach(hide);
      userOnly.forEach(show);
      hide(btnLogin);
      hide(btnSignup);
      show(btnLogout);
      show(userMenu);

      const name = u.name || u.username || u.email || '사용자';
      setText(userGreet, `${name} 님 안녕하세요`);
    } else {
      guestOnly.forEach(show);
      userOnly.forEach(hide);
      show(btnLogin);
      show(btnSignup);
      hide(btnLogout);
      hide(userMenu);
      setText(userGreet, '');
    }
  }

  function setupLogout(){
    const btnLogout = document.querySelector('#btnLogout');
    if (!btnLogout) return;
    btnLogout.addEventListener('click', (e)=>{
      e.preventDefault();
      try { localStorage.removeItem('user'); } catch {}
      toggleAuthUI();
      // location.href = 'first.html'; // 원하면 홈 이동
    });
  }

  function protectIfNeeded(){
    const needAuth = document.querySelector('[data-require-auth="true"]');
    if (!needAuth) return;
    if (!getUser()){
      alert('로그인이 필요합니다.');
      location.href = 'login.html';
    }
  }

  document.addEventListener('DOMContentLoaded', ()=>{
    toggleAuthUI();
    setupLogout();
    protectIfNeeded();
  });

  window.AuthUI = { refresh: toggleAuthUI };
})();