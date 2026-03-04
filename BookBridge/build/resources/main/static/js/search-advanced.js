// first.html 전용: 상세검색 모달 + ISBN 스캔 + 서버/학과 필터 검색
(function () {
  const API_BASE = (window.API_BASE ?? "").toString().replace(/\/+$/, "");
  const $ = (s, r = document) => r.querySelector(s);
  const isMobile = () =>
    /Mobi|Android|iPhone|iPad|iPod/i.test(navigator.userAgent) ||
    window.innerWidth <= 768;

  let html5qrcode = null, scanning = false;

  function secureContext() {
    return window.isSecureContext || location.protocol === "https:" || location.hostname === "localhost";
  }

  function openModal() {
    $("#adv-modal")?.style.setProperty("display","block");
    $("#adv-backdrop")?.style.setProperty("display","block");
  }
  function closeModal() {
    $("#adv-modal")?.style.setProperty("display","none");
    $("#adv-backdrop")?.style.setProperty("display","none");
    stopScanner();
  }

  function stopScanner() {
    if (html5qrcode && scanning) {
      html5qrcode.stop().finally(() => {
        html5qrcode.clear();
        scanning = false;
        $("#adv-scanner")?.style.setProperty("display","none");
      });
    } else {
      $("#adv-scanner")?.style.setProperty("display","none");
    }
  }

  async function startScanner() {
    if (!secureContext()) {
      alert("카메라는 HTTPS(또는 localhost)에서만 사용 가능합니다. https 주소로 접속해 주세요.");
      return;
    }
    const box = $("#adv-scanner");
    if (!box) return;
    box.style.display = "block";
    box.innerHTML = "";
    if (scanning) return;

    try { await (window.ensureScanLibLoaded ? ensureScanLibLoaded() : Promise.resolve()); }
    catch (e) { console.error(e); alert("스캔 라이브러리를 불러오지 못했습니다."); box.style.display="none"; return; }

    html5qrcode = new Html5Qrcode("adv-scanner");
    const config = {
      fps: 10, qrbox: 240,
      formatsToSupport: [
        Html5QrcodeSupportedFormats.EAN_13,
        Html5QrcodeSupportedFormats.QR_CODE,
        Html5QrcodeSupportedFormats.CODE_39,
        Html5QrcodeSupportedFormats.CODE_128,
        Html5QrcodeSupportedFormats.UPC_A
      ]
    };

    try {
      await html5qrcode.start(
        { facingMode: "environment" },
        config,
        (decoded) => {
          const v = (decoded || "").replace(/[^0-9Xx]/g, "");
          if (v.length >= 10) {
            const el = $("#adv-isbn"); if (el) el.value = v;
            stopScanner();
          }
        },
        () => {}
      );
      scanning = true;
    } catch (err) {
      console.error("Scanner start failed:", err);
      alert("카메라를 시작할 수 없어요. 브라우저 권한을 확인해 주세요.");
      box.style.display = "none";
    }
  }

  function esc(s) {
    return String(s ?? "").replace(/[&<>"']/g, (m)=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[m]));
  }

  function resolveImg(u) {
    if (!u) return "media/no-image.png";
    const s = String(u).trim();
    if (/^https?:\/\//i.test(s) || s.startsWith("data:")) return s;
    return s.startsWith("/") ? (API_BASE + s) : s;
  }

  function cardHtml(b) {
    const id = (b?.id ?? b?.bookId ?? b?.isbn ?? b?.title ?? "").toString();
    const img = resolveImg(b.thumbnail || b.thumbnailUrl || b.imageUrl || b.coverImage);
    const price = (b.price != null) ? `${Number(b.price).toLocaleString()}원` : "";
    return `
      <div class="reco-card" data-book-card>
        <button class="wish-btn" data-wish-btn data-book-id="${esc(id)}" aria-pressed="false">♡</button>
        <a class="cover-link" href="detail.html?id=${encodeURIComponent(id)}">
          <img src="${img}" alt="${esc(b.title||'책 이미지')}"/>
          <div class="meta">
            <div class="title">${esc(b.title||"")}</div>
            <div class="price">${esc(price)}</div>
          </div>
        </a>
      </div>`;
  }

  async function searchAndRender(params) {
    const sList = $("#search-list");
    const sEmpty = $("#search-empty");

    // 1) ISBN 우선
    const isbn = (params.isbn || "").trim();
    if (isbn) {
      try {
        const r = await fetch(`${API_BASE}/api/books-adv/by-isbn?isbn=${encodeURIComponent(isbn)}`, { cache: "no-store" });
        const list = r.ok ? await r.json() : [];
        if (!list?.length) { if(sList) sList.innerHTML = ""; if (sEmpty) sEmpty.style.display = "block"; }
        else { if (sEmpty) sEmpty.style.display = "none"; if (sList) sList.innerHTML = list.map(cardHtml).join(""); document.dispatchEvent(new Event("wishlist:invalidate")); }
        return;
      } catch (e) { console.warn(e); }
    }

    // 2) 필터 검색 (페이지 기본값 보정)
    const u = new URL(`${API_BASE}/api/books-adv/filter`);
    Object.entries(params).forEach(([k, v]) => { if (v !== null && v !== undefined && v !== "") u.searchParams.set(k, String(v)); });
    if (!u.searchParams.has("page")) u.searchParams.set("page", "0");
    if (!u.searchParams.has("size")) u.searchParams.set("size", "30");

    try {
      const r = await fetch(u.toString(), { cache: "no-store" });
      const data = r.ok ? await r.json() : null;
      const list = Array.isArray(data?.content) ? data.content : (Array.isArray(data) ? data : []);
      if (!list?.length) { if(sList) sList.innerHTML = ""; if (sEmpty) sEmpty.style.display = "block"; }
      else { if (sEmpty) sEmpty.style.display = "none"; if (sList) sList.innerHTML = list.map(cardHtml).join(""); document.dispatchEvent(new Event("wishlist:invalidate")); }
    } catch (e) { console.error(e); alert("검색 실패"); }
  }

  function bind() {
    $("#btn-adv")?.addEventListener("click", () => {
      openModal();
      const canScan = secureContext() && isMobile();
      $("#adv-scan-btn")?.style.setProperty("display", canScan ? "inline-block" : "none");
      $("#adv-scanner")?.style.setProperty("display","none");
      stopScanner();
    });
    $("#adv-backdrop")?.addEventListener("click", closeModal);
    $("#adv-close")?.addEventListener("click", closeModal);
    $("#adv-clear-btn")?.addEventListener("click", () => { const el = $("#adv-isbn"); if (el) el.value = ""; });

    $("#adv-scan-btn")?.addEventListener("click", (e) => { e.preventDefault(); startScanner(); });

    $("#adv-form")?.addEventListener("submit", async (e) => {
      e.preventDefault();
      $("#recommendation-section")?.classList.add("hidden");
      const ss = $("#search-section"); if (ss) ss.style.display = "block";

      const params = {
        q: ($("#adv-q")?.value || "").trim() || null,
        department: ($("#adv-dept")?.value || "").trim() || null,
        minPrice: $("#adv-min")?.value ? Number($("#adv-min").value) : null,
        maxPrice: $("#adv-max")?.value ? Number($("#adv-max").value) : null,
        hasImage: $("#adv-hasimage")?.checked ? true : null,
        conditionText: ($("#adv-cond")?.value || "").trim() || null,
        isbn: ($("#adv-isbn")?.value || "").trim() || null,
        page: 0, size: 30
      };
      await searchAndRender(params);
      closeModal();
    });
  }

  document.addEventListener("DOMContentLoaded", bind);
})();
