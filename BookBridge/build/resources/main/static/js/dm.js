(function(g){
  const KAKAO_KEY = g.KAKAO_REST_API_KEY || "90a311f2c60acb7d0df2336171942b23";
  const API = "https://dapi.kakao.com/v3/search/book";
  async function kakaoSearch(q, page=1, size=10){
    const u = new URL(API);
    u.searchParams.set("query", q);
    u.searchParams.set("page", String(page));
    u.searchParams.set("size", String(size));
    u.searchParams.set("target", "title");
    const r = await fetch(u.toString(), { headers:{ Authorization:"KakaoAK "+KAKAO_KEY }});
    if(!r.ok) throw new Error("kakao search failed");
    const j = await r.json();
    return Array.isArray(j?.documents) ? j.documents : [];
  }
  const toSimple = (d)=>({
    title: d.title,
    authors: (d.authors||[]).join(", "),
    isbn: (d.isbn||"").split(" ").pop(),
    publisher: d.publisher||"",
    thumbnail: d.thumbnail||"",
    price: d.price||""
  });
  function bind(){
    const input = document.querySelector("#bookTitle") || document.querySelector("#title")
               || document.querySelector("[name=title]") || document.querySelector("[data-sell-title]");
    const list  = document.querySelector("#kakaoResults") || document.querySelector("#titleSearchList")
               || document.querySelector("#searchResults") || document.querySelector("[data-kakao-results]");
    if(!input || !list) return;

    let t=null;
    input.addEventListener("input", ()=>{
      const q=input.value.trim(); clearTimeout(t);
      if(!q){ list.innerHTML=""; return; }
      t=setTimeout(async ()=>{
        try{
          const docs=(await kakaoSearch(q,1,10)).map(toSimple);
          list.innerHTML = docs.map(d=>`
            <button type="button" class="k-item" data-isbn="${d.isbn}"
              data-title="${d.title.replace(/"/g,'&quot;')}"
              data-authors="${d.authors.replace(/"/g,'&quot;')}"
              data-publisher="${d.publisher.replace(/"/g,'&quot;')}"
              data-thumb="${d.thumbnail}">
              <img src="${d.thumbnail||'media/no-image.png'}" alt=""/>
              <div class="meta"><div class="title">${d.title}</div><div class="sub">${d.authors} · ${d.publisher}</div></div>
            </button>`).join("") || "<div class='hint'>검색 결과가 없습니다.</div>";
        }catch(e){ list.innerHTML="<div class='hint'>검색 실패</div>"; }
      },220);
    });

    list.addEventListener("click",(e)=>{
      const it=e.target.closest(".k-item"); if(!it) return;
      const set=(sel,val)=>{ const el=document.querySelector(sel); if(el) el.value=val; };
      set("#isbn", it.dataset.isbn||"");
      set("#title", it.dataset.title||"");
      set("#bookTitle", it.dataset.title||"");
      set("#author", it.dataset.authors||"");
      set("#publisher", it.dataset.publisher||"");
      const prev=document.querySelector("#previewImg")||document.querySelector("[data-preview]");
      if(prev) prev.src = it.dataset.thumb || "media/no-image.png";
      list.innerHTML="";
    });
  }
  document.addEventListener("DOMContentLoaded", bind);
  g.KakaoBook = { search:kakaoSearch, toSimple };
})(window);
