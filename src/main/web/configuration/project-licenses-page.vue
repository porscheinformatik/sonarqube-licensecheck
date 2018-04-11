<template>
  <div>
    <header class="page-header">
      <h1 class="page-title">License Check - Project Licenses</h1>
      <div class="page-description">Allow/disallow licences for specific projects.</div>
      <div class="page-actions">
        <div class="button-group">
          <button id="license-add" @click="showAddDialog()">Add License</button>
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
            <th>Project</th>
            <th>License</th>
            <th>Actions</th>
            <th>Allowed</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in displayedItems" :key="item.key">
            <td><span :title="item.projectKey">{{item.projectName}}</span></td>
            <td>{{item.license}} / {{item.licenseName}}</td>
            <td>{{item.status}}</td>
            <td>
              <a class="icon-edit" @click="showEditDialog(item)" title="Edit License"></a>
              <a class="icon-delete" @click="showDeleteDialog(item)" title="Delete License"></a>
            </td>
          </tr>
          <tr v-show="!displayedItems.length">
            <td colspan="4">No project licenses available</td>
          </tr>
        </tbody>
      </table>
    </div>
    <modal-dialog :header="editMode === 'add' ? 'Add License' : 'Edit License'" :show="!!itemToEdit" @close="cancelEdit()">
      <div slot="body" v-if="itemToEdit">
        <div class="modal-field">
          <label for="projectSelect">Project<em class="mandatory">*</em></label>
          <select required :disabled="editMode !== 'add'" v-model="itemToEdit.projectKey" id="projectSelect" name="projectSelect">
            <option v-for="project in projects" v-bind:value="project.key" v-bind:key="project.key">
              {{ project.name }}
            </option>
          </select>
        </div>
        <div class="modal-field">
          <label for="licenseSelect">License<em class="mandatory">*</em></label>
          <select required :disabled="editMode !== 'add'" v-model="itemToEdit.license" id="licenseSelect" name="licenseSelect">
            <option v-for="license in licenses" v-bind:value="license.identifier" v-bind:key="license.identifier">
              {{ license.identifier }} / {{ license.name }}
            </option>
          </select>
        </div>
        <div class="modal-field">
          <label for="itemStatusEdit">Status<em class="mandatory">*</em></label>
          <select required v-model="itemToEdit.status" id="itemStatusEdit" name="itemStatusEdit">
            <option value="true">true</option>
            <option value="false">false</option>
          </select>
        </div>
      </div>
      <span slot="footer"><button @click="saveItem(itemToEdit)">Save</button></span>
    </modal-dialog>
    <modal-dialog header="Delete License" :show="!!itemToDelete" @close="cancelDelete()">
      <div slot="body" v-if="itemToDelete">Are you sure you want to delete the license mapping &quot;{{itemToDelete.projectName}}&quot; / &quot;{{itemToDelete.license}}&quot;?</div>
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
      licenses: [],
      projects: []
    };
  },
  computed: {
    displayedItems() {
      if (!this.searchText || this.searchText.length === 0) {
        return this.items;
      }

      let search = this.searchText.toLowerCase();
      return this.items.filter(
        item =>
          item.projectKey.toLowerCase().indexOf(search) >= 0 ||
          item.license.toLowerCase().indexOf(search) >= 0
      );
    }
  },
  created() {
    this.load();
  },
  methods: {
    load() {
      Promise.all([this.loadLicenses(), this.loadProjects()]).then(() => this.loadProjectLicenses());
    },
    loadProjectLicenses() {
      window.SonarRequest
        .getJSON("/api/licensecheck/project-licenses/show")
        .then(response => {
          this.items = response.map(item => {
            let license = this.licenses.find(license => license.identifier === item.license);
            if (license) {
              item.licenseName = license.name;
            }
            let project = this.projects.find(project => project.key === item.projectKey);
            if (project) {
              item.projectName = project.name;
            }
            return item;
          });
        });
    },
    loadLicenses() {
      return window.SonarRequest
        .getJSON("/api/licensecheck/licenses/show")
        .then(response => {
          this.licenses = response;
        });
    },
    loadProjects() {
      return window.SonarRequest
        .getJSON('/api/components/search?qualifiers=TRK&pageSize=10000')
        .then(response => {
          this.projects = response.components;
        });
    },
    showAddDialog() {
      this.loadProjects();
      this.itemToEdit = {};
      this.editMode = 'add';
    },
    showEditDialog(item) {
      this.loadProjects();
      this.itemToEdit = Object.assign({ old_regex: item.regex }, item);
      this.editMode = 'edit';
    },
    cancelEdit() {
      this.itemToEdit = null;
    },
    saveItem(item) {
      window.SonarRequest
        .post(`/api/licensecheck/project-licenses/${this.editMode}`, item)
        .then(() => {
          this.loadProjectLicenses();
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
        .post('/api/licensecheck/project-licenses/delete', { projectKey: item.projectKey, license: item.license })
        .then(() => {
          this.loadProjectLicenses();
        });
      this.itemToDelete = null;
    }
  },
  directives: { focus }
}
</script>
