
window.Utils = (() => {
  const numberFormat = new Intl.NumberFormat(window.CONFIG.locale || 'pt-BR', {
    style: 'currency',
    currency: window.CONFIG.currency || 'BRL'
  });

  
  const UNIT_LABELS = {
    UN: 'Unidade',
    KIT: 'Kit',
    KG: 'Quilograma',
    G: 'Grama',
    L: 'Litro',
    ML: 'Mililitro',
    M: 'Metro',
    CX: 'Caixa',
    PR: 'Par'
  };

  function fmtBRL(value) {
    const n = Number(value);
    return numberFormat.format(Number.isFinite(n) ? n : 0);
  }

  function unitLabel(code) {
    return UNIT_LABELS[code] || code || '';
  }

  function escapeHtml(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  // Remove acentos e normaliza para minúsculas usado em busca

  function normalize(value) {
    return String(value || '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '');
  }

  function debounce(fn, ms = 250) {
    let timer;
    return function (...args) {
      clearTimeout(timer);
      timer = setTimeout(() => fn.apply(this, args), ms);
    };
  }

  function placeholderImage(seed = '') {
    const initial = String(seed || 'C').trim().charAt(0).toUpperCase() || 'C';
    const svg =
      `<svg xmlns="http://www.w3.org/2000/svg" width="640" height="640" viewBox="0 0 640 640">` +
      `<defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1">` +
      `<stop offset="0" stop-color="#5a3b2a"/><stop offset="1" stop-color="#2a1a12"/>` +
      `</linearGradient></defs>` +
      `<rect width="640" height="640" fill="url(#g)"/>` +
      `<g fill="none" stroke="#e3c98a" stroke-width="7" stroke-linecap="round" opacity="0.9">` +
      `<path d="M248 218c0-30 16-46 46-46s46 16 46 46"/>` +
      `<path d="M212 218c0-30 16-46 46-46s46 16 46 46"/>` +
      `<path d="M284 218c0-30 16-46 46-46s46 16 46 46"/>` +
      `</g>` +
      `<g fill="#f1e6d4">` +
      `<rect x="206" y="256" width="228" height="24" rx="12"/>` +
      `<path d="M210 276h220l-14 150a24 24 0 0 1-24 22H248a24 24 0 0 1-24-22z"/>` +
      `<rect x="328" y="248" width="72" height="16" rx="8"/>` +
      `</g>` +
      `<text x="320" y="522" font-family="Georgia, 'Times New Roman', serif" font-size="96" ` +
      `fill="#e3c98a" text-anchor="middle" font-weight="700">${initial}</text>` +
      `<text x="320" y="576" font-family="Georgia, 'Times New Roman', serif" font-size="30" ` +
      `letter-spacing="6" fill="#c9a24b" text-anchor="middle">CAFÉ</text>` +
      `</svg>`;
    return 'data:image/svg+xml;utf8,' + encodeURIComponent(svg);
  }

  function toast(message, type = 'info') {
    let host = document.getElementById('toast-host');
    if (!host) {
      host = document.createElement('div');
      host.id = 'toast-host';
      host.setAttribute('aria-live', 'polite');
      document.body.appendChild(host);
    }
    const el = document.createElement('div');
    el.className = 'toast toast--' + type;
    el.setAttribute('role', 'status');
    el.textContent = message;
    host.appendChild(el);
    requestAnimationFrame(() => el.classList.add('is-visible'));
    window.setTimeout(() => {
      el.classList.remove('is-visible');
      window.setTimeout(() => el.remove(), 320);
    }, 3200);
  }

  return { fmtBRL, unitLabel, escapeHtml, normalize, debounce, placeholderImage, toast };
})();
