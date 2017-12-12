<template>
  <div>
    <header class="page-header">
      <h1 class="page-title">License Check - Maven Dependencies</h1>
      <div class="page-description">Map maven identifiers (groupId/artifactId) to licenses.</div>
      <div class="page-actions">
        <div class="button-group">
          <button id="license-add" @click="showAddDialog()">Add Maven Dependency</button>
        </div>
      </div>
    </header>
    <div>
      <div class="panel panel-vertical bordered-bottom spacer-bottom">
        <button class="search-box-submit button-clean"><i class="icon-search"></i></button>
        <input v-model="searchText" class="search-box-input" type="search" maxlength="100" placeholder="Search" autocomplete="off">
      </div>
    </div>
    <div>
      <table class="data zebra" width="100%">
        <thead>
          <tr>
            <th>Key Regex</th>
            <th>License</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in displayedItems" :key="item.key">
            <td>{{item.key}}</td>
            <td>{{item.license}}</td>
            <td>
              <a class="icon-edit" @click="showEditDialog(item)" title="Edit Maven Dependency"></a>
              <a class="icon-delete" @click="showDeleteDialog(item)" title="Delete Maven Dependency"></a>
            </td>
          </tr>
          <tr v-show="!displayedItems.length">
            <td colspan="3">No Maven dependencies available</td>
          </tr>
        </tbody>
      </table>
    </div>
    <modal-dialog header="Delete Maven Dependency" :show="!!itemToDelete" @close="cancelDelete()">
      <div slot="body" v-if="itemToDelete">Are you sure you want to delete the Maven dependency mapping &quot;{{itemToDelete.key}}&quot; / &quot;{{itemToDelete.license}}&quot;?</div>
      <span slot="footer"><button @click="deleteItem(itemToDelete)">Delete</button></span>
    </modal-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      items: [],
      itemToDelete: null,
      itemToEdit: null,
      editMode: null,
      searchText: null
    };
  },
  computed: {
    displayedItems() {
      if (!this.searchText || this.searchText.length == 0) {
        return this.items;
      }

      let search = this.searchText.toLowerCase();
      return this.items.filter(
        item =>
          item.key.toLowerCase().indexOf(search) >= 0 ||
          item.license.toLowerCase().indexOf(search) >= 0
      );
    }
  },
  created() {
    this.load();
  },
  methods: {
    load() {
      window.SonarRequest
        .getJSON("/api/licensecheck/maven-dependencies/show")
        .then(response => {
          this.items = response.mavenDependencies;
        });
    },
    showAddDialog() {
      this.itemToEdit = {};
      this.editMode = 'add';
    },
    showEditDialog(item) {
      this.itemToEdit = Object.assign({}, item);
      this.editMode = 'edit';
    },
    cancelEdit() {
      this.itemToEdit = null;
    },
    saveItem(item) {
      window.SonarRequest
        .post(`/api/licensecheck/maven-dependencies/${this.editMode}`, item)
        .then(() => {
          this.load()
        });
      this.itemToEdit = null;
    },
    showDeleteDialog(item) {
      this.itemToDelete = item;
    },
    cancelDelete() {
      this.itemToDelete = null;
    },
    deleteItem(item) {
      window.SonarRequest
        .post('/api/licensecheck/maven-dependencies/delete', { identifier: item.identifier })
        .then(() => {
          this.load()
        });
      this.itemToDelete = null;
    }
  },
  directives: { focus }
};
</script>
