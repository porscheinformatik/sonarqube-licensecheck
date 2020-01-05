<template>
  <div class="boxed-group boxed-group-inner">
    <h3>Licenses</h3>
    <table class="data zebra">
      <caption>This is a list of all licenses used in any dependencies listed above.</caption>
      <thead>
        <tr>
          <th @click="sort('identifier')" scope="col">Identifier</th>
          <th @click="sort('name')" scope="col">Name</th>
          <th @click="sort('status')" scope="col">Allowed</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(license) in sortedLicenses" v-bind:key="license">
          <td>{{license.identifier}}</td>
          <td>{{license.name}}</td>
          <td>
            <span :class="{ 'icon-license-ok': license.status === 'true', 'icon-license-nok': license.status !== 'true' }"></span>
            {{license.status}}
          </td>
        </tr>
      </tbody>
    </table>
  </div>


</template>

<script>
  export default {
props: ["licenses"],
  data() {
    return {
      license: {
        identifier: "",
        name: "",
        status: ""
      },
      sortBy: "identifier",
      sortDirection: "asc"
    };
  },
  computed: {
    sortedLicenses() {
      return this.licenses.sort((a, b) => {
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
