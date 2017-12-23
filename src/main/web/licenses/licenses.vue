<template>
  <div>
    <h3>Licenses</h3>
    <p>This is a list of all licenses used in any dependencies listed above.</p>
    <table class="data zebra">
      <thead>
      <tr>
        <th>Name</th>
        <th>Identifier</th>
        <th>Allowed</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="license in licenses">
        <td>{{license.name}}</td>
        <td>{{license.identifier}}</td>
        <td>{{license.status}}</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
  export default {
    props: ['licenses']
  }
</script>

<style>

</style>
