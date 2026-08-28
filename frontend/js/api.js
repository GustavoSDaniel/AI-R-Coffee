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
    const { method = 'GET', body, token } = options;
    const headers = { Accept: 'application/json' };
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    if (token) headers['Authorization'] = 'Bearer ' + token;

    let response;
    try {
      response = await fetch(BASE + path, {
        method,
        headers,
        body: body !== undefined ? JSON.stringify(body) : undefined
      });
    } catch (err) {
      throw new ApiError('Não foi possível conectar ao servidor. Verifique se o backend está no ar.', 0);
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

      
      try {
        const size = 50;
        let page = 0;
        while (page < 10) {
          const data = await request(`/products/active?page=${page}&size=${size}&sort=name,asc`);
          const content = (data && data.content) || [];
          items.push(...content);
          if (!data || data.last || content.length === 0) break;
          page += 1;
        }
      } catch (err) {
        console.warn('[api] Backend offline. Usando catálogo estático local.', err);
      }

      
      if (items.length === 0) {
        try {
          const response = await fetch('products.json');
          if (response.ok) {
            const localData = await response.json();
            if (localData && localData.content) {
              items.push(...localData.content);
            }
          }
        } catch (err) {
          console.warn('[api] products.json local não encontrado.', err);
        }
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
