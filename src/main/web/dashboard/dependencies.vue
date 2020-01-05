<template>
  <div class="boxed-group boxed-group-inner">
    <h3>Dependencies</h3>
    <table class="data zebra">
      <caption>Here you see all project dependencies from Maven (including transitive) and NPM.</caption>
      <thead>
        <tr>
          <th @click="sort('name')" scope="col">Name</th>
          <th @click="sort('version')" scope="col">Version</th>
          <th @click="sort('license')" scope="col">License</th>
          <th @click="sort('status')" scope="col">Status</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(dependency) in sortedDependencies" v-bind:key="dependency.name">
          <td>{{dependency.name}}</td>
          <td>{{dependency.version}}</td>
          <td>{{dependency.license}}</td>
          <td>
            <span
              :class="{ 'icon-license-ok': dependency.status === 'Allowed', 'icon-license-nok': dependency.status === 'Forbidden', 'icon-license-unknown': dependency.status === 'Unknown' }"
            ></span>
            {{dependency.status}}
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

  <script>
export default {
  props: ["dependencies"],
  data() {
    return {
      dependency: {
        name: "",
        version: "",
        license: "",
        status: ""
      },
      sortBy: "name",
      sortDirection: "asc"
    };
  },
  computed: {
    sortedDependencies() {
      return this.dependencies.sort((a, b) => {
        let modifier = 1;
        if (this.sortDirection === "desc") modifier = -1;
        if (a[this.sortBy] < b[this.sortBy]) return -1 * modifier;
        if (a[this.sortBy] > b[this.sortBy]) return 1 * modifier;
        return 0;
      });
    }
  },
  methods: {
    sort(param) {
      if (param === this.sortBy) {
        this.sortDirection = this.sortDirection === "asc" ? "desc" : "asc";
      }
      this.sortBy = param;
    }
  }
};
</script>

<style>
@import "icons.css";
</style>
