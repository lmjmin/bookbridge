// first.html 상세검색 + 카메라 ISBN 스캔 전용 (+ Kakao 썸네일 보강)
(function () {
  const API_BASE = (window.API_BASE ?? "").replace(/\/+$/, "");
  const $ = (sel, root = document) => root.querySelector(sel);

  const KAKAO_KEY = window.KAKAO_REST_KEY || window.KAKAO_REST_API_KEY || "";
  const KAKAO_API = "https://dapi.kakao.com/v3/search/book";

  let html5qrcode = null;
  let scanning = false;

  function isSecure() {
    return (
      window.isSecureContext ||
      location.protocol === "https:" ||
      location.hostname === "localhost"
    );
  }

  function openModal() {
    $("#adv-modal")?.style.setProperty("display", "block");
    $("#adv-backdrop")?.style.setProperty("display", "block");
  }

  function closeModal() {
    $("#adv-modal")?.style.setProperty("display", "none");
    $("#adv-backdrop")?.style.setProperty("display", "none");
    stopScan();
  }

  function normalizeIsbn(s) {
    return (s || "").toString().replace(/[^0-9Xx]/g, "");
  }

  async function ensureScanLibLoaded() {
    if (window.Html5Qrcode && window.Html5QrcodeSupportedFormats) return;

    // scan-loader.js가 있으면 먼저 사용
    if (typeof window.ensureScanLibLoaded === "function") {
      await window.ensureScanLibLoaded();
      return;
    }

    await new Promise((resolve, reject) => {
      const s = document.createElement("script");
      s.src =
        "https://unpkg.com/html5-qrcode@2.3.8/minified/html5-qrcode.min.js";
      s.onload = resolve;
      s.onerror = () => reject(new Error("scan lib load fail"));
      document.head.appendChild(s);
    });
  }

  // 상세검색 폼 값을 하나로 모으는 헬퍼
  function buildParams(isbnOverride) {
    const rawIsbn =
      typeof isbnOverride === "string" && isbnOverride
        ? isbnOverride
        : ($("#adv-isbn")?.value || "");

    return {
      q: ($("#adv-q")?.value || "").trim() || null,
      department: ($("#adv-dept")?.value || "").trim() || null,
      minPrice: $("#adv-min")?.value ? Number($("#adv-min").value) : null,
      maxPrice: $("#adv-max")?.value ? Number($("#adv-max").value) : null,
      conditionText: ($("#adv-cond")?.value || "").trim() || null,
      hasImage: $("#adv-hasimage")?.checked ? true : null,
      isbn: normalizeIsbn(rawIsbn) || null,
    };
  }

  async function startScan() {
    if (scanning) return;
    const box = $("#adv-scanner");
    const hint = $("#scan-hint");
    if (!box) return;

    if (!isSecure()) {
      alert("카메라는 HTTPS 또는 localhost 환경에서만 사용할 수 있습니다.");
      return;
    }

    box.style.display = "block";
    if (hint) hint.style.display = "inline";

    try {
      await ensureScanLibLoaded();
    } catch (e) {
      console.error("scan lib load fail", e);
      alert("카메라 라이브러리를 불러올 수 없습니다.");
      box.style.display = "none";
      if (hint) hint.style.display = "none";
      return;
    }

    if (!window.Html5Qrcode) {
      alert("카메라 라이브러리를 불러오지 못했습니다.");
      box.style.display = "none";
      if (hint) hint.style.display = "none";
      return;
    }

    if (!html5qrcode) {
      html5qrcode = new Html5Qrcode("adv-scanner");
    }

    const config = {
      fps: 10,
      qrbox: { width: 260, height: 180 },
      formatsToSupport: [
        Html5QrcodeSupportedFormats.EAN_13,
        Html5QrcodeSupportedFormats.QR_CODE,
        Html5QrcodeSupportedFormats.CODE_39,
        Html5QrcodeSupportedFormats.CODE_128,
      ],
    };

    scanning = true;
    html5qrcode
      .start(
        { facingMode: "environment" },
        config,
        (decodedText) => {
          const val = normalizeIsbn(decodedText);
          if (!val) return;

          const input = $("#adv-isbn");
          if (input) input.value = val;

          // 스캔 중지
          stopScan();

          // ISBN 기준으로 즉시 검색 실행
          const params = buildParams(val);
          searchByIsbnOrFilter(params);
          closeModal();
        },
        () => {}
      )
      .catch((err) => {
        console.error("scan start error", err);
        alert("카메라를 시작할 수 없습니다.");
        stopScan();
      });
  }

  function stopScan() {
    const box = $("#adv-scanner");
    const hint = $("#scan-hint");

    if (!html5qrcode || !scanning) {
      if (box) box.style.display = "none";
      if (hint) hint.style.display = "none";
      return;
    }

    scanning = false;
    html5qrcode
      .stop()
      .catch(() => {})
      .finally(() => {
        if (box) box.style.display = "none";
        if (hint) hint.style.display = "none";
      });
  }

  /* -------- Kakao 썸네일 보강 -------- */

  async function kakaoLookup({ isbn, title, signal }) {
    if (!KAKAO_KEY) return null;

    const qIsbn = normalizeIsbn(isbn);
    const qTitle = (title || "").toString().trim();
    if (!qIsbn && !qTitle) return null;

    const u = new URL(KAKAO_API);
    if (qIsbn) {
      u.searchParams.set("query", qIsbn);
      u.searchParams.set("target", "isbn");
    } else {
      u.searchParams.set("query", qTitle);
      u.searchParams.set("target", "title");
    }
    u.searchParams.set("page", "1");
    u.searchParams.set("size", "1");

    let res;
    try {
      res = await fetch(u.toString(), {
        headers: { Authorization: "KakaoAK " + KAKAO_KEY },
        cache: "no-store",
        signal,
      });
    } catch {
      return null;
    }
    if (!res || !res.ok) return null;
    const data = await res.json().catch(() => null);
    return data?.documents?.[0] || null;
  }

  async function enrichWithThumbnails(list) {
    if (!Array.isArray(list) || !list.length) return list || [];

    const out = [...list];
    const cache = new Map();
    const ctl = new AbortController();

    const tasks = out.map(async (b, idx) => {
      if (b.thumbnail || b.imageUrl || b.coverImage) return;

      const key =
        normalizeIsbn(b.isbn) || (b.title || "").trim() || "";
      if (!key) return;

      if (cache.has(key)) {
        const t = cache.get(key);
        if (t) out[idx] = { ...b, thumbnail: t };
        return;
      }

      const doc = await kakaoLookup({
        isbn: b.isbn,
        title: b.title,
        signal: ctl.signal,
      });
      const thumb = doc?.thumbnail || "";
      cache.set(key, thumb || null);
      if (thumb) out[idx] = { ...b, thumbnail: thumb };
    });

    try {
      await Promise.all(tasks);
    } catch {}
    return out;
  }

  /* -------- 필터 실패 시 단순 검색 fallback -------- */

  async function fallbackSimpleSearch(params) {
    const renderResults = window.renderResults;
    const showSearchSection = window.showSearchSection;

    const key = [
      params.q,
      params.department,
      params.conditionText,
      params.isbn,
    ]
      .filter(Boolean)
      .join(" ")
      .trim();

    if (!key || !window.$api?.catalog?.search) {
      if (typeof showSearchSection === "function") showSearchSection();
      if (typeof renderResults === "function") renderResults([]);
      return;
    }

    try {
      const data = await window.$api.catalog.search(key);
      let list = Array.isArray(data)
        ? data
        : Array.isArray(data?.content)
        ? data.content
        : [];
      list = await enrichWithThumbnails(list);
      if (typeof showSearchSection === "function") showSearchSection();
      if (typeof renderResults === "function") renderResults(list);
    } catch (e) {
      console.warn("fallback search error", e);
      if (typeof showSearchSection === "function") showSearchSection();
      if (typeof renderResults === "function") renderResults([]);
    }
  }

  /* -------- 상세 검색 실행 -------- */

  async function searchByIsbnOrFilter(params) {
    // 상세검색 결과는 첫 페이지에만 사용
    params.page = 0;
    params.size = 30;

    const renderResults = window.renderResults;
    const showSearchSection = window.showSearchSection;

    const isbn = (params.isbn || "").toString().trim();

    // 1) ISBN 우선 검색
    if (isbn) {
      try {
        const u = new URL(
          API_BASE + "/api/books-adv/by-isbn",
          window.location.origin
        );
        u.searchParams.set("isbn", isbn);
        const res = await fetch(u.toString(), {
          cache: "no-store",
          credentials: "include",
        });
        const raw = res.ok ? await res.json().catch(() => null) : null;
        let list = Array.isArray(raw)
          ? raw
          : Array.isArray(raw?.content)
          ? raw.content
          : [];

        if (Array.isArray(list) && list.length) {
          list = await enrichWithThumbnails(list);
          if (typeof showSearchSection === "function") showSearchSection();
          if (typeof renderResults === "function") renderResults(list);
          return;
        }
      } catch (e) {
        console.warn("isbn search fail", e);
      }
    }

    // 2) 필터 검색
    try {
      const u = new URL(
        API_BASE + "/api/books-adv/filter",
        window.location.origin
      );
      Object.entries(params).forEach(([k, v]) => {
        if (v !== null && v !== undefined && v !== "") {
          u.searchParams.set(k, String(v));
        }
      });

      let res;
      try {
        res = await fetch(u.toString(), {
          cache: "no-store",
          credentials: "include",
        });
      } catch (e) {
        console.warn("filter fetch fail", e);
        await fallbackSimpleSearch(params);
        return;
      }

      if (!res.ok) {
        console.warn("filter status", res.status);
        // 404/405 등일 때만 fallback
        if (res.status === 404 || res.status === 405) {
          await fallbackSimpleSearch(params);
        } else {
          if (typeof showSearchSection === "function") showSearchSection();
          if (typeof renderResults === "function") renderResults([]);
        }
        return;
      }

      const data = await res.json().catch(() => null);
      let list = Array.isArray(data?.content)
        ? data.content
        : Array.isArray(data)
        ? data
        : [];

      list = await enrichWithThumbnails(list);

      if (typeof showSearchSection === "function") showSearchSection();
      if (typeof renderResults === "function") renderResults(list);
    } catch (e) {
      console.warn("filter search fail", e);
      await fallbackSimpleSearch(params);
    }
  }

  /* -------- 이벤트 바인딩 -------- */

  function bind() {
    const openBtn = $("#btn-adv");
    const closeBtn = $("#adv-close");
    const backdrop = $("#adv-backdrop");
    const form = $("#adv-form");
    const scanBtn = $("#adv-scan-btn");
    const clearBtn = $("#adv-clear-btn");

    if (openBtn)
      openBtn.addEventListener("click", (e) => {
        e.preventDefault();
        openModal();
      });
    if (closeBtn)
      closeBtn.addEventListener("click", (e) => {
        e.preventDefault();
        closeModal();
      });
    if (backdrop)
      backdrop.addEventListener("click", (e) => {
        if (e.target === backdrop) closeModal();
      });

    if (scanBtn)
      scanBtn.addEventListener("click", (e) => {
        e.preventDefault();
        if (scanning) stopScan();
        else startScan();
      });

    if (clearBtn)
      clearBtn.addEventListener("click", (e) => {
        e.preventDefault();
        $("#adv-isbn") && ($("#adv-isbn").value = "");
        $("#adv-q") && ($("#adv-q").value = "");
        $("#adv-dept") && ($("#adv-dept").value = "");
        $("#adv-min") && ($("#adv-min").value = "");
        $("#adv-max") && ($("#adv-max").value = "");
        $("#adv-cond") && ($("#adv-cond").value = "");
        $("#adv-hasimage") && ($("#adv-hasimage").checked = false);
      });

    if (form) {
      form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const params = buildParams();
        await searchByIsbnOrFilter(params);
        closeModal();
      });
    }
  }

  document.addEventListener("DOMContentLoaded", bind);
})();
