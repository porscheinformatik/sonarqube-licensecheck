import Vue from 'vue';
import Licenses from './dashboard/licenses.vue';
import Dependencies from './dashboard/dependencies.vue';
import saveAs from 'file-saverjs';
import buildExcel from './dashboard/excel-builder';
 
window.registerExtension('licensecheck/dashboard', function (options) {

  const app = new Vue({
    el: options.el,
    data: () => {
      return {
        licenses: [],
        dependencies: [],
        component: options.component
      }
    },
    created() {
      window.SonarRequest
        .getJSON(`/api/measures/search?projectKeys=${this.component.key}&metricKeys=licensecheck.license,licensecheck.dependency`)
        .then(response => {
          response.measures.forEach(measure => {
            if (measure.metric === 'licensecheck.license') {
              this.licenses = JSON.parse(measure.value);
            } else if (measure.metric === 'licensecheck.dependency') {
              this.dependencies = JSON.parse(measure.value);
            }
          });
          this.dependencies.forEach(dependency => {
            dependency.status = 'Unknown';
            this.licenses.forEach(license => {
              if (dependency.license === license.identifier) {
                dependency.status = license.status === 'true' ? 'Allowed' : 'Forbidden';
              }
            });
          });
        });
    },
    methods: {
      exportExcel() {
        const blob = new Blob([buildExcel(this.dependencies, this.licenses)], {type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"});
        saveAs(blob, `license-check-${this.component.key}.xls`);
      }
    },
    template: `<div class="page page-limited">
  <h1>License Check</h1>
  <div><a href="#" v-on:click="exportExcel()">Export to Excel</a></div>
  <p>&nbsp;</p>
  <dependencies :dependencies="dependencies"></dependencies>
  <p>&nbsp;</p>
  <licenses :licenses="licenses"></licenses>
</div>`,
    components: { Licenses, Dependencies },
  });

  return function () {
    app.$destroy();
  };

});
