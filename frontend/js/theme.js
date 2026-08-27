// Theme 

(function () {
  var KEY = 'aircoffee.theme';

  function currentTheme() {
    return document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
  }

  function apply(theme, btn) {
    document.documentElement.setAttribute('data-theme', theme);
    try { localStorage.setItem(KEY, theme); } catch (e) { /* sem storage */ }
    if (btn) btn.setAttribute('aria-pressed', String(theme === 'dark'));
  }

  function init() {
    var btn = document.getElementById('theme-toggle');
    if (!btn) return;

    btn.addEventListener('click', function () {
      apply(currentTheme() === 'dark' ? 'light' : 'dark', btn);
    });

    btn.setAttribute('aria-pressed', String(currentTheme() === 'dark'));
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
