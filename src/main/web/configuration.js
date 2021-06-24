import Vue from 'vue';
import LicensesPage from './configuration/licenses-page';
import DependencyMappingsPage from './configuration/dependency-mappings-page';
import LicenseMappingsPage from './configuration/license-mappings-page';
import ProjectLicensesPage from './configuration/project-licenses-page';
import ModalDialog from './modal-dialog';
import VueSVGIcon from 'vue-svgicon'

Vue.use(VueSVGIcon)

window.registerExtension('licensecheck/configuration', function (options) {

  Vue.component('modal-dialog', ModalDialog);
  const app = new Vue({
    el: options.el,
    data: () => {
      return {
        currentRoute: 'licenses'
      }
    },
    methods: {
      activateCategory(event, route) {
        event.preventDefault();
        this.currentRoute = route;
        window.history.pushState({}, document.title, `?category=${route}`)
      },
      routeListener() {
        const result = window.location.search.match(/category=([^&=]*)/);
        if (result && result.length > 1) {
          this.currentRoute = result[1];
        } else {
          this.currentRoute = 'licenses';
        }
      }
    },
    components: { LicensesPage, DependencyMappingsPage, LicenseMappingsPage, ProjectLicensesPage},
    template: `<div class="page page-limited">
  <ul class="tabs">
    <li><a href="?category=licenses" :class="{selected: currentRoute==='licenses'}" @click="activateCategory($event, 'licenses')">Licenses</a></li>
    <li><a href="?category=project-licenses" :class="{selected: currentRoute==='project-licenses'}" @click="activateCategory($event, 'project-licenses')">Project Licenses</a></li>
    <li><a href="?category=dependency-mappings" :class="{selected: currentRoute==='dependency-mappings'}" @click="activateCategory($event, 'dependency-mappings')">Dependency Mappings</a></li>
    <li><a href="?category=license-mappings" :class="{selected: currentRoute==='license-mappings'}" @click="activateCategory($event, 'license-mappings')">License Mappings</a></li>
  </ul>
  <br>
  <licenses-page v-if="currentRoute === 'licenses'">
  </licenses-page>
  <dependency-mappings-page v-if="currentRoute === 'dependency-mappings'">
  </dependency-mappings-page>
  <license-mappings-page v-if="currentRoute === 'license-mappings'">
  </license-mappings-page>
  <project-licenses-page v-if="currentRoute === 'project-licenses'">
  </project-licenses-page>
</div>`
  });

  window.addEventListener('popstate', app.routeListener);

  app.routeListener();

  return function () {
    window.removeEventListener('popstate', app.routeListener);
    app.$destroy();
  };

});
