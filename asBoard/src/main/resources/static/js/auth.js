// 모든 페이지에서 쓰는 API 호출 헬퍼
// - 쿠키 기반 인증이므로 Authorization 헤더 불필요
// - 401이면 /api/auth/refresh 후 1회 재시도

(function () {
  let refreshInFlight = null;
  function refreshOnce() {
    if (!refreshInFlight) {
      refreshInFlight = fetch('/api/auth/refresh', { method: 'POST', credentials: 'same-origin' })
        .finally(() => { refreshInFlight = null; });
    }
    return refreshInFlight;
  }

  window.apiFetch = async function(url, opts = {}) {
    const options = { credentials: 'same-origin', ...opts };
    let res = await fetch(url, options);

    if (res.status === 401) {
      const r = await refreshOnce();
      if (r.ok) {
        res = await fetch(url, options);   // 재시도
      } else {
        const back = encodeURIComponent(location.pathname + location.search);
        location.href = '/login?redirect=' + back;
        return r;
      }
    } else if (res.status === 403) {
      location.href = '/403';
      return res;
    }
    return res;
  };

  window.doLogout = async function() {
    // GET /logout 지원해놨으면:
    location.href = '/logout';
//    try { await fetch('/api/auth/logout', { method: 'POST', credentials: 'same-origin' }); } catch(e) {}
//    location.href = '/';
  };
})();