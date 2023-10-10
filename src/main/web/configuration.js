import Vue from 'vue';
import ModalDialog from './modal-dialog';
import Configuration from './configuration/configuration.vue';
import VueSVGIcon from 'vue-svgicon'

Vue.use(VueSVGIcon)

window.registerExtension('licensecheck/configuration', function (options) {

  Vue.component('modal-dialog', ModalDialog);
  const app = new Vue({
    el: options.el,
    // Cannot use `template`. Eval is blocked by CSP
    render(createElement) {
      return createElement(Configuration, {
        props: {
          options
        }
      });
    }
  });

  window.addEventListener('popstate', app.routeListener);

  app.routeListener();

  return function () {
    window.removeEventListener('popstate', app.routeListener);
    app.$destroy();
  };

});
