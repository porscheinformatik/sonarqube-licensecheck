<template>
  <div>
    <header class="page-header">
      <h1 class="page-title">License Check - Maven Licenses</h1>
      <div class="page-description">Map strings entered in Maven POM license name to licenses.</div>
      <div class="page-actions">
        <div class="button-group">
          <button id="license-add" @click="showAddDialog()">Add Maven License</button>
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
            <th>License Text Regex</th>
            <th>License</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in displayedItems" :key="item.key">
            <td>{{item.regex}}</td>
            <td>{{item.license}} / {{item.licenseName}}</td>
            <td>
              <a class="icon-edit" @click="showEditDialog(item)" title="Edit Maven License"></a>
              <a class="icon-delete" @click="showDeleteDialog(item)" title="Delete Maven License"></a>
            </td>
          </tr>
          <tr v-show="!displayedItems.length">
            <td colspan="3">No Maven licenses available</td>
          </tr>
        </tbody>
      </table>
    </div>
    <modal-dialog :header="editMode === 'add' ? 'Add Maven License' : 'Edit Maven License'" :show="!!itemToEdit" @close="cancelEdit()">
      <div slot="body" v-if="itemToEdit">
        <div class="modal-field">
          <label for="regexEdit">License Text Regex<em class="mandatory">*</em></label>
          <input required v-focus v-model="itemToEdit.regex" id="regexEdit" name="regexEdit" type="text" size="50"
            maxlength="255">
        </div>
        <div class="modal-field">
          <label for="licenseSelect">License<em class="mandatory">*</em></label>
          <select required v-model="itemToEdit.license" id="licenseSelect" name="licenseSelect">
            <option v-for="license in licenses" v-bind:value="license.identifier" v-bind:key="license.identifier">
              {{ license.identifier }} / {{ license.name }}
            </option>
          </select>
        </div>
      </div>
      <span slot="footer"><button @click="saveItem(itemToEdit)">Save</button></span>
    </modal-dialog>
    <modal-dialog header="Delete Maven License" :show="!!itemToDelete" @close="cancelDelete()">
      <div slot="body" v-if="itemToDelete">Are you sure you want to delete the Maven license mapping &quot;{{itemToDelete.regex}}&quot; / &quot;{{itemToDelete.license}}&quot;?</div>
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
      searchText: null,
      licenses: []
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
          item.rgex.toLowerCase().indexOf(search) >= 0 ||
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
        .getJSON("/api/licensecheck/licenses/show")
        .then(response => {
          this.licenses = response;
        })
        .then(this.loadMavenLicenses);
    },
    loadMavenLicenses() {
      window.SonarRequest
        .getJSON("/api/licensecheck/maven-licenses/show")
        .then(response => {
          this.items = response.mavenLicenses.map(item => {
            let license = this.licenses.find(license => license.identifier === item.license);
            if (license) {
              item.licenseName = license.name;
            }
            return item;
          });
        });
    },
    showAddDialog() {
      this.itemToEdit = {};
      this.editMode = 'add';
    },
    showEditDialog(item) {
      this.itemToEdit = Object.assign({ old_regex: item.regex }, item);
      this.editMode = 'edit';
    },
    cancelEdit() {
      this.itemToEdit = null;
    },
    saveItem(item) {
      window.SonarRequest
        .post(`/api/licensecheck/maven-licenses/${this.editMode}`, item)
        .then(() => {
          this.loadMavenLicenses();
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
        .post('/api/licensecheck/maven-licenses/delete', { regex: item.regex })
        .then(() => {
          this.loadMavenLicenses();
        });
      this.itemToDelete = null;
    }
  },
  directives: { focus }
}
</script>
