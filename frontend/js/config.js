const isLocal = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';

window.CONFIG = {
  
  API_BASE_URL: isLocal 
    ? 'http://localhost:7070/api/v1' 
    : 'https://api.ai-r-coffee.gustavosdaniel.com/api/v1',

  keycloak: {

    url: isLocal 
      ? 'http://localhost:7072/auth' 
      : 'https://api.ai-r-coffee.gustavosdaniel.com/auth',
    realm: 'ai-r-coffee-realm',
    clientId: 'ai-r-coffee-app'
  },

  currency: 'BRL',

  locale: 'pt-BR',

  pageSize: 12,

  cartStorageKey: 'aircoffee.cart.v1'
};