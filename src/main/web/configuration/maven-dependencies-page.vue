<template>
  <div class="boxed-group boxed-group-inner">
    <header class="page-header">
      <h1 class="page-title">License Check - Maven Dependencies</h1>
      <div class="page-description" id="page-description">Map maven identifiers (groupId/artifactId) to licenses.</div>
      <div class="page-actions">
        <button class="button" id="license-add" @click="showAddDialog()">Add Maven Dependency</button>
      </div>
    </header>
    <div class="panel panel-vertical bordered-bottom spacer-bottom">
      <div class="search-box">
        <svgicon icon="magnify" width="15" height="16" style="padding-left: 5px; margin-top: 4px; fill: #999;"></svgicon>
        <input style="background: none; width: 100%; border: none" v-model="searchText" class="search-box-input" type="search" maxlength="100" placeholder="Search" autocomplete="off">
      </div>
    </div>
    <div>
      <table class="data zebra" aria-describedby="page-description">
        <thead>
          <tr>
            <th @click="sort('key')" scope="col">Key Regex<div class="arrow" v-if="sortBy === 'key'" v-bind:class="{ 'arrow_up' : sortDirection === 'asc', 'arrow_down' : sortDirection === 'desc'}"></div></th>
            <th @click="sort('license')" scope="col">License<div class="arrow" v-if="sortBy === 'license'" v-bind:class="{ 'arrow_up' : sortDirection === 'asc', 'arrow_down' : sortDirection === 'desc'}"></div></th>
            <th scope="col">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in displayedItems" :key="item.key">
            <td>{{item.key}}</td>
            <td>{{item.license}} / {{item.licenseName}}</td>
            <td class="thin nowrap">
              <a class="button" @click="showEditDialog(item)" title="Edit item">
                <svgicon icon="pencil" width="16" height="16" style="fill: currentcolor"></svgicon>
              </a>
              <a class="button" @click="showDeleteDialog(item)" title="Delete item">
                <svgicon icon="delete" width="16" height="16" style="fill: rgb(212, 51, 63)"></svgicon>
              </a>
            </td>
          </tr>
          <tr v-show="!displayedItems.length">
            <td colspan="3">No Maven dependencies available</td>
          </tr>
        </tbody>
      </table>
    </div>
    <modal-dialog :header="editMode === 'add' ? 'Add Maven Dependency' : 'Edit Maven Dependency'" :show="!!itemToEdit" @close="cancelEdit()">
      <div slot="body" v-if="itemToEdit">
        <div class="modal-field">
          <label for="keyEdit">Key Regex<em class="mandatory">*</em></label>
          <input required v-focus v-model="itemToEdit.key" id="keyEdit" name="keyEdit" type="text" size="50"
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
      <span slot="footer"><button class="button" @click="saveItem(itemToEdit)">Save</button></span>
    </modal-dialog>
    <modal-dialog header="Delete Maven Dependency" :show="!!itemToDelete" @close="cancelDelete()">
      <div slot="body" v-if="itemToDelete">Are you sure you want to delete the Maven dependency mapping &quot;{{itemToDelete.key}}&quot; / &quot;{{itemToDelete.license}}&quot;?</div>
      <span slot="footer"><button class="button" @click="deleteItem(itemToDelete)">Delete</button></span>
    </modal-dialog>
  </div>
</template>

<script>
import '../../../compiled-icons';

export default {
  data() {
    return {
      items: [],
      itemToDelete: null,
      itemToEdit: null,
      editMode: null,
      searchText: null,
      licenses: [],
      sortBy: "key",
      sortDirection: "asc"
    };
  },
  computed: {
    displayedItems() {
      if (!this.searchText || this.searchText.length === 0) {
        return this.sortedItems;
      }

      let search = this.searchText.toLowerCase();
      return this.sortedItems.filter(
        item =>
          item.key.toLowerCase().indexOf(search) >= 0 ||
          item.license.toLowerCase().indexOf(search) >= 0
      );
    },
    sortedItems() {
      return this.items.sort((a, b) => {
        let modifier = 1;
        if (this.sortDirection === "desc") modifier = -1;
        if (a[this.sortBy] < b[this.sortBy]) return -1 * modifier;
        if (a[this.sortBy] > b[this.sortBy]) return modifier;
        return 0;
      });
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
          this.loadMavenDependencies();
        });
    },
    loadMavenDependencies() {
      window.SonarRequest
        .getJSON("/api/licensecheck/maven-dependencies/show")
        .then(response => {
          this.items = response.mavenDependencies.map(item => {
            let license = this.licenses.find(usedLicense => usedLicense.identifier === item.license);
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
      this.itemToEdit = Object.assign({ old_key: item.key }, item);
      this.editMode = 'edit';
    },
    cancelEdit() {
      this.itemToEdit = null;
    },
    saveDependencies(){
      let dependenciesToPost = [];

      for (let i = 0; i < this.items.length; i++){
        dependenciesToPost.push(new Object({nameMatches: this.items[i].key, license: this.items[i].license}));
      }

      let post = {
        'key': "licensecheck.alloweddependencies",
        'values': JSON.stringify(dependenciesToPost).replaceAll(",", "COMMA_PLACEHOLDER")
      };

      window.SonarRequest
        .post(`/api/settings/set`, post)
        .then(() => {
          this.loadMavenDependencies();
        });
    },
    saveItem(item) {
      if (this.editMode === 'add') {
        if (this.items.filter(dependency => dependency.key === item.key).length >= 1) {
          //key (!) already available
          return;
        }

        this.items.push(item);
      }
      else {
        if (this.items.filter(dependency => dependency.key === item.key && dependency.license === item.license).length === 1) {
          //nothing has changed
          return;
        }

        //remove old key/item
        this.items = this.items.filter(dependency => dependency.key !== item.old_key);
        //add new key/item
        this.items.push(item);
      }
      this.saveDependencies()
      this.itemToEdit = null;
    },
    showDeleteDialog(item) {
      this.itemToDelete = item;
    },
    cancelDelete() {
      this.itemToDelete = null;
    },
    deleteItem(item) {
      if (this.items.filter(dependency => dependency.key === item.key).length === 0) {
        //key not found
        return;
      }

      this.items = this.items.filter(dependency => dependency.key !== item.key);

      this.saveDependencies();

      this.itemToDelete = null;
    },
    sort(param) {
      if (param === this.sortBy) {
        this.sortDirection = this.sortDirection === "asc" ? "desc" : "asc";
      }
      this.sortBy = param;
    }
  },
  directives: { focus }
};
</script>
<style>
  @import "../dashboard/icons.css";
</style>
