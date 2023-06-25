<template>
  <div class="page page-limited">
    <h1>License Check</h1>
    <div><a href="#" v-on:click="exportExcel()">Export to Excel</a></div>
    <p>&nbsp;</p>
    <licenses :licenses="licenses"></licenses>
    <p>&nbsp;</p>
    <dependencies :dependencies="dependencies"></dependencies>
  </div>
</template>

<script>
import Licenses from './licenses.vue';
import Dependencies from './dependencies.vue';
import saveAs from 'file-saverjs';
import buildExcel from './excel-builder';

export default {
  props: ["options"],
  data() {
    return {
      licenses: [],
      dependencies: [],
      component: this.options.component
    }
  },
  created() {
    let params = new URLSearchParams(window.location.search);
    let request = {

      component : this.component.key,
      metricKeys : "licensecheck.license,licensecheck.dependency"
    };
    if (params.has("branch")) {
      request.branch = params.get("branch");
    } else if (params.has("pullRequest")) {
      request.pullRequest = params.get("pullRequest");
    }
    window.SonarRequest
      .getJSON("/api/measures/component", request)
      .then(response => {
        response.component.measures.forEach(measure => {
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
  components: { Licenses, Dependencies },
};
</script>
