(function(){
  auth.applyNav();

  const listEl = document.getElementById("list");
  const emptyEl = document.getElementById("empty");
  const recoEl = document.getElementById("reco");

  const iconHeart = (on)=> on
    ? `<svg width="20" height="20" viewBox="0 0 24 24" fill="#fb7185"><path d="M12 21s-7.053-4.412-9.428-8.59C.842 9.31 2.3 6 5.5 6c1.85 0 3.09 1.06 3.87 2.2C10.41 7.06 11.65 6 13.5 6c3.2 0 4.658 3.31 2.928 6.41C19.053 16.588 12 21 12 21Z"/></svg>`
    : `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#111"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 1 0-7.78 7.78L12 21l8.84-8.61a5.5 5.5 0 0 0 0-7.78Z"/></svg>`;

  function card(book){
    const wished = !!book.wished;
    return `
      <article class="book-card" data-id="${book.id}">
        <button class="wish-btn" aria-label="찜" data-on="${wished}">${iconHeart(wished)}</button>
        <img class="thumb" src="${book.thumbUrl || '/media/noimg.png'}" alt="">
        <div class="title">${book.title||'제목 없음'}</div>
        <div class="meta">${book.author||''} · ${book.publisher||''} · ${book.isbn||''}</div>
        <div class="price">${(book.price!=null? book.price.toLocaleString()+'원' : '')}</div>
      </article>
    `;
  }

  async function load(){
    try{
      const books = await bbApi.listBooks("size=24");
      if(!books || books.length===0){
        emptyEl.style.display="block";
        return;
      }
      listEl.innerHTML = books.map(card).join("");

      if(auth.isLogin()){
        // 개인화 추천 띠 및 하이라이트
        try{
          const rec = await bbApi.reco();
          if(Array.isArray(rec) && rec.length){
            recoEl.style.display="block";
          }
        }catch(e){}
      }
    }catch(e){
      emptyEl.style.display="block";
      console.error(e);
    }
  }

  listEl.addEventListener("click", async (e)=>{
    const btn = e.target.closest(".wish-btn");
    if(!btn) return;
    if(!auth.isLogin()) return alert("로그인 후 이용해 주세요.");
    const cardEl = btn.closest(".book-card");
    const id = cardEl?.dataset?.id;
    if(!id) return;
    try{
      const r = await bbApi.toggleWish(Number(id));
      const on = !!r?.on;
      btn.setAttribute("data-on", on);
      btn.innerHTML = iconHeart(on);
    }catch(err){
      alert("오류: " + (err?.message||""));
    }
  });

  load();
})();