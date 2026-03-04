// 빠른 카탈로그 엔드포인트 자동탐지 (병렬 + 타임아웃) — UI 불변
(function () {
  const BASE = (window.API_BASE || "");
  const H = (m, b) => ({
    method: m,
    credentials: "include",
    cache: "no-store",
    headers: m === "POST" ? { "content-type": "application/json" } : undefined,
    body: m === "POST" ? JSON.stringify(b || {}) : undefined,
  });

  const pick = (x) =>
    Array.isArray(x)
      ? x
      : Array.isArray(x?.content)
      ? x.content
      : Array.isArray(x?.data)
      ? x.data
      : Array.isArray(x?.list)
      ? x.list
      : [];

  // 후보 목록 (GET/POST 혼용)
  const CATALOG = [
    ["/api/books", "GET"],
    ["/api/books/latest", "GET"],
    ["/api/book/list", "GET"],
    ["/api/listings", "GET"],
    ["/books", "GET"],
    ["/list", "GET"],
    ["/api/books", "POST", { page: 0, size: 30 }],
    ["/api/books/list", "POST", { page: 0, size: 30 }],
    ["/api/listings", "POST", { page: 0, size: 30 }],
  ];

  const SEARCH = (q) => [
    ["/api/books/search", "POST", { q, page: 0, size: 30 }],
    ["/api/listings/search", "POST", { q, page: 0, size: 30 }],
    [`/api/book/search?q=${encodeURIComponent(q)}`, "GET"],
    [`/api/books/search?q=${encodeURIComponent(q)}`, "GET"],
    [`/api/search?q=${encodeURIComponent(q)}`, "GET"],
  ];

  function withTimeout(promise, ms = 800) {
    const ctrl = new AbortController();
    const t = setTimeout(() => ctrl.abort(), ms);
    return promise
      .then((v) => {
        clearTimeout(t);
        return v;
      })
      .catch((e) => {
        clearTimeout(t);
        throw e;
      });
  }

  async function tryOne(path, method, body) {
    const r = await withTimeout(fetch(BASE + path, H(method, body)));
    if (r.status === 200) {
      // OK
      const ct = (r.headers.get("content-type") || "").toLowerCase();
      if (ct.includes("json")) {
        const j = await r.json().catch(() => ({}));
        const list = pick(j);
        if (Array.isArray(list)) return { path, method, ok: true };
      } else {
        return { path, method, ok: true };
      }
    }
    // 400이지만 JSON 리스트가 있으면 역시 OK
    if (r.status === 400) {
      const j = await r.json().catch(() => ({}));
      const list = pick(j);
      if (Array.isArray(list)) return { path, method, ok: true };
    }
    throw new Error("not ok");
  }

  async function probeCatalogFast() {
    // 병렬 레이스: 가장 먼저 성공한 후보를 채택
    const racers = CATALOG.map(([p, m, b]) => tryOne(p, m, b));
    try {
      const ep = await Promise.any(racers);
      return ep; // {path, method}
    } catch {
      // 전부 실패 → 검색 전용 엔드포인트로 폴백
      return { path: "/api/books/search", method: "POST", isSearchOnly: true };
    }
  }

  async function callCatalog(ep) {
    if (ep.isSearchOnly) {
      try {
        const r = await withTimeout(
          fetch(BASE + ep.path, H("POST", { q: "", page: 0, size: 30 })),
          900
        );
        if (!r.ok) return [];
        const j = await r.json().catch(() => []);
        return pick(j);
      } catch {
        return [];
      }
    }
    try {
      const r = await withTimeout(
        fetch(BASE + ep.path, H(ep.method, ep.method === "POST" ? { page: 0, size: 30 } : undefined)),
        900
      );
      if (!r.ok) return [];
      const j = await r.json().catch(() => []);
      return pick(j);
    } catch {
      return [];
    }
  }

  async function searchFast(q) {
    const racers = SEARCH(q).map(([p, m, b]) =>
      withTimeout(fetch(BASE + p, H(m, b)), 900)
        .then((r) => (r.ok ? r.json() : Promise.reject()))
        .then((j) => pick(j))
    );
    try {
      return await Promise.any(racers);
    } catch {
      return [];
    }
  }

  // 전역 API
  window.$api = window.$api || {};
  let cachedEP = null;

  window.$api.catalog = {
    latest: async () => {
      if (!cachedEP) cachedEP = await probeCatalogFast(); // 최초 1회만
      const list = await callCatalog(cachedEP);
      return Array.isArray(list) ? list : [];
    },
    search: async (q) => {
      // 검색은 항상 빠른 병렬 레이스
      return searchFast((q || "").toString());
    },
  };
})();
