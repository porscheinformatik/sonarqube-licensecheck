<template>
  <div class="boxed-group boxed-group-inner">
    <h3>Licenses</h3>
    <table class="data zebra">
      <caption>This is a list of all licenses used in any dependencies listed below.</caption>
      <thead>
        <tr>
          <th v-for="license in columns" v-bind:key="license" v-on:click="sort(license)" scope="col"> {{license}} 
            <div class="arrow" v-if="license == sortBy" v-bind:class="{ 'arrow_up' : sortDirection === 'asc', 'arrow_down' : sortDirection === 'desc'}"></div>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(license) in sortedLicenses" v-bind:key="license">
          <td>{{license.identifier}}</td>
          <td>{{license.name}}</td>
          <td>
            <span :class="{ 'icon-license-ok': license.status === 'true', 'icon-license-nok': license.status !== 'true' }"></span>
            {{ license.status === 'true' ? 'Allowed' : 'Forbidden' }}
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
      sortBy: 'status',
      sortDirection: 'asc'
    };
  },
  computed: {
    sortedLicenses() {
      return this.licenses.sort((a, b) => {
        let modifier = 1;
        if (this.sortDirection === 'desc') modifier = -1;
        if (a[this.sortBy] < b[this.sortBy]) return -1 * modifier;
        if (a[this.sortBy] > b[this.sortBy]) return 1 * modifier;
        return 0;
      });
    },
    "columns": function columns() {
      if (this.licenses.length == 0) {
        return [];
      }
      return Object.keys(this.licenses[0])
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
