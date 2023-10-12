import Vue from 'vue';
import Dashboard from './dashboard/dashboard.vue';
 
window.registerExtension('licensecheck/dashboard', function (options) {

  const app = new Vue({
    el: options.el,
    // Cannot use `template`. Eval is blocked by CSP
    render(createElement) {
      return createElement(Dashboard, {
        props: {
          options
        }
      })
    }
  });

  return function () {
    app.$destroy();
  };

});
