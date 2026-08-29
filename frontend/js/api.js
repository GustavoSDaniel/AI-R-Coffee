// Endpoints públicos consumidos 

window.Api = (() => {
  const BASE = window.CONFIG.API_BASE_URL;

  class ApiError extends Error {
    constructor(message, status) {
      super(message);
      this.name = 'ApiError';
      this.status = status;
    }
  }

  async function request(path, options = {}) {
    const { method = 'GET', body, token, timeout = 15000 } = options;
    const headers = { Accept: 'application/json' };
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    if (token) headers['Authorization'] = 'Bearer ' + token;

    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeout);

    let response;
    try {
      response = await fetch(BASE + path, {
        method,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined,
        signal: controller.signal
      });
    } catch (err) {
      if (err && err.name === 'AbortError') {
        throw new ApiError('O servidor demorou demais para responder. Tente novamente.', 0);
      }
      throw new ApiError('Não foi possível conectar ao servidor. Verifique se o backend está no ar.', 0);
    } finally {
      clearTimeout(timer);
    }

    if (response.status === 204) return null;

    const text = await response.text();
    let data = null;
    if (text) {
      try {
        data = JSON.parse(text);
      } catch (err) {
        data = text;
      }
    }

    if (!response.ok) {
      throw new ApiError(extractMessage(data, response.status), response.status);
    }

    return data;
  }

  function extractMessage(data, status) {
    if (data && typeof data === 'object') {
      if (data.detail) return data.detail;
      if (data.message) return data.message;
      if (data.title) return data.title;
    }
    if (typeof data === 'string' && data) return data;
    if (status === 401) return 'Sessão expirada. Entre novamente para continuar.';
    if (status === 403) return 'Você não tem permissão para esta ação.';
    return 'Algo deu errado. Tente novamente.';
  }

  return {
    ApiError,


    getActivePage(page = 0, size = window.CONFIG.pageSize, sort = 'name,asc') {
      return request(`/products/active?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`);
    },


    async getAllActiveProducts() {
      const items = [];
      const seen = new Set();

      const pushUnique = (list) => {
        for (const p of list || []) {
          const key = String(p && p.id);
          if (key && key !== 'undefined' && !seen.has(key)) {
            seen.add(key);
            items.push(p);
          }
        }
      };

      // Backend (melhor esforço), com timeout curto para não travar a vitrine.
      const backendPromise = (async () => {
        const out = [];
        const size = 50;
        let page = 0;
        while (page < 10) {
          const data = await request(`/products/active?page=${page}&size=${size}&sort=name,asc`, { timeout: 6000 });
          const content = (data && data.content) || [];
          out.push(...content);
          if (!data || data.last || content.length === 0) break;
          page += 1;
        }
        return out;
      })();

      // products.json local, sempre carregado e mesclado.
      const localPromise = (async () => {
        try {
          const response = await fetch('products.json');
          if (!response.ok) return [];
          const localData = await response.json();
          return (localData && localData.content) || [];
        } catch (err) {
          console.warn('[api] products.json local não encontrado.', err);
          return [];
        }
      })();

      const [backendRes, localRes] = await Promise.allSettled([backendPromise, localPromise]);

      if (backendRes.status === 'fulfilled') {
        pushUnique(backendRes.value);
      } else {
        console.warn('[api] Backend offline. Usando apenas o catálogo local.', backendRes.reason);
      }

      if (localRes.status === 'fulfilled') {
        pushUnique(localRes.value);
      }

      if (items.length === 0) {
        throw new ApiError('Não foi possível carregar o catálogo de nenhuma fonte.', 0);
      }

      return items;
    },

    getProduct(id) {
      return request(`/products/${encodeURIComponent(id)}`);
    },

    getCategories() {
      return request('/categories/active');
    },

    searchProducts(name) {
      return request(`/products/search?name=${encodeURIComponent(name || '')}`);
    },

    // Usuário logado 
    getCurrentUser(token) {
      return request('/users/me', { token });
    },

    createCheckoutSession(items, token) {
      return request('/checkout/session', { method: 'POST', body: { items }, token });
    }
  };
})();
