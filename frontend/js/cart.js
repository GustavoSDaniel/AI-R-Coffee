// Cart estado do carrinho, 100% no localStorage.

window.Cart = (() => {
  const KEY = window.CONFIG.cartStorageKey || 'aircoffee.cart.v1';

  function read() {
    try {
      const parsed = JSON.parse(localStorage.getItem(KEY));
      return Array.isArray(parsed) ? parsed : [];
    } catch (err) {
      return [];
    }
  }

  function write(items) {
    localStorage.setItem(KEY, JSON.stringify(items));
    emit();
  }

  function emit() {
    window.dispatchEvent(new CustomEvent('cart:change'));
  }

  function sanitize(item) {
    return {
      productId: item.productId,
      name: item.name || '',
      price: Number(item.price) || 0,
      imageUrl: item.imageUrl || '',
      quantity: Math.max(1, Math.floor(Number(item.quantity) || 1))
    };
  }

  return {
    get() {
      return read();
    },

    // Quantidade total de itens 
    count() {
      return read().reduce((acc, item) => acc + (Number(item.quantity) || 0), 0);
    },

    // Subtotal recalculado a partir da lista salva
    subtotal() {
      const total = read().reduce(
        (acc, item) => acc + (Number(item.price) || 0) * (Number(item.quantity) || 0),
        0
      );
      return Math.round(total * 100) / 100;
    },

    add(product, quantity = 1) {
      const items = read();
      const index = items.findIndex((item) => item.productId === product.id);
      if (index >= 0) {
        items[index].quantity += quantity;
        items[index].name = product.name;
        items[index].price = Number(product.price);
        items[index].imageUrl = product.imageUrl;
      } else {
        items.push(
          sanitize({
            productId: product.id,
            name: product.name,
            price: product.price,
            imageUrl: product.imageUrl,
            quantity
          })
        );
      }
      write(items);
    },

    setQuantity(productId, quantity) {
      const next = read().map((item) =>
        item.productId === productId ? { ...item, quantity: Math.max(1, quantity) } : item
      );
      write(next);
    },

    remove(productId) {
      write(read().filter((item) => item.productId !== productId));
    },

    clear() {
      localStorage.removeItem(KEY);
      emit();
    }
  };
})();
