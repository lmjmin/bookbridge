(function (g) {
  "use strict";
  const API_BASE = (g.API_BASE || "").replace(/\/+$/,"");

  async function listPurchases({ page=0, size=20 } = {}) {
    const candidates = [
      `/api/purchases?page=${page}&size=${size}`,
      `/purchases?page=${page}&size=${size}`,
      `/api/purchase/list?page=${page}&size=${size}`
    ];
    for (const p of candidates) {
      try {
        const r = await fetch(API_BASE + p, { credentials:"include", cache:"no-store" });
        if (!r.ok) continue;
        const j = await r.json().catch(()=>null);
        if (!j) continue;
        const arr = Array.isArray(j) ? j : (j.content||j.data||j.list||[]);
        if (arr && arr.length) return arr;
      } catch {}
    }
    return [];
  }

  g.Purchases = { list: listPurchases };
})(window);
