// Auth  keycloak-js (v25).
 
window.Auth = (() => {
  let keycloak = null;
  let readyPromise = null;

  function cleanRedirectUri() {
    return window.location.origin + window.location.pathname;
  }

  function init() {
    if (readyPromise) return readyPromise;

    readyPromise = new Promise((resolve) => {
      if (typeof Keycloak === 'undefined') {
        console.warn('[auth] keycloak-js não carregado (CDN indisponível).');
        resolve(false);
        return;
      }

      try {
        keycloak = new Keycloak(window.CONFIG.keycloak);
      } catch (err) {
        console.warn('[auth] falha ao instanciar o Keycloak.', err);
        resolve(false);
        return;
      }

      keycloak
        .init({

          redirectUri: cleanRedirectUri(),
          pkceMethod: 'S256',
          scope: 'openid profile email'
          
        })

        .then((authenticated) => resolve(!!authenticated))
        .catch((err) => {
          console.warn('[auth] init falhou.', err);
          resolve(false);
        });
    });

    return readyPromise;
  }

  return {
    init,

    isAuthenticated() {
      return !!(keycloak && keycloak.authenticated);
    },

    available() {
      return !!keycloak;
    },

    // Redireciona para o login do Keycloak
  
    login(redirectHash) {
      if (!keycloak) {
        init().then(() => {
          if (keycloak) this.login(redirectHash);
          else Utils.toast('Serviço de login indisponível (keycloak-js não carregou).', 'error');
        });
        return;
      }
      if (redirectHash) {
        sessionStorage.setItem('aircoffee.postLoginRedirect', redirectHash);
      }
      keycloak.login({ redirectUri: cleanRedirectUri(), scope: 'openid profile email' });
    },

    logout() {
      if (!keycloak) return;
      keycloak.logout({ redirectUri: cleanRedirectUri() });
    },

    
    async ensureToken(minValidity = 30) {
      if (!keycloak || !keycloak.authenticated) return null;
      try {
        await keycloak.updateToken(minValidity);
      } catch (err) {
        console.warn('[auth] não foi possível renovar o token.', err);
        return null;
      }
      return keycloak.token || null;
    },

    
    consumePostLoginRedirect() {
      const dest = sessionStorage.getItem('aircoffee.postLoginRedirect');
      sessionStorage.removeItem('aircoffee.postLoginRedirect');
      return dest;
    },

    userLabel() {
      const parsed = keycloak && keycloak.tokenParsed;
      if (!parsed) return 'Conta';
      return parsed.preferred_username || parsed.name || parsed.email || 'Conta';
    }
  };
})();
