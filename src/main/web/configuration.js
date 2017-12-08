import Vue from 'vue';
import LicensesPage from './licenses-page';

window.registerExtension('licensecheck/configuration', function (options) {

  const app = new Vue({
    el: options.el,
    template: '<div class="page page-limited"><licenses-page></licenses-page></div>',
    components: { LicensesPage }
  });

  const routeListener = () => {
    app.currentRoute = window.location.pathname;
  };

  window.addEventListener('popstate', routeListener);

  return function () {
    app.$destroy();
    window.removeEventListener('popstate', routeListener);
  };

});
