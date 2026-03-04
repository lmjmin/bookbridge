// 상세 페이지: 이미지 경로 보정 + 쪽지 버튼 보장
(function () {
  const API_BASE = (window.API_BASE ?? "").toString().replace(/\/+$/,"");
  const $ = (s,r=document)=>r.querySelector(s);

  function getId(){ try{ return new URL(location.href).searchParams.get("id"); }catch{ return null; } }
  function resolveImg(u){
    if(!u) return "/media/no-image.png";
    const s=String(u).trim();
    if(/^https?:\/\//i.test(s)||s.startsWith("data:")) return s;
    if(s.startsWith("/")) return API_BASE + s;
    return s;
  }

  async function fetchDetail(id){
    try{
      const r = await fetch(`${API_BASE}/api/books/${encodeURIComponent(id)}`, {cache:"no-store", credentials:"include"});
      if(!r.ok) throw new Error(r.status);
      return await r.json();
    }catch(e){ console.error(e); return null; }
  }

  function render(b){
    if(!b) return;
    const imgEl = $("#detail-thumb") || $("#product-image img") || $("#product-image");
    if (imgEl) {
      // 🔽🔽 여기만 변경
      const src = resolveImg(
        b.thumbnail || b.thumbUrl || b.imageUrl || b.coverImage || b.imagePath
      );
      // 🔼🔼
      if (imgEl.tagName==="IMG") imgEl.src = src; else imgEl.style.backgroundImage=`url(${src})`;
    }
    const title = b.title || b.bookTitle || "";
    ($("#detail-title")||document.querySelector(".product-title")||document.querySelector("h1"))?.textContent = title;
    ($("#detail-price")||document.querySelector(".price"))?.textContent = b.price!=null ? `${Number(b.price).toLocaleString()}원` : "";
    ($("#detail-isbn")||document.querySelector(".isbn"))?.textContent = b.isbn || "";

    const wishBtn = document.querySelector("[data-wish-btn]");
    if (wishBtn && !wishBtn.getAttribute("data-book-id")) {
      const id = b.id || b.bookId || b.isbn || "";
      wishBtn.setAttribute("data-book-id", String(id));
    }
  }

  function pickSeller(b){
    const s = b.seller || b.owner || b.member || b.user || {};
    return {
      id: s.id || s.userId || b.sellerId || "",
      name: s.name || s.username || b.sellerName || "",
      email: s.email || b.sellerEmail || ""
    };
  }

  function bindMessage(seller, book){
    const msgBtn =
      $("#btnMessage") ||
      Array.from(document.querySelectorAll("a,button")).find(el => /쪽지/.test(el.textContent||"")) ||
      null;
    if (!msgBtn) return;

    msgBtn.addEventListener("click",(e)=>{
      e.preventDefault();

      // 로그인 체크
      try{
        const u = JSON.parse(localStorage.getItem("user")||"null");
        if (!u && localStorage.getItem("isLoggedIn")!=="true") {
          alert("로그인이 필요합니다."); location.href="login.html"; return;
        }
      }catch{}

      const email = seller.email || "";
      const sellerId = seller.id || seller.userId || seller.username || seller.name || "seller";
      const listingId = book.id || book.bookId || "";

      if (email) {
        const subject = `[BookBridge] 문의: ${(book.title || "").trim()}`;
        location.href = `mailto:${encodeURIComponent(email)}?subject=${encodeURIComponent(subject)}`;
      } else {
        location.href = `dm.html?listingId=${encodeURIComponent(String(listingId))}&sellerId=${encodeURIComponent(String(sellerId))}`;
      }
    });
  }

  async function init(){
    const id = getId();
    if (!id) return;
    const data = await fetchDetail(id);
    if (!data) return;
    render(data);
    bindMessage(pickSeller(data), data);
    document.dispatchEvent(new Event("wishlist:invalidate"));
  }

  document.addEventListener("DOMContentLoaded", init);
})();
