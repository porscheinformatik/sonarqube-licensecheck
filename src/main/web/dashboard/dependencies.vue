<template>
  <div class="boxed-group boxed-group-inner">
    <h3>Dependencies</h3>
    <table class="data zebra">
      <caption>Here you see all project dependencies from Maven (including transitive) and NPM.</caption>
      <thead>
        <tr>
          <th v-for="dependency in columns" v-bind:key="dependency" v-on:click="sort(dependency)" scope="col"> {{dependency}} 
            <div class="arrow" v-if="dependency == sortByDep" v-bind:class="{ 'arrow_up' : sortDirectionDep === 'asc', 'arrow_down' : sortDirectionDep === 'desc'}"></div>
          </th>
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
      sortByDep: "status",
      sortDirectionDep: "desc"
    };
  },
  computed: {
    sortedDependencies() {
      return this.dependencies.sort((a, b) => {
        let modifier = 1;
        if (this.sortDirectionDep === "desc") modifier = -1;
        if (a[this.sortByDep] < b[this.sortByDep]) return -1 * modifier;
        if (a[this.sortByDep] > b[this.sortByDep]) return 1 * modifier;
        return 0;
      });
    },
    "columns": function columns() {
      if (this.dependencies.length == 0) {
        return [];
      }
      return Object.keys(this.dependencies[0])
    }
  },
  methods: {
    sort(param) {
      if (param === this.sortByDep) {
        this.sortDirectionDep = this.sortDirectionDep === "asc" ? "desc" : "asc";
      }
      this.sortByDep = param;
    }
  }
};
</script>

<style>
@import "icons.css";
</style>
