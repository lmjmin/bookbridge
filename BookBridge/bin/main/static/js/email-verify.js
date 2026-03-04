(function(){
  function el(id){ return document.getElementById(id); }

  async function sendCode(){
    const email = el("reg-email")?.value?.trim();
    if(!email){ alert("이메일을 입력하세요"); return; }
    try{
      const out = await window.api.email.send(email);
      el("email-verify-msg").textContent = out.message || (out.ok ? "전송됨" : "실패");
    }catch(e){
      el("email-verify-msg").textContent = "메일 전송 실패";
    }
  }

  async function verifyCode(){
    const email = el("reg-email")?.value?.trim();
    const code  = el("reg-code")?.value?.trim();
    if(!email || !code){ alert("이메일/코드를 입력하세요"); return; }
    try{
      const out = await window.api.email.verify(email, code);
      el("email-verify-msg").textContent = out.message || (out.ok ? "인증 완료" : "실패");
      if(out.ok){ localStorage.setItem("emailVerified","1"); }
    }catch(e){
      el("email-verify-msg").textContent = "인증 실패";
    }
  }

  document.getElementById("btn-send-code")?.addEventListener("click", sendCode);
  document.getElementById("btn-verify-code")?.addEventListener("click", verifyCode);
})();
