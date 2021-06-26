<template>
  <div class="boxed-group boxed-group-inner">
    <header class="page-header">
      <h1 class="page-title">License Check - Project Licenses</h1>
      <div class="page-description">Allow/disallow licences for specific projects.</div>
      <div class="page-actions">
        <button class="button" id="license-add" @click="showAddDialog()">Add License</button>
      </div>
    </header>
    <div class="panel panel-vertical bordered-bottom spacer-bottom">
      <div class="search-box">
        <svgicon icon="magnify" width="15" height="16" style="padding-left: 5px; margin-top: 4px; fill: #999;"></svgicon>
        <input style="background: none; width: 100%; border: none" v-model="searchText" class="search-box-input" type="search" maxlength="100" placeholder="Search" autocomplete="off">
      </div>
    </div>
    <div>
      <table class="data zebra">
        <caption>This is a list of all project specific licenses</caption>
        <thead>
          <tr>
            <th @click="sort('projectName')" scope="col">Project<div class="arrow" v-if="sortBy === 'projectName'" v-bind:class="{ 'arrow_up' : sortDirection === 'asc', 'arrow_down' : sortDirection === 'desc'}"></div></th>
            <th @click="sort('license')" scope="col">License<div class="arrow" v-if="sortBy === 'license'" v-bind:class="{ 'arrow_up' : sortDirection === 'asc', 'arrow_down' : sortDirection === 'desc'}"></div></th>
            <th @click="sort('status')" scope="col">Status<div class="arrow" v-if="sortBy === 'status'" v-bind:class="{ 'arrow_up' : sortDirection === 'asc', 'arrow_down' : sortDirection === 'desc'}"></div></th>
            <th scope="col">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in displayedItems" :key="item.key">
            <td><span :title="item.projectKey">{{findProjectName(item.projectKey)}}</span></td>
            <td>{{item.license}} / {{findLicenseName(item.license)}}</td>
            <td>
              <span :class="{ 'icon-license-ok': item.allowed === 'true', 'icon-license-nok': item.allowed === 'false' }"></span>
              {{item.allowed === 'true' ? 'Allowed': 'Forbidden'}}
            </td>
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
            <option v-for="license in licenses" v-bind:value="license.id" v-bind:key="license.id">
              {{ license.id }} / {{ license.name }}
            </option>
          </select>
        </div>
        <div class="modal-field">
          <label>Status<em class="mandatory">*</em></label>
          <label for="itemStatusEdit">
            <input type="checkbox" id="itemStatusEdit" name="itemStatusEdit" v-model="itemToEdit.allowed"
                   true-value="true" false-value="false">
            Allowed
          </label>
        </div>
      </div>
      <span slot="footer"><button class="button" @click="saveItem(itemToEdit)">Save</button></span>
    </modal-dialog>
    <modal-dialog header="Delete License" :show="!!itemToDelete" @close="cancelDelete()">
      <div slot="body" v-if="itemToDelete">Are you sure you want to delete the license mapping &quot;{{findProjectName(itemToDelete.projectKey)}}&quot; / &quot;{{itemToDelete.license}}&quot;?</div>
      <span slot="footer"><button class="button" @click="deleteItem(itemToDelete)">Delete</button></span>
    </modal-dialog>
  </div>
</template>

<script>
import '../../../compiled-icons';
import {loadLicenses, loadProjectLicenses, loadProjects, saveProjectLicenses} from "./sonar-api";

export default {
  data() {
    return {
      items: [],
      itemToDelete: null,
      itemToEdit: null,
      editMode: null,
      searchText: null,
      licenses: [],
      projects: [],
      sortBy: "status",
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
          item.projectKey.toLowerCase().indexOf(search) >= 0 ||
          item.license.toLowerCase().indexOf(search) >= 0
      );
    },
    sortedItems() {
      return this.items.sort((a, b) => {
        let modifier = 1;
        if (this.sortDirection === "desc") modifier = -1;
        if (a[this.sortBy] < b[this.sortBy]) return -modifier;
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
      return Promise.all([
        loadLicenses().then(l => this.licenses = l),
        loadProjects().then(p => this.projects = p),
        this.loadProjectLicenses(),
      ]);
    },
    loadProjectLicenses() {
      return loadProjectLicenses().then(pl => this.items = pl)
    },
    findProjectName(projectKey) {
      let projectItem = this.projects.find(p => p.key === projectKey);
      return projectItem ? projectItem.name : '-';
    },
    findLicenseName(license) {
      let licenseItem = this.licenses.find(l => l.id === license);
      return licenseItem ? licenseItem.name : '-';
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
    saveItems(items) {
      return saveProjectLicenses(items)
        .then(() => {
          this.loadProjectLicenses()
          this.itemToEdit = null;
          this.itemToDelete = null;
        });
    },
    saveItem(item) {
      if (this.editMode === 'add') {
        this.saveItems([...this.items, item]);
      } else {
        const itemToChange = this.items.find(i => i.projectKey === item.projectKey && i.license === item.license);
        itemToChange.allowed = item.allowed;
        this.saveItems(this.items);
      }
    },
    showDeleteDialog(item) {
      this.itemToDelete = item;
    },
    cancelDelete() {
      this.itemToDelete = null;
    },
    deleteItem(item) {
      this.saveItems(this.items.filter(i => (i.projectKey !== item.projectKey || i.license !== item.license)));
    },
    sort(param) {
      if (param === this.sortBy) {
        this.sortDirection = this.sortDirection === "asc" ? "desc" : "asc";
      }
      this.sortBy = param;
    }
  },
  directives: { focus }
}
</script>
<style>
  @import "../dashboard/icons.css";
</style>
