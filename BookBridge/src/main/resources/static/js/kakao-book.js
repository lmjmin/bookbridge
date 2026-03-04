(function (g) {
  const KAKAO_KEY = g.KAKAO_REST_API_KEY || g.KAKAO_REST_KEY || "";
  const API_BASE  = (g.API_BASE || "").replace(/\/+$/, "");
  const KAKAO_API = "https://dapi.kakao.com/v3/search/book";

  let currentCtl = null;

  async function xfetch(url, opt = {}) {
    const res = await fetch(url, {
      credentials: "omit",
      cache: "no-store",
      mode: "cors",
      ...opt,
      signal: opt.signal
    });
    if (!res.ok) throw new Error(res.status + " " + res.statusText);
    return res.json();
  }
  const esc = (s) => (s ?? "").toString().replace(/[&<>"']/g, m => ({
    "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"
  }[m]));
  const $ = (s,r=document)=>r.querySelector(s);

  async function proxyKakao(q, page = 1, size = 10, signal) {
    const enc = encodeURIComponent(q);
    const candidates = [
      `${API_BASE}/api/kakao/books?query=${enc}&page=${page}&size=${size}`,
      `${API_BASE}/api/kakao/proxy/books-proxy?query=${enc}&page=${page}&size=${size}`
    ];
    for (const url of candidates) {
      try {
        const j = await xfetch(url, { signal });
        if (j && (j.documents || j.content || j.data || j.list)) return j;
      } catch (_) {}
    }
    return { documents: [] };
  }

  async function kakaoDirect(q, page = 1, size = 10, signal) {
    if (!KAKAO_KEY) return { documents: [] };
    const u = new URL(KAKAO_API);
    u.searchParams.set("query", q);
    u.searchParams.set("page", String(page));
    u.searchParams.set("size", String(size));
    u.searchParams.set("target", "title");
    return xfetch(u.toString(), {
      headers: { Authorization: "KakaoAK " + KAKAO_KEY },
      signal
    });
  }

  function normalize(doc) {
    if (!doc) return [];
    if (Array.isArray(doc)) return doc;
    const arr = doc.documents || doc.content || doc.data || doc.list || [];
    return (arr || []).map(v => ({
      isbn: ((v.isbn || v.ISBN || "").toString().split(" ").pop()),
      title: v.title || v.bookTitle || "",
      author: (Array.isArray(v.authors) ? v.authors[0] : (v.author || "")) || "",
      thumbnail: v.thumbnail || v.image || v.imageUrl || "",
      publisher: v.publisher || "",
      price: v.price || v.sale_price || v.salePrice || null
    }));
  }

  async function search(query, { page = 1, size = 10 } = {}) {
    if (currentCtl) { try { currentCtl.abort(); } catch {} }
    currentCtl = new AbortController();
    const { signal } = currentCtl;

    try {
      const prox = await proxyKakao(query, page, size, signal);
      const l1 = normalize(prox);
      if (l1.length) return l1;
    } catch {}
    try {
      const dir = await kakaoDirect(query, page, size, signal);
      return normalize(dir);
    } catch { return []; }
  }

  // SELL 페이지 바인딩
  function bindSell() {
    const titleInput = $("#title");
    const listBox    = $("#titleSearchList");
    if (!titleInput || !listBox) return false;

    let to = null;
    function qSearch() {
      const q = (titleInput.value || "").trim();
      if (!q) { listBox.innerHTML = ""; return; }
      search(q, { page: 1, size: 10 }).then((items)=>{
        if (!items.length) { listBox.innerHTML = ""; return; }
        listBox.innerHTML = items.map(it => {
          const sub = [it.author || "", it.publisher || ""].filter(Boolean).join(" · ");
          const img = it.thumbnail || "media/no-image.png";
          return `
            <button type="button" class="k-item"
                    data-isbn="${esc(it.isbn||"")}"
                    data-title="${esc(it.title||"")}"
                    data-author="${esc(it.author||"")}"
                    data-publisher="${esc(it.publisher||"")}"
                    data-thumbnail="${esc(img)}">
              <img src="${img}" alt="">
              <span class="meta">
                <span class="title">${esc(it.title||"")}</span>
                <span class="sub">${esc(sub)}</span>
              </span>
            </button>`;
        }).join("");

        listBox.querySelectorAll(".k-item").forEach(btn=>{
          btn.addEventListener("click", ()=>{
            const d = btn.dataset;
            let el;

            el = $("#title");      if (el) el.value = d.title || "";
            el = $("#author");     if (el) el.value = d.author || "";
            el = $("#publisher");  if (el) el.value = d.publisher || "";
            el = $("#isbn");       if (el) el.value = (d.isbn || "").replace(/[^0-9Xx]/g,"");

            const prev = $("#previewImg");
            if (prev) prev.src = d.thumbnail || "media/no-image.png";

            listBox.innerHTML = "";
          });
        });
      }).catch(()=>{ listBox.innerHTML=""; });
    }

    titleInput.setAttribute("autocomplete","off");
    titleInput.addEventListener("input", ()=>{ clearTimeout(to); to = setTimeout(qSearch, 220); });
    titleInput.addEventListener("focus", ()=>{ if (listBox.innerHTML.trim()) listBox.style.display = "block"; });
    titleInput.addEventListener("blur",  ()=>{ setTimeout(()=>{ listBox.style.display = ""; }, 120); });

    return true;
  }

  // 홈/검색 페이지 바인딩(지금은 사용 안 함; first.html은 자체 검색 사용)
  function bindHome() {
    const inp = document.getElementById("searchInput");
    const btn = document.getElementById("btnSearch");
    if (!inp || !btn) return false;
    // 사용 안 함
    return true;
  }

  // 🔴 여기만 수정: 판매글 페이지에서만 Kakao 자동완성 활성화
  document.addEventListener("DOMContentLoaded", () => {
    bindSell();
  });

  g.KakaoBook = { search };
})(window);
