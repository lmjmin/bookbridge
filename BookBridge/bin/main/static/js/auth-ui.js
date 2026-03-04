// API_BASE 기본값
window.API_BASE = (window.API_BASE ?? "").toString().replace(/\/+$/,"");

// 세션 사용자 조회
async function fetchMe(){
  try{
    const r = await fetch(`${API_BASE}/api/auth/me`, { credentials: "same-origin" });
    const j = await r.json().catch(()=>({}));
    return j && j.user ? j.user : null;
  }catch{ return null; }
}

// 로그인 요청 (id/email/username 아무 키나)
async function loginRequest(id, password){
  const body = { id, password }; // 서버에서 @JsonAlias로 모두 수용
  const r = await fetch(`${API_BASE}/api/auth/login`, {
    method: "POST",
    headers: {"Content-Type":"application/json"},
    body: JSON.stringify(body),
    credentials: "same-origin"     // ★ 세션 쿠키 필수
  });
  const j = await r.json().catch(()=>({}));
  if(j.ok && j.user){
    localStorage.setItem("user", JSON.stringify(j.user));  // 클라이언트 캐시
  }
  return j;
}

// 로그아웃
async function logoutRequest(){
  try{
    await fetch(`${API_BASE}/api/auth/logout`, {
      method: "POST",
      credentials: "same-origin"
    });
  }finally{
    localStorage.removeItem("user");
  }
}

// 헤더 영역 토글
async function hydrateAuthUI(){
  const authBtn   = document.getElementById("btn-auth");      // "로그인/회원가입"
  const userBox   = document.getElementById("auth-user");     // 로그인 후 영역
  const userName  = document.getElementById("auth-username"); // 이름 출력
  const logoutBtn = document.getElementById("btn-logout");

  // 우선 localStorage, 안 되면 /me
  let u = null;
  try{ u = JSON.parse(localStorage.getItem("user")||"null"); }catch{}
  if(!u) u = await fetchMe();

  if(u){
    authBtn?.classList.add("hidden");
    if(userBox){ userBox.classList.remove("hidden"); }
    if(userName){ userName.textContent = u.name || u.username || u.email || "User"; }
  }else{
    userBox?.classList.add("hidden");
    authBtn?.classList.remove("hidden");
  }

  // 로그아웃 클릭
  if(logoutBtn){
    logoutBtn.onclick = async ()=>{
      await logoutRequest();
      // 즉시 UI 갱신
      authBtn?.classList.remove("hidden");
      userBox?.classList.add("hidden");
      location.reload();
    };
  }
}

// 초기화
document.addEventListener("DOMContentLoaded", hydrateAuthUI);
