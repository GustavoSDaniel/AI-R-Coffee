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
      // ProblemDetail do Spring (RFC 7807): a mensagem fica em `detail`.
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

    /** Uma página de produtos ativos (resposta Page do Spring Data). */
    getActivePage(page = 0, size = window.CONFIG.pageSize, sort = 'name,asc') {
      return request(`/products/active?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`);
    },

    /** Percorre todas as páginas de produtos ativos e retorna um array plano. */
    async getAllActiveProducts() {
      const items = [];
      const size = 50;
      let page = 0;

      // Limite de segurança para nunca entrar em loop infinito.
      while (page < 100) {
        const data = await request(`/products/active?page=${page}&size=${size}&sort=name,asc`);
        const content = (data && data.content) || [];
        items.push(...content);
        if (!data || data.last || content.length === 0) break;
        page += 1;
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

    /** Usuário logado */
    getCurrentUser(token) {
      return request('/users/me', { token });
    },

    createCheckoutSession(items, token) {
      return request('/checkout/session', { method: 'POST', body: { items }, token });
    }
  };
})();
