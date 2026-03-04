(function () {
  const LS_KEY = "bookbridge_recent_views";
  const MAX_RECENT = 12;

  const API_BASE = (window.API_BASE ?? "").toString().replace(/\/+$/, "");
  const KAKAO_KEY = window.KAKAO_REST_KEY || window.KAKAO_REST_API_KEY || "";

  const uniqBy = (arr, key) => {
    const seen = new Set(),
      out = [];
    for (const x of arr) {
      const k = typeof key === "function" ? key(x) : x[key];
      if (seen.has(k)) continue;
      seen.add(k);
      out.push(x);
    }
    return out;
  };

  function getRecent() {
    try {
      const raw = localStorage.getItem(LS_KEY);
      if (!raw) return [];
      const parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }

  function saveRecent(list) {
    try {
      localStorage.setItem(LS_KEY, JSON.stringify(list.slice(0, MAX_RECENT)));
    } catch {}
  }

  function trackView({ isbn, title, author }) {
    if (!isbn && !title) return;
    const recent = getRecent();
    const now = {
      isbn: (isbn || "").trim(),
      title: (title || "").trim(),
      author: (author || "").trim(),
      ts: Date.now(),
    };
    const merged = [now, ...recent].filter((x) => x && (x.isbn || x.title));
    const dedup = uniqBy(
      merged.map((x) => ({ ...x, _k: x.isbn || x.title })),
      "_k"
    );
    saveRecent(dedup);
  }

  const esc = (s) =>
    (s ?? "").toString().replace(/[&<>"]/g, (m) => {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[m];
    });

  function resolveImg(u) {
    if (!u) return (API_BASE || "") + "/media/no-image.png";
    const s = String(u).trim();
    if (/^https?:\/\//i.test(s) || s.startsWith("data:")) return s;
    if (s.startsWith("/")) return API_BASE + s;
    return s;
  }

  async function kakaoSearch({ query, isbn, target = "title", size = 20 }) {
    if (!KAKAO_KEY) return [];
    const q = (query || "").toString().trim();
    const i = (isbn || "").toString().replace(/[^0-9Xx]/g, "");
    if (!q && !i) return [];

    const url = new URL("https://dapi.kakao.com/v3/search/book");
    const searchQuery = q || i;
    url.searchParams.set("query", searchQuery);
    url.searchParams.set("target", i ? "isbn" : target);
    url.searchParams.set("page", "1");
    url.searchParams.set("size", String(size));

    let res;
    try {
      res = await fetch(url.toString(), {
        headers: { Authorization: "KakaoAK " + KAKAO_KEY },
        cache: "no-store",
      });
    } catch {
      return [];
    }
    if (!res.ok) return [];
    let data;
    try {
      data = await res.json();
    } catch {
      return [];
    }
    const docs = data.documents || [];
    return docs.map((b) => ({
      isbn: (b.isbn || "").split(" ").pop(),
      title: b.title,
      author: (b.authors && b.authors[0]) || "",
      thumbnail: b.thumbnail,
      publisher: b.publisher || "",
    }));
  }

  const recommendByAuthor = (author) =>
    author ? kakaoSearch({ query: author, target: "person" }) : [];

  const recommendByTitleLike = (title) =>
    title ? kakaoSearch({ query: title, target: "title" }) : [];

  // 👉 서버 추천: /api/books/reco/{userId}
  async function recommendFromServer() {
    try {
      let uid = null;

      // 1) Auth 모듈이 있으면 거기서 먼저
      try {
        if (window.Auth && typeof window.Auth.getUser === "function") {
          const u = window.Auth.getUser();
          if (u && (u.email || u.username || u.id || u.name)) {
            uid = u.email || u.username || u.id || u.name;
          }
        }
      } catch {}

      // 2) localStorage.user 에서 이메일
      if (!uid) {
        try {
          const raw = localStorage.getItem("user");
          if (raw) {
            const u = JSON.parse(raw);
            uid = u.email || u.username || u.id || u.name || null;
          }
        } catch {}
      }

      // 3) 그래도 없으면 익명 UUID (백엔드에서 처리 가능하다면 사용)
      if (!uid) {
        uid = localStorage.getItem("bb_uid");
        if (!uid) {
          uid = crypto.randomUUID
            ? crypto.randomUUID()
            : Date.now() + "-" + Math.random();
          localStorage.setItem("bb_uid", uid);
        }
      }

      const url =
        (API_BASE || "") + "/api/books/reco/" + encodeURIComponent(uid);

      const res = await fetch(url, { cache: "no-store", credentials: "include" });
      if (!res.ok) return [];
      return await res.json();
    } catch (e) {
      console.warn("server reco error", e);
      return [];
    }
  }

  async function enrichWithThumbnails(list) {
    if (!list || !list.length) return list || [];
    const needThumb = list.filter(
      (b) => !(b.thumbnail || b.imageUrl || b.coverImage)
    );
    if (!needThumb.length) return list;

    const cache = new Map();
    const tasks = [];

    for (const b of needThumb) {
      const key = (b.isbn || b.title || "").trim();
      if (!key || cache.has(key)) continue;

      tasks.push(
        (async () => {
          try {
            let results = [];
            if (b.isbn) {
              results = await kakaoSearch({ isbn: b.isbn });
            }
            if ((!results || !results.length) && b.title) {
              results = await kakaoSearch({ query: b.title, target: "title" });
            }
            const thumb = results && results[0] ? results[0].thumbnail || "" : "";
            cache.set(key, thumb || null);
          } catch {
            cache.set(key, null);
          }
        })()
      );
    }

    if (tasks.length) {
      await Promise.all(tasks);
    }

    return list.map((b) => {
      if (b.thumbnail || b.imageUrl || b.coverImage) return b;
      const key = (b.isbn || b.title || "").trim();
      const thumb = key ? cache.get(key) : null;
      return thumb ? { ...b, thumbnail: thumb } : b;
    });
  }

  function bookCardHTML(b) {
    const id = (
      b.id ??
      b.bookId ??
      b.listingId ??
      b.isbn ??
      b.title ??
      ""
    ).toString();
    const img = resolveImg(
      b.thumbnail || b.imageUrl || b.coverImage || b.imagePath
    );

    return `
      <div class="reco-card" data-book-card
           data-isbn="${esc(b.isbn || "")}"
           data-title="${esc(b.title || "")}"
           data-author="${esc(b.author || "")}">
        <button class="wish-btn" data-wish-btn data-book-id="${esc(
          id
        )}" aria-pressed="false">♡</button>
        <a class="cover-link" href="detail.html?id=${encodeURIComponent(id)}">
          <img src="${img}" alt="${esc(b.title || "책 이미지")}" />
          <div class="meta">
            <div class="title">${esc(b.title || "")}</div>
            <div class="price">${
              b.price != null ? Number(b.price).toLocaleString() + "원" : ""
            }</div>
          </div>
        </a>
      </div>`;
  }

  function renderToListContainer(el, list) {
    if (!el) return;
    if (!list?.length) {
      el.innerHTML = "";
      return;
    }
    el.innerHTML = list.map(bookCardHTML).join("");
  }

  async function run() {
    const recoListEl =
      document.getElementById("home-list") ||
      document.getElementById("recommendation-section");
    if (!recoListEl) return;

    try {
      // 1) 서버 추천 먼저
      const serverReco = await recommendFromServer();
      if (serverReco && serverReco.length) {
        const enriched = await enrichWithThumbnails(serverReco);
        renderToListContainer(recoListEl, (enriched || []).slice(0, 12));
        document.dispatchEvent(new Event("wishlist:invalidate"));
        return;
      }
    } catch (e) {
      console.warn("server reco failed", e);
    }

    // 2) 서버 추천 없으면 Kakao 기반 fallback
    const recent = getRecent();
    const byAuthor = recent.find((x) => x.author)?.author || "";
    const byTitle = recent.find((x) => x.title)?.title || "";
    const keywordInput = document.getElementById("searchInput");
    const keyword = keywordInput ? keywordInput.value.trim() : "";

    let fallback = [];
    if (byAuthor) fallback = await recommendByAuthor(byAuthor);
    else if (byTitle) fallback = await recommendByTitleLike(byTitle);
    else if (keyword) fallback = await kakaoSearch({ query: keyword });

    if (fallback && fallback.length) {
      renderToListContainer(recoListEl, fallback.slice(0, 12));
      document.dispatchEvent(new Event("wishlist:invalidate"));
    }
  }

  function bindSearchResultTracking() {
    document.body.addEventListener("click", (e) => {
      const card = e.target.closest("[data-book-card]");
      if (!card) return;
      const isbn = card.getAttribute("data-isbn") || "";
      const title = card.getAttribute("data-title") || "";
      const author = card.getAttribute("data-author") || "";
      trackView({ isbn, title, author });
    });
  }

  window.Reco = {
    init() {
      bindSearchResultTracking();
    },
    run,
    refresh: run,
    trackView,
  };
})();
