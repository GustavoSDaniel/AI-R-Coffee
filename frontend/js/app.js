// * App — roteador (hash) + renderização das páginas + header.
 
(() => {
  'use strict';

  const App = (window.App = {
    categories: [],
    categoryById: {},
    products: [],
    catalogState: 'idle', 
    searchTerm: '',
    sortBy: 'name-asc'
  });

  // helpers
  function parseHash() {
    const raw = location.hash.slice(1) || '/';
    const [pathPart, queryPart] = raw.split('?');
    const path = pathPart.startsWith('/') ? pathPart : '/' + pathPart;
    return { path, params: new URLSearchParams(queryPart || '') };
  }

  function spinnerHtml() {
    return `<div class="loading"><span class="spinner" aria-hidden="true"></span><span>Carregando…</span></div>`;
  }

  function emptyHtml(title, message, actionHtml = '') {
    return (
      `<div class="empty">` +
      `<span class="empty-icon" aria-hidden="true">☕</span>` +
      `<h2>${Utils.escapeHtml(title)}</h2>` +
      `<p>${Utils.escapeHtml(message)}</p>` +
      actionHtml +
      `</div>`
    );
  }

  function imageTag(src, alt, seed, className) {
    const safeSrc = Utils.escapeHtml(src || '');
    const safeAlt = Utils.escapeHtml(alt || '');
    const safeSeed = Utils.escapeHtml(seed || '');
    const cls = className || '';
    return (
      `<img src="${safeSrc}" alt="${safeAlt}" data-seed="${safeSeed}" loading="lazy" class="${cls}" ` +
      `onerror="this.onerror=null;this.src=Utils.placeholderImage(this.dataset.seed)">`
    );
  }

  function sortProducts(list, sortBy) {
    const copy = list.slice();
    if (sortBy === 'price-asc') copy.sort((a, b) => Number(a.price) - Number(b.price));
    else if (sortBy === 'price-desc') copy.sort((a, b) => Number(b.price) - Number(a.price));
    else copy.sort((a, b) => Utils.normalize(a.name).localeCompare(Utils.normalize(b.name)));
    return copy;
  }

  //header 
  function renderNav(categories) {
    const nav = document.getElementById('category-nav');
    if (!nav) return;
    const activeId = parseHash().params.get('categoria') || null;
    const all = `<button class="chip ${activeId ? '' : 'is-active'}" type="button" data-cat="">Todos</button>`;
    const items = (categories || [])
      .map(
        (c) =>
          `<button class="chip ${activeId === c.id ? 'is-active' : ''}" type="button" data-cat="${Utils.escapeHtml(c.id)}">${Utils.escapeHtml(c.name)}</button>`
      )
      .join('');
    nav.innerHTML = all + items;
    nav.querySelectorAll('[data-cat]').forEach((btn) =>
      btn.addEventListener('click', () => {
        const id = btn.dataset.cat;
        location.hash = id ? '/?categoria=' + encodeURIComponent(id) : '/';
      })
    );
  }

  function renderAuth() {
    const el = document.getElementById('auth-area');
    if (!el) return;
    if (Auth.isAuthenticated()) {
      el.innerHTML = `
        <span class="auth-user">Olá, <strong>${Utils.escapeHtml(Auth.userLabel())}</strong></span>
        <button class="btn btn--ghost btn--sm" id="logout-btn" type="button">Sair</button>`;
      el.querySelector('#logout-btn').addEventListener('click', () => Auth.logout());
    } else {
      el.innerHTML = `<button class="btn btn--solid btn--sm" id="login-btn" type="button">Entrar / Cadastrar</button>`;
      el.querySelector('#login-btn').addEventListener('click', () => Auth.login(location.hash || '#/'));
    }
  }

  function renderCartCount() {
    const el = document.getElementById('cart-count');
    if (!el) return;
    const n = Cart.count();
    el.textContent = n > 99 ? '99+' : String(n);
    el.hidden = n === 0;
  }

  // Sincroniza o usuário logado no banco da API (cria se ainda não existir). 
  async function syncCurrentUser() {
    if (!Auth.isAuthenticated()) return;
    try {
      const token = await Auth.ensureToken(30);
      if (!token) return;
      await Api.getCurrentUser(token);
    } catch (err) {
      console.warn('[app] não foi possível sincronizar o usuário.', err);
    }
  }

  // vitrine 
  async function renderHome(app) {
    const catId = parseHash().params.get('categoria') || null;

    app.innerHTML = `
      <section class="hero">
        <div class="hero-inner">
          <span class="hero-kicker">Café · acessórios · brindes</span>
          <h1 class="hero-title">Café de origem, <em>torrado com calma</em>.</h1>
          <p class="hero-subtitle">Grãos de origem, torra artesanal, acessórios para o seu ritual e brindes — tudo com entrega na sua porta. Sem pressa, sem atalhos.</p>
          <div class="hero-cta">
            <button class="btn btn--solid btn--lg" id="hero-cta" type="button">Ver o catálogo</button>
            <a class="btn btn--ghost btn--lg" href="#/carrinho">Ir ao carrinho</a>
          </div>
          <div class="hero-trust">
            <span>Grãos de origem</span><span>Torra artesanal</span><span>Entrega nacional</span>
          </div>
        </div>
      </section>

      <section class="section container" id="catalogo">
        <div class="section-head">
          <h2 class="section-title" id="catalog-title">Catálogo</h2>
          <span class="section-count" id="catalog-count"></span>
        </div>

        <div class="toolbar">
          <div class="search-box">
            <svg viewBox="0 0 24 24" width="19" height="19" fill="none" aria-hidden="true">
              <circle cx="11" cy="11" r="7" stroke="currentColor" stroke-width="1.7"/>
              <path d="M20 20l-3.2-3.2" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/>
            </svg>
            <input class="search-input" id="search-input" type="search" placeholder="Buscar por nome…"
              value="${Utils.escapeHtml(App.searchTerm)}" autocomplete="off" />
            <div class="suggest" id="suggest" hidden></div>
          </div>

          <div class="sort-row">
            <label class="sort-label" for="sort-select">Ordenar</label>
            <select class="sort-select" id="sort-select">
              <option value="name-asc">Nome (A–Z)</option>
              <option value="price-asc">Menor preço</option>
              <option value="price-desc">Maior preço</option>
            </select>
          </div>
        </div>

        <div id="grid-root"></div>
      </section>`;

    const searchInput = app.querySelector('#search-input');
    const sortSelect = app.querySelector('#sort-select');
    sortSelect.value = App.sortBy;

    app.querySelector('#hero-cta').addEventListener('click', () => {
      const target = document.getElementById('catalogo');
      if (target) target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });

    searchInput.addEventListener('input', (e) => {
      App.searchTerm = e.target.value;
      renderGrid(app, catId);
      handleSuggest(e.target.value, app);
    });

    searchInput.addEventListener('blur', () => {
      const box = app.querySelector('#suggest');
      window.setTimeout(() => { if (box) box.hidden = true; }, 180);
    });

    sortSelect.addEventListener('change', (e) => {
      App.sortBy = e.target.value;
      renderGrid(app, catId);
    });

    app.querySelector('#grid-root').addEventListener('click', (e) => {
      const btn = e.target.closest('[data-add]');
      if (!btn) return;
      const product = App.products.find((p) => p.id === btn.dataset.add);
      if (product) {
        Cart.add(product, 1);
        Utils.toast(`“${product.name}” adicionado ao carrinho.`, 'success');
      }
    });

    await ensureProducts(app);
    renderGrid(app, catId);
  }

  async function ensureProducts(app) {
    if (App.catalogState === 'ready' || App.catalogState === 'loading') return;
    App.catalogState = 'loading';
    const root = app.querySelector('#grid-root');
    if (root) root.innerHTML = spinnerHtml();
    try {
      App.products = await Api.getAllActiveProducts();
      App.catalogState = 'ready';
    } catch (err) {
      App.catalogState = 'error';
      const r = app.querySelector('#grid-root');
      if (r) {
        r.innerHTML = emptyHtml(
          'Não foi possível carregar o catálogo',
          'Verifique se o backend está no ar e tente novamente.',
          '<button class="btn btn--solid" type="button" onclick="location.reload()">Tentar novamente</button>'
        );
      }
      Utils.toast(err.message || 'Erro ao carregar produtos.', 'error');
    }
  }

  function renderGrid(app, catId) {
    const root = app.querySelector('#grid-root');
    const count = app.querySelector('#catalog-count');
    const title = app.querySelector('#catalog-title');
    if (!root) return;

    if (App.catalogState === 'loading') {
      root.innerHTML = spinnerHtml();
      if (count) count.textContent = '';
      return;
    }
    if (App.catalogState === 'error') {
      if (count) count.textContent = '';
      return;
    }

    let list = App.products.slice();
    if (catId) list = list.filter((p) => p.categoryId === catId);
    const term = Utils.normalize(App.searchTerm);
    if (term) list = list.filter((p) => Utils.normalize(p.name).includes(term));
    list = sortProducts(list, App.sortBy);

    const catName = catId && App.categoryById[catId] ? App.categoryById[catId].name : 'Catálogo';
    if (title) title.textContent = catName;
    if (count) count.textContent = `${list.length} ${list.length === 1 ? 'produto' : 'produtos'}`;

    if (!list.length) {
      const isFiltered = term || catId;
      root.innerHTML = emptyHtml(
        isFiltered ? 'Nenhum produto encontrado' : 'Catálogo em preparo',
        isFiltered
          ? 'Tente outro termo de busca ou escolha outra categoria.'
          : 'Ainda não há produtos disponíveis. Volte em breve.',
        isFiltered ? '<button class="btn btn--ghost" type="button" id="clear-filters">Limpar filtros</button>' : ''
      );
      const clearBtn = root.querySelector('#clear-filters');
      if (clearBtn) clearBtn.addEventListener('click', () => { App.searchTerm = ''; location.hash = '/'; });
      return;
    }

    root.innerHTML = `<div class="grid">${list.map(productCardHtml).join('')}</div>`;
  }

  function productCardHtml(product) {
    const cat = App.categoryById[product.categoryId];
    const unit = Utils.unitLabel(product.unitMeasure);
    const tags = [];
    if (unit) tags.push(`<span class="tag tag--gold">${Utils.escapeHtml(unit)}</span>`);
    if (cat) tags.push(`<span class="tag">${Utils.escapeHtml(cat.name)}</span>`);

    return `
      <article class="card">
        <a class="card-media" href="#/produto/${product.id}" aria-label="${Utils.escapeHtml(product.name)}">
          ${imageTag(product.imageUrl, product.name, product.name)}
          <span class="card-tags">${tags.join('')}</span>
        </a>
        <div class="card-body">
          <a class="card-title" href="#/produto/${product.id}">${Utils.escapeHtml(product.name)}</a>
          <p class="card-desc">${Utils.escapeHtml(product.description || '')}</p>
          <div class="card-foot">
            <span class="card-price">${Utils.fmtBRL(product.price)}</span>
            <button class="card-add" type="button" data-add="${product.id}" aria-label="Adicionar ${Utils.escapeHtml(product.name)} ao carrinho">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" aria-hidden="true">
                <path d="M12 5v14M5 12h14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>
      </article>`;
  }

  function handleSuggest(term, app) {
    const box = app.querySelector('#suggest');
    if (!box) return;
    const q = (term || '').trim();
    if (q.length < 2) {
      box.hidden = true;
      box.innerHTML = '';
      return;
    }
    Api.searchProducts(q)
      .then((results) => {
        const current = app.querySelector('#search-input');
        if (!current || current.value.trim() !== term.trim()) return;
        if (!results || !results.length) {
          box.hidden = true;
          box.innerHTML = '';
          return;
        }
        box.innerHTML = results
          .slice(0, 8)
          .map(
            (r) =>
              `<a class="suggest-item" href="#/produto/${Utils.escapeHtml(r.id)}">` +
              imageTag(r.imageUrl, r.name, r.name, 'suggest-thumb') +
              `<span class="suggest-name">${Utils.escapeHtml(r.name)}</span></a>`
          )
          .join('');
        box.hidden = false;
      })
      .catch(() => {
        
      });
  }

  // produto
  async function renderProduct(app, id) {
    app.innerHTML = `
      <div class="container section">
        <div class="breadcrumb"><a href="#/">Loja</a> &nbsp;/&nbsp; <span>Produto</span></div>
        <div id="detail-root"></div>
      </div>`;
    const root = app.querySelector('#detail-root');
    root.innerHTML = spinnerHtml();
    try {
      // Garante o catálogo carregado (cobre deep-link direto a #/produto/...).
      await ensureProducts(app);

      // Usa primeiro o produto já carregado (estático do products.json ou da API).
      let product = App.products.find((p) => String(p.id) === String(id));

      // Fallback: produto do backend não presente no catálogo paginado.
      if (!product) {
        product = await Api.getProduct(id);
      }

      renderDetail(root, product);
    } catch (err) {
      root.innerHTML = `
        <div class="notfound">
          <h1>404</h1>
          <p>${Utils.escapeHtml(err.message || 'Produto não encontrado.')}</p>
          <a class="btn btn--solid" href="#/">Voltar à loja</a>
        </div>`;
    }
  }

  function renderDetail(root, product) {
    const cat = App.categoryById[product.categoryId];
    const unit = Utils.unitLabel(product.unitMeasure);
    const lowStock = product.quantity != null && Number(product.quantity) <= 5;

    root.innerHTML = `
      <div class="detail">
        <div class="detail-media">${imageTag(product.imageUrl, product.name, product.name)}</div>
        <div class="detail-body">
          <div class="detail-tags">
            ${cat ? `<span class="tag">${Utils.escapeHtml(cat.name)}</span>` : ''}
            ${unit ? `<span class="tag tag--gold">${Utils.escapeHtml(unit)}</span>` : ''}
          </div>
          <h1 class="detail-title">${Utils.escapeHtml(product.name)}</h1>
          <p class="detail-desc">${Utils.escapeHtml(product.description || '')}</p>
          <div class="detail-price">${Utils.fmtBRL(product.price)}<small>${unit ? 'por ' + Utils.escapeHtml(unit.toLowerCase()) : ''}</small></div>

          <div class="qty-row">
            <div class="qty">
              <button class="qty-btn" type="button" id="qty-minus" aria-label="Diminuir quantidade">−</button>
              <input class="qty-input" id="qty-input" type="number" value="1" min="1" step="1" inputmode="numeric" aria-label="Quantidade" />
              <button class="qty-btn" type="button" id="qty-plus" aria-label="Aumentar quantidade">+</button>
            </div>
            <button class="btn btn--solid btn--lg" id="add-to-cart" type="button">Adicionar ao carrinho</button>
          </div>

          ${
            product.quantity != null
              ? `<p class="stock-note ${lowStock ? 'stock-note--low' : ''}">${lowStock ? 'Últimas unidades! ' : ''}Disponibilidade: ${product.quantity} em estoque</p>`
              : ''
          }
        </div>
      </div>`;

    const qtyInput = root.querySelector('#qty-input');
    const getQty = () => Math.max(1, Math.floor(Number(qtyInput.value) || 1));

    root.querySelector('#qty-minus').addEventListener('click', () => { qtyInput.value = getQty() - 1; });
    root.querySelector('#qty-plus').addEventListener('click', () => { qtyInput.value = getQty() + 1; });
    qtyInput.addEventListener('change', () => { qtyInput.value = getQty(); });

    root.querySelector('#add-to-cart').addEventListener('click', () => {
      const q = getQty();
      Cart.add(product, q);
      Utils.toast(`${q}× ${product.name} no carrinho.`, 'success');
      qtyInput.value = 1;
    });
  }

  // carrinho
  function renderCartPage(app) {
    const items = Cart.get();
    app.innerHTML = `<div class="container section"><h1 class="section-title" style="margin-bottom:22px">Seu carrinho</h1><div id="cart-root"></div></div>`;
    const root = app.querySelector('#cart-root');

    if (!items.length) {
      root.innerHTML = emptyHtml('Seu carrinho está vazio', 'Explore o catálogo e adicione cafés de origem.', '<a class="btn btn--solid" href="#/">Ver produtos</a>');
      return;
    }

    const subtotal = Cart.subtotal();
    root.innerHTML = `
      <div class="cart-page">
        <div class="cart-list">${items.map(cartItemHtml).join('')}</div>
        <aside class="cart-summary">
          <h2>Resumo</h2>
          <div class="summary-row"><span>Subtotal</span><span>${Utils.fmtBRL(subtotal)}</span></div>
          <div class="summary-row"><span>Frete</span><span>Calculado na finalização</span></div>
          <div class="summary-total"><span>Total</span><span>${Utils.fmtBRL(subtotal)}</span></div>
          <p class="summary-note">O valor final é confirmado pelo backend na hora do pagamento — nenhum preço é calculado no navegador.</p>
          <a class="btn btn--solid btn--block" href="#/checkout">Finalizar compra</a>
        </aside>
      </div>`;

    root.addEventListener('click', (e) => {
      const dec = e.target.closest('[data-dec]');
      const inc = e.target.closest('[data-inc]');
      const rem = e.target.closest('[data-remove]');
      if (dec) {
        const item = items.find((i) => i.productId === dec.dataset.dec);
        if (item) { Cart.setQuantity(item.productId, item.quantity - 1); renderCartPage(app); }
      } else if (inc) {
        const item = items.find((i) => i.productId === inc.dataset.inc);
        if (item) { Cart.setQuantity(item.productId, item.quantity + 1); renderCartPage(app); }
      } else if (rem) {
        Cart.remove(rem.dataset.remove);
        renderCartPage(app);
      }
    });
  }

  function cartItemHtml(item) {
    return `
      <div class="cart-item">
        <a class="cart-item-media" href="#/produto/${Utils.escapeHtml(item.productId)}">${imageTag(item.imageUrl, item.name, item.name)}</a>
        <div class="cart-item-body">
          <a class="cart-item-name" href="#/produto/${Utils.escapeHtml(item.productId)}">${Utils.escapeHtml(item.name)}</a>
          <span class="cart-item-price">${Utils.fmtBRL(item.price)} cada</span>
          <div class="qty" style="margin-top:6px">
            <button class="qty-btn" type="button" data-dec="${Utils.escapeHtml(item.productId)}" aria-label="Diminuir quantidade">−</button>
            <span class="qty-input qty-value">${item.quantity}</span>
            <button class="qty-btn" type="button" data-inc="${Utils.escapeHtml(item.productId)}" aria-label="Aumentar quantidade">+</button>
          </div>
        </div>
        <div class="cart-item-actions">
          <span class="line-total">${Utils.fmtBRL(item.price * item.quantity)}</span>
          <button class="cart-remove" type="button" data-remove="${Utils.escapeHtml(item.productId)}">Remover</button>
        </div>
      </div>`;
  }

  // checkout
  function renderCheckout(app) {
    const items = Cart.get();
    app.innerHTML = `<div class="container section"><h1 class="section-title" style="margin-bottom:22px">Checkout</h1><div id="checkout-root"></div></div>`;
    const root = app.querySelector('#checkout-root');

    if (!items.length) {
      root.innerHTML = emptyHtml('Nada para pagar', 'Seu carrinho está vazio.', '<a class="btn btn--solid" href="#/">Ver produtos</a>');
      return;
    }

    const subtotal = Cart.subtotal();
    root.innerHTML = `
      <div class="checkout-page">
        <div class="order-summary">
          <h2>Resumo do pedido</h2>
          ${items
            .map(
              (i) =>
                `<div class="order-line"><span class="ol-name">${Utils.escapeHtml(i.name)} <small>×${i.quantity}</small></span><span class="ol-total">${Utils.fmtBRL(i.price * i.quantity)}</span></div>`
            )
            .join('')}
          <div class="summary-total"><span>Total</span><span>${Utils.fmtBRL(subtotal)}</span></div>
        </div>

        <div class="pay-box">
          <h2>Pagamento</h2>
          <p class="pay-note">Você será redirecionado para a página segura da Stripe. O valor cobrado é sempre o preço salvo no nosso sistema.</p>
          <button class="btn btn--solid btn--lg btn--block" id="pay-btn" type="button">Pagar agora</button>
          <span class="pay-lock">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" aria-hidden="true">
              <rect x="5" y="10.5" width="14" height="9.5" rx="2" stroke="currentColor" stroke-width="1.7"/>
              <path d="M8 10.5V8a4 4 0 0 1 8 0v2.5" stroke="currentColor" stroke-width="1.7"/>
            </svg>
            Pagamento criptografado via Stripe
          </span>
        </div>
      </div>`;

    root.querySelector('#pay-btn').addEventListener('click', () => handlePay(root.querySelector('#pay-btn')));
  }

  async function handlePay(button) {
    if (!Cart.get().length) {
      Utils.toast('Seu carrinho está vazio.', 'error');
      return;
    }
    const original = button.textContent;
    button.disabled = true;
    button.textContent = 'Verificando sessão…';

    try {
      await Auth.init();

      if (!Auth.isAuthenticated()) {
        if (!Auth.available()) {
          Utils.toast('Serviço de login indisponível (keycloak-js não carregou).', 'error');
          button.disabled = false;
          button.textContent = original;
          return;
        }
        Auth.login('#/checkout');
        return; // o usuário volta autenticado e clica em "Pagar" de novo
      }

      button.textContent = 'Criando pagamento…';
      const token = await Auth.ensureToken(30);
      if (!token) {
        Auth.login('#/checkout');
        return;
      }

      const items = Cart.get().map((i) => ({ productId: i.productId, quantity: i.quantity }));
      const data = await Api.createCheckoutSession(items, token);

      if (data && data.url) {
        window.location.href = data.url;
      } else {
        throw new Error('Resposta inesperada ao criar o pagamento.');
      }
    } catch (err) {
      Utils.toast(err.message || 'Não foi possível iniciar o pagamento.', 'error');
      button.disabled = false;
      button.textContent = original;
    }
  }

  // roteador 
  async function route() {
    renderNav(App.categories);
    const { path } = parseHash();
    const app = document.getElementById('app');
    window.scrollTo(0, 0);

    if (path === '/' || path === '') await renderHome(app);
    else if (path.startsWith('/produto/')) await renderProduct(app, path.split('/')[2]);
    else if (path === '/carrinho') renderCartPage(app);
    else if (path === '/checkout') renderCheckout(app);
    else renderNotFound(app);
  }

  function renderNotFound(app) {
    app.innerHTML = `
      <div class="container section notfound">
        <h1>404</h1>
        <p>Página não encontrada.</p>
        <a class="btn btn--solid" href="#/">Voltar à loja</a>
      </div>`;
  }

  // init
  async function init() {
    const yearEl = document.getElementById('year');
    if (yearEl) yearEl.textContent = new Date().getFullYear();

    const authPromise = Auth.init().catch(() => false);

    try {
      App.categories = (await Api.getCategories()) || [];
    } catch (err) {
      App.categories = [];
      console.warn('[app] não foi possível carregar as categorias.', err);
    }
    App.categoryById = Object.fromEntries(App.categories.map((c) => [c.id, c]));

    renderNav(App.categories);
    renderCartCount();
    renderAuth();

    window.addEventListener('hashchange', route);
    window.addEventListener('cart:change', renderCartCount);
    document.addEventListener('click', (e) => {
      const box = document.getElementById('suggest');
      if (box && !box.hidden && !e.target.closest('.search-box')) box.hidden = true;
    });

    await route();

    authPromise.then(() => {
      renderAuth();
      if (Auth.isAuthenticated()) {
        syncCurrentUser();
      }
      const dest = Auth.consumePostLoginRedirect();
      if (dest && Auth.isAuthenticated()) {
        location.hash = dest;
      }
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
