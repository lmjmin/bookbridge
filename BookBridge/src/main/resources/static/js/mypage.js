// 서버 위시리스트가 비어도 로컬 찜으로 렌더
(function(){
   const API_BASE = (window.API_BASE ?? "").toString().replace(/\/+$/,"");
  const LS_KEY = "bb_wishlist_items";
  const $ = (s,r=document)=>r.querySelector(s);

  function readLocal(){
    try { const a = JSON.parse(localStorage.getItem(LS_KEY)||"[]"); return Array.isArray(a)? a:[]; }
    catch { return []; }
  }

  function card(item){
    const id = String(item.id||"");
    const img = item.imageUrl || "/media/no-image.png";
    const href = item.href && !item.href.startsWith("http") ? item.href : (`detail.html?id=${encodeURIComponent(id)}`);
    const title = item.title || "";
    const price = item.price || "";
    return `
      <div class="reco-card" data-book-card>
        <button class="wish-btn wished" data-wish-btn data-book-id="${id}" aria-pressed="true">♥</button>
        <a class="cover-link" href="${href}">
          <img src="${img}" alt="${title||'책 이미지'}" />
          <div class="meta">
            <div class="title">${title}</div>
            <div class="price">${price}</div>
          </div>
        </a>
      </div>`;
  }

  async function fromServer(){
    try{
      const r = await fetch(`${API_BASE}/api/wishlist`, {credentials:"include", cache:"no-store"});
      if (!r.ok) return [];
      const data = await r.json();
      if (!Array.isArray(data) || data.length===0) return [];
      return data.map(d=>({
        id: d.id || d.bookId || d.listingId || d.isbn || "",
        title: d.title || d.bookTitle || "",
        price: d.price ? `${Number(d.price).toLocaleString()}원` : "",
        imageUrl: d.imageUrl || d.thumbnail || "",
        href: `detail.html?id=${encodeURIComponent(d.id || d.bookId || d.isbn || "")}`
      }));
    }catch(_){ return []; }
  }

  async function renderWish(){
    const box = $("#wishlistList");
    const empty = $("#wishlistEmpty");
    if (!box) return;

    let items = await fromServer();
    if (items.length === 0) items = readLocal();

    if (items.length === 0) {
      if (empty) empty.style.display="block";
      box.innerHTML = "";
      return;
    }
    if (empty) empty.style.display="none";
    box.innerHTML = items.map(card).join("");
    document.dispatchEvent(new Event("wishlist:invalidate"));
  }

  document.addEventListener("DOMContentLoaded", renderWish);
})();