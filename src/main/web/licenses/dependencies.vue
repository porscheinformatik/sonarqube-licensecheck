<template>
  <div>
    <h3>Dependencies</h3>
    <p>Here you see all project dependencies from Maven (including transitive) and NPM.</p>
    <table class="data zebra">
      <thead>
      <tr>
        <th>Name</th>
        <th>Version</th>
        <th>License</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="dependency in dependencies">
        <td>{{dependency.name}}</td>
        <td>{{dependency.version}}</td>
        <td>{{dependency.license}}</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
  export default {
    props: ['dependencies']
  }
</script>

<style>

</style>
