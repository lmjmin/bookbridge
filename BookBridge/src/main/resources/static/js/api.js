/* BookBridge 통합 API (404/405 흡수 + 프론트 검색 fallback) */
(function (g) {
  const BASE = (g.API_BASE || "").replace(/\/+$/, "");
  const J = (p) => BASE + p;
  const H = (m, b) => ({
    method: m,
    credentials: "include",
    cache: "no-store",
    headers: (m === "POST" || m === "PUT") ? { "content-type": "application/json" } : undefined,
    body: (m === "POST" || m === "PUT") ? JSON.stringify(b || {}) : undefined,
  });
  const okJson = async (r) => (r && r.ok) ? await r.json().catch(() => null) : null;
  const pickList = (x) =>
    Array.isArray(x) ? x
    : Array.isArray(x?.content) ? x.content
    : Array.isArray(x?.data) ? x.data
    : Array.isArray(x?.list) ? x.list
    : [];

  /* -------- 사용자 -------- */
  function getUser() {
    try { const u = JSON.parse(localStorage.getItem("user") || "null"); return u && typeof u === "object" ? u : null; }
    catch { return null; }
  }
  function userKey() {
    const u = getUser();
    return u?.email || u?.username || u?.id || u?.userId || null;
  }

  /* -------- 카탈로그: 최신/목록 자동탐지 -------- */
  let _CAT_EP = null;
  const CAND = [
    // GET
    ["/api/books", "GET"], ["/api/books/latest", "GET"], ["/api/book/list", "GET"],
    ["/api/books/all", "GET"], ["/api/book/all", "GET"], ["/api/list", "GET"], ["/api/listings", "GET"],
    ["/books", "GET"], ["/books/latest", "GET"],
    // POST
    ["/api/books", "POST"], ["/api/books/list", "POST"], ["/api/books/latest", "POST"],
    ["/api/listings", "POST"], ["/api/list", "POST"], ["/api/book/list", "POST"],
  ];
  async function probeCatalog() {
    if (_CAT_EP) return _CAT_EP;
    for (const [p, m] of CAND) {
      try {
        const b = m === "POST" ? { page: 0, size: 30 } : undefined;
        const r = await fetch(J(p), H(m, b));
        if (r.status === 200) return (_CAT_EP = { path: p, method: m });
        if (r.status === 400) {
          const js = await okJson(r);
          if (pickList(js).length) return (_CAT_EP = { path: p, method: m });
        }
        if (r.status === 405) {
          const allow = (r.headers.get("Allow") || "").toUpperCase();
          if (allow.includes("GET")) {
            const r2 = await fetch(J(p), H("GET"));
            if (r2.ok) return (_CAT_EP = { path: p, method: "GET" });
          }
          if (allow.includes("POST")) {
            const r2 = await fetch(J(p), H("POST", { page: 0, size: 30 }));
            if (r2.ok) return (_CAT_EP = { path: p, method: "POST" });
          }
        }
      } catch {}
    }
    // 서버에 목록 API가 없다면, 검색 API로만 동작
    return (_CAT_EP = { path: "/api/books/search", method: "POST", searchOnly: true });
  }
  async function catalogLatest() {
    const ep = await probeCatalog();
    if (ep.searchOnly) {
      const r = await fetch(J(ep.path), H("POST", { q: "", page: 0, size: 30 })).catch(() => null);
      return pickList(await okJson(r) || []);
    }
    const body = ep.method === "POST" ? { page: 0, size: 30 } : undefined;
    const r = await fetch(J(ep.path), H(ep.method, body)).catch(() => null);
    return pickList(await okJson(r) || []);
  }

  /* -------- 검색: 서버 엔드포인트 없으면 프론트 필터로 대체 -------- */
  function norm(s) { return (s ?? "").toString().toLowerCase(); }
  function korEngContains(hay, needle) {
    hay = norm(hay); needle = norm(needle);
    return hay.includes(needle);
  }
  async function serverSearch(q) {
    const C = [
      ["/api/books/search", "POST", { q, page: 0, size: 30 }],
      ["/api/listings/search", "POST", { q, page: 0, size: 30 }],
      ["/api/search", "POST", { q, page: 0, size: 30 }],
      [`/api/book/search?q=${encodeURIComponent(q)}`, "GET"],
      [`/api/books/search?q=${encodeURIComponent(q)}`, "GET"],
      [`/api/search?q=${encodeURIComponent(q)}`, "GET"],
      // 혹시 /books?q= 형태
      [`/books?q=${encodeURIComponent(q)}`, "GET"],
      [`/api/books?q=${encodeURIComponent(q)}`, "GET"],
    ];
    for (const [p, m, b] of C) {
      try {
        const r = await fetch(J(p), H(m, b));
        if (r.ok) return pickList(await okJson(r) || []);
      } catch {}
    }
    return null; // 서버 검색 불가
  }
  async function search(q) {
    q = (q || "").toString().trim();
    // 1) 서버 검색 시도
    const s = await serverSearch(q);
    if (s) return s;

    // 2) 서버 검색이 없으면, 최신/전체를 받아 프론트에서 제목/저자/출판사에 대해 부분일치 필터
    const base = await catalogLatest();
    if (!base.length) return [];
    return base.filter((b) => {
      const title = b.title || b.bookTitle || "";
      const author= (Array.isArray(b.authors) ? b.authors.join(",") : (b.author||""));
      const pub   = b.publisher || "";
      return korEngContains(title, q) || korEngContains(author, q) || korEngContains(pub, q);
    });
  }

  /* -------- 상세/유사/판매자 -------- */
  async function detail(id) {
    const eps = [
      `/api/books/${id}`, `/api/book/${id}`, `/books/${id}`, `/api/listings/${id}`,
      `/api/book/detail?id=${encodeURIComponent(id)}`
    ];
    for (const p of eps) {
      try { const r = await fetch(J(p), H("GET")); if (r.ok) return await okJson(r); } catch {}
    }
    return null;
  }
  async function similar(id, anchor) {
    const eps = [
      `/api/books/${id}/similar`, `/api/book/${id}/similar`,
      `/api/books/similar?id=${encodeURIComponent(id)}`,
      `/api/similar?id=${encodeURIComponent(id)}`
    ];
    for (const p of eps) {
      try { const r = await fetch(J(p), H("GET")); if (r.ok) return pickList(await okJson(r)||[]); } catch {}
    }
    // 서버 유사 API 없으면: 같은 저자/비슷한 제목 프론트 필터
    const base = await catalogLatest();
    if (!anchor) {
      anchor = await detail(id);
    }
    const t = (anchor?.title || "").split(/[ \-:_/]/)[0] || "";
    const a = Array.isArray(anchor?.authors) ? anchor.authors.join(",") : (anchor?.author || "");
    return base.filter(x => {
      const xt = x.title || "";
      const xa = Array.isArray(x.authors) ? x.authors.join(",") : (x.author || "");
      return (a && norm(xa).includes(norm(a))) || (t && norm(xt).includes(norm(t)));
    }).slice(0,12);
  }

// 내 판매글 불러오기
async function mySales() {
  const me = userKey(); // localStorage 에서 email 같은 거 가져오는 함수

  const C = [
    // ✅ 1순위: 우리가 실제로 만든 엔드포인트
    [me ? `/api/listings/mine?email=${encodeURIComponent(me)}` : `/api/listings/mine`, "GET"],

    // ⬇️ 혹시 팀장이 옛날 엔드포인트 살려놨을 가능성 대비 (있으면 쓰고, 없으면 그냥 에러 무시)
    [me ? `/api/books/mine?email=${encodeURIComponent(me)}` : `/api/books/mine`, "GET"],
    [me ? `/api/my/sales?me=${encodeURIComponent(me)}` : `/api/my/sales`, "GET"],

    // 최후 보정: 전체 목록 불러와서 프론트에서 필터
    [`/api/listings`, "GET"],
    [`/api/books`, "GET"],
  ];

  for (const [url, method] of C) {
    try {
      const res = await fetch(url, { method });
      if (!res.ok) {
        console.warn("mySales 실패:", url, res.status);
        continue;
      }
      const data = await res.json();
      console.log("MY SALES VIA", url, data);
      return data;
    } catch (e) {
      console.warn("mySales 예외:", url, e);
    }
  }
  return [];
}

      async function deleteListing(id) {
    try {
      const r = await fetch(J(`/api/listings/${encodeURIComponent(id)}`), H("DELETE"));
      // 200 / 204 / 404 면 일단 "요청은 성공"으로 본다
      if (r.ok || r.status === 204 || r.status === 404) return true;
    } catch (e) {
      console.warn("deleteListing error", e);
    }
    return false;
  }


  /* -------- 찜 -------- */
  function wishRead() {
    try { const a = JSON.parse(localStorage.getItem("bb_wishlist_items") || "[]"); return Array.isArray(a) ? a : []; }
    catch { return []; }
  }
  function wishWrite(a){ localStorage.setItem("bb_wishlist_items", JSON.stringify(a||[])); }
  async function wishToggle(book) {
    const body = { id: book.id || book.bookId, title: book.title, imageUrl: book.thumbnail || book.imageUrl };
    const eps = [
      ["/api/favorite", "POST"], ["/api/wishlist", "POST"],
      ["/api/favorite/remove", "POST"], ["/api/wishlist/remove", "POST"]
    ];
    for (const [p,m] of eps) {
      try { await fetch(J(p), H(m, body)); } catch {}
    }
    const arr = wishRead();
    const i = arr.findIndex(x => String(x.id) === String(body.id));
    if (i >= 0) arr.splice(i,1); else arr.push(body);
    wishWrite(arr);
    return arr;
  }

  /* -------- DM -------- */
  async function dmThreads() {
    const me = userKey();
    const C = [
      `/api/dm/threads?meId=${encodeURIComponent(me||"")}`,
      `/api/dm/threads?me=${encodeURIComponent(me||"")}`,
      `/api/dm/threads`
    ];
    for (const p of C) {
      try { const r = await fetch(J(p), H("GET")); if (r.ok) return pickList(await okJson(r)||[]); } catch {}
    }
    return [];
  }

  g.API = {
    latest: catalogLatest,
    search,
    detail, similar,
    mySales, deleteListing,
    wish: { read: wishRead, toggle: wishToggle },
    dm: { threads: dmThreads },
    user: { get: getUser, key: userKey }
  };
})(window);
