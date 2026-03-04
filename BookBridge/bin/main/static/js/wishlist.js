// wishlist.js – BookBridge 공용 찜(하트) 기능
(function (window, document) {
  "use strict";

  const API_BASE = (window.API_BASE ?? "").toString().replace(/\/+$/, "");
  const $ = (s, r = document) => r.querySelector(s);

  // ---------------- 공통: 로그인/유저 ----------------
  function getUser() {
    // auth-adapter / API.user.get 있으면 우선 사용
    if (window.API?.user?.get) {
      try { return window.API.user.get(); } catch { /* ignore */ }
    }
    try {
      return JSON.parse(localStorage.getItem("user") || "null");
    } catch {
      return null;
    }
  }

  function getUserId() {
    const u = getUser();
    // WishlistController 가 Long userId 를 쓰니까 숫자 id 우선
    return u && (u.id || u.userId || null);
  }

  // ---------------- 내부 상태 ----------------
  const Wishlist = {
    _items: new Set(),

    // 서버에서 내 찜 목록 가져오기
    async load() {
      const uid = getUserId();
      if (!uid) {
        this._items = new Set();
        this.paintAll();
        return;
      }

      try {
        const res = await fetch(
          `${API_BASE}/api/wishlist?userId=${encodeURIComponent(uid)}`,
          { credentials: "include", cache: "no-store" }
        );
        if (!res.ok) {
          console.warn("wishlist load 실패:", res.status);
          return;
        }
        const data = await res.json().catch(() => null);

        // 컨트롤러 응답: { ok:true, items:[listingId,...] }
        let arr = [];
        if (Array.isArray(data)) arr = data;
        else if (Array.isArray(data?.items)) arr = data.items;

        this._items = new Set(arr.map(v => String(v)));
        this.paintAll(); // 버튼들 상태 반영
      } catch (e) {
        console.warn("wishlist load 에러:", e);
      }
    },

    // 한 개 토글
    async toggle(listingId, btn) {
      const uid = getUserId();
      if (!uid) {
        alert("로그인이 필요합니다.");
        location.href = "login.html";
        return;
      }

      const idStr = String(listingId);
      const wasLiked = this._items.has(idStr);

      // 낙관적 UI: 일단 바로 반영
      this._setLiked(btn, !wasLiked);

      try {
        const res = await fetch(`${API_BASE}/api/wishlist/toggle`, {
          method: "POST",
          credentials: "include",
          headers: { "content-type": "application/json" },
          body: JSON.stringify({
            userId: uid,
            listingId: listingId
          }),
        });

        if (!res.ok) throw new Error(res.status);

        const data = await res.json().catch(() => ({}));
        const liked = data.liked ?? !wasLiked; // 컨트롤러: {ok:true, liked:true/false}

        if (liked) this._items.add(idStr);
        else this._items.delete(idStr);

        this._setLiked(btn, liked);
      } catch (e) {
        console.error("wishlist toggle 실패:", e);
        // 실패하면 원래 상태로 롤백
        this._setLiked(btn, wasLiked);
        alert("찜 처리 중 오류가 발생했습니다.");
      }
    },

    // 버튼 하나 상태 적용
    _setLiked(btn, liked) {
      if (!btn) return;
      btn.classList.toggle("on", liked);
      btn.setAttribute("aria-pressed", liked ? "true" : "false");
      // 텍스트 변경 (♡ / ♥)
      const onText  = btn.dataset.onText  || "♥";
      const offText = btn.dataset.offText || "♡";
      btn.textContent = liked ? onText : offText;
    },

    // 화면에 있는 모든 버튼에 상태 반영
    paintAll() {
      const buttons = document.querySelectorAll("[data-wish-btn]");
      buttons.forEach(btn => {
        const id = btn.getAttribute("data-book-id");
        const liked = this._items.has(String(id));
        this._setLiked(btn, liked);
      });
    },

    // ✅ 지금 서버에서 받아온 찜 목록 id 배열로 꺼내기
    listIds() {
      return Array.from(this._items);
    },

    async init() {
      // 최초 1회 서버에서 목록 불러오기
      await this.load();
    },
  };

  // ---------------- 전역 이벤트 바인딩 ----------------

  // 어디 페이지든 공통: data-wish-btn 버튼 클릭 시 토글
  document.addEventListener("click", (e) => {
  const btn = e.target.closest("[data-wish-btn]");
  if (!btn) return;

  e.preventDefault();
  const id = btn.getAttribute("data-book-id");
  if (!id) return;          // ❗ id 없으면 여기서 바로 리턴
  Wishlist.toggle(id, btn);
});


  // 페이지에서 수동으로 "상태 다시 계산해!" 할 때
  document.addEventListener("wishlist:invalidate", () => {
    Wishlist.paintAll();
  });

  // 전역 노출
  window.Wishlist = Wishlist;
})(window, document);
