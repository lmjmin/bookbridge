(function () {
  const API = window.api;
  const $  = (sel, root=document)=> root.querySelector(sel);
  const esc = (s)=> (s ?? "").toString().replace(/[&<>]/g, m=>({"&":"&amp;","<":"&lt;",">":"&gt;"}[m]));

  const baseDir = location.pathname.replace(/\/[^\/]*$/, "/");
  const detailHref = (id) => baseDir + "detail.html?id=" + encodeURIComponent(id ?? "");

  const BOX_IDS = ["#search-results","#list-results","#top-list","#book-list","#cards"];
  function pickBox(){ for(const id of BOX_IDS){ const el=$(id); if(el) return el; } return document.body; }

  function bookCard(b){
    const thumb=b.thumbnail||b.imageUrl||"";
    const id=b.id??b.bookId??b.book?.id;
    const title=esc(b.title||b.book?.title||"");
    const author=esc(b.author||b.book?.author||"");
    const publisher=esc(b.publisher||b.book?.publisher||"");
    const price=b.price??b.book?.price;

    return `
    <a class="card" href="${detailHref(id)}">
      <div class="thumb">${thumb?`<img src="${esc(thumb)}" alt="">`:`<div class="noimg">NO IMAGE</div>`}</div>
      <div class="meta">
        <div class="t">${title}</div>
        <div class="s">${author} · ${publisher}</div>
        ${price!=null?`<div class="p">${price.toLocaleString()}원</div>`:""}
      </div>
    </a>`;
  }

  function renderList(list){
    const box = pickBox();
    box.innerHTML = (list||[]).map(bookCard).join("") || `<div class="empty">검색 결과가 없습니다.</div>`;
  }

  async function doSearch(q){
    const pageResp = await API.books.search({ q, page:0, size:30 });
    const list = pageResp?.content || [];
    renderList(list);
  }

  function bind(){
    const inp = $("#searchInput") || $("#q") || $("input[type='search']") || $("[data-search-input]");
    const btn = $(".search-button") || $("#btnSearch") || $("[data-search-btn]");
    if (inp) inp.addEventListener("keydown",(e)=>{ if(e.key==="Enter"){ e.preventDefault(); const kw=(inp.value||"").trim(); if(kw) doSearch(kw); }});
    if (btn) btn.addEventListener("click",(e)=>{ e.preventDefault(); const kw=(inp && inp.value || "").trim(); if(kw) doSearch(kw); });

    try{
      const u=new URL(location.href); const q=u.searchParams.get("q");
      if(q){ if(inp) inp.value=q; doSearch(q); }
    }catch{}
  }

  window.SearchRender = { renderList, doSearch };
  document.addEventListener("DOMContentLoaded", bind);
})();
