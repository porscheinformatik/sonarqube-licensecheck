<template>
  <div class="boxed-group boxed-group-inner">
    <header class="page-header">
      <h1 class="page-title">License Check - Dependency Mappings</h1>
      <div class="page-description">Maps a dependency name/key (with regex) to a license</div>
      <div class="page-actions">
        <button class="button" id="license-add" @click="showAddDialog()">
          Add Dependency Mapping
        </button>
      </div>
    </header>
    <div class="panel panel-vertical bordered-bottom spacer-bottom">
      <div class="search-box">
        <svgicon
          icon="magnify"
          width="15"
          height="16"
          style="padding-left: 5px; margin-top: 4px; fill: #999"
        ></svgicon>
        <input
          style="background: none; width: 100%; border: none"
          v-model="searchText"
          class="search-box-input"
          type="search"
          maxlength="100"
          placeholder="Search"
          autocomplete="off"
        />
      </div>
    </div>
    <div>
      <table class="data zebra">
        <caption>
          Dependency mapping - name/key (with regex) to a license
        </caption>
        <thead>
          <tr>
            <th @click="sort('key')" scope="col">
              Key Regex
              <div
                class="arrow"
                v-if="sortBy === 'key'"
                v-bind:class="{
                  arrow_up: sortDirection === 'asc',
                  arrow_down: sortDirection === 'desc',
                }"
              ></div>
            </th>
            <th @click="sort('license')" scope="col">
              License
              <div
                class="arrow"
                v-if="sortBy === 'license'"
                v-bind:class="{
                  arrow_up: sortDirection === 'asc',
                  arrow_down: sortDirection === 'desc',
                }"
              ></div>
            </th>
            <th @click="sort('overwrite')" scope="col">
              Overwrite License
              <div
                class="arrow"
                v-if="sortBy === 'overwrite'"
                v-bind:class="{
                  arrow_up: sortDirection === 'asc',
                  arrow_down: sortDirection === 'desc',
                }"
              ></div>
            </th>
            <th scope="col">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in displayedItems" :key="item.key">
            <td>{{ item.key }}</td>
            <td>{{ item.license }} / {{ findLicenseName(item.license) }}</td>
            <td>
              {{ item.overwrite === "false" ? "No" : "Yes" }}
            </td>
            <td class="thin nowrap">
              <a class="button" @click="showEditDialog(item)" title="Edit item">
                <svgicon icon="pencil" width="16" height="16" style="fill: currentcolor"></svgicon>
              </a>
              <a
                class="button"
                v-if="items.length > 1"
                @click="showDeleteDialog(item)"
                title="Delete item"
              >
                <svgicon
                  icon="delete"
                  width="16"
                  height="16"
                  style="fill: rgb(212, 51, 63)"
                ></svgicon>
              </a>
            </td>
          </tr>
          <tr v-show="!displayedItems.length">
            <td colspan="3">No Maven dependencies available</td>
          </tr>
        </tbody>
      </table>
    </div>
    <modal-dialog
      :header="editMode === 'add' ? 'Add Maven Dependency' : 'Edit Maven Dependency'"
      :show="!!itemToEdit"
      @close="cancelEdit()"
    >
      <div slot="body" v-if="itemToEdit">
        <div class="modal-field">
          <label for="keyEdit">Key Regex<em class="mandatory">*</em></label>
          <input
            required
            v-focus
            v-model="itemToEdit.key"
            id="keyEdit"
            name="keyEdit"
            type="text"
            size="50"
            maxlength="255"
          />
        </div>
        <div class="modal-field">
          <label for="licenseSelect">License<em class="mandatory">*</em></label>
          <select required v-model="itemToEdit.license" id="licenseSelect" name="licenseSelect">
            <option v-for="license in licenses" v-bind:value="license.id" v-bind:key="license.id">
              {{ license.id }} / {{ license.name }}
            </option>
          </select>
        </div>
        <div class="modal-field">
          <label>Overwrite License</label>
          <label for="overwriteCheckbox">
            <input
              type="checkbox"
              id="overwriteCheckbox"
              name="overwrite"
              v-model="itemToEdit.overwrite"
              true-value="true"
              false-value="false"
            />
            Overwrite
          </label>
        </div>
      </div>
      <span slot="footer"><button class="button" @click="saveItem(itemToEdit)">Save</button></span>
    </modal-dialog>
    <modal-dialog header="Delete Maven Dependency" :show="!!itemToDelete" @close="cancelDelete()">
      <div slot="body" v-if="itemToDelete">
        Are you sure you want to delete the Maven dependency mapping &quot;{{
          itemToDelete.key
        }}&quot; / &quot;{{ itemToDelete.license }}&quot;?
      </div>
      <span slot="footer"
        ><button class="button" @click="deleteItem(itemToDelete)">Delete</button></span
      >
    </modal-dialog>
  </div>
</template>

<script>
import "../../../compiled-icons";
import { loadLicenses, loadDependencyMappings, saveDependencyMappings } from "./sonar-api";

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
      sortDirection: "asc",
    };
  },
  computed: {
    displayedItems() {
      if (!this.searchText || this.searchText.length === 0) {
        return this.sortedItems;
      }

      let search = this.searchText.toLowerCase();
      return this.sortedItems.filter(
        (item) =>
          item.key.toLowerCase().indexOf(search) >= 0 ||
          item.license.toLowerCase().indexOf(search) >= 0,
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
    },
  },
  created() {
    this.load();
  },
  methods: {
    load() {
      return Promise.all([
        loadLicenses().then((l) => (this.licenses = l)),
        this.loadDependencyMappings(),
      ]);
    },
    loadDependencyMappings() {
      return loadDependencyMappings().then((md) => (this.items = md));
    },
    findLicenseName(license) {
      let licenseItem = this.licenses.find((l) => l.id === license);
      return licenseItem ? licenseItem.name : "-";
    },
    showAddDialog() {
      this.itemToEdit = {};
      this.editMode = "add";
    },
    showEditDialog(item) {
      this.itemToEdit = Object.assign({ old_key: item.key }, item);
      this.editMode = "edit";
    },
    cancelEdit() {
      this.itemToEdit = null;
    },
    saveItems(items) {
      saveDependencyMappings(items).then(() => {
        this.loadDependencyMappings();
        this.itemToEdit = null;
        this.itemToDelete = null;
      });
    },
    saveItem(item) {
      if (this.editMode === "add") {
        this.saveItems([...this.items, item]);
      } else {
        const itemToChange = this.items.find((i) => i.key === item.old_key);
        itemToChange.key = item.key;
        itemToChange.license = item.license;
        itemToChange.overwrite = item.overwrite;
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
      this.saveItems(this.items.filter((i) => i.key !== item.key));
    },
    sort(param) {
      if (param === this.sortBy) {
        this.sortDirection = this.sortDirection === "asc" ? "desc" : "asc";
      }
      this.sortBy = param;
    },
  },
  directives: { focus },
};
</script>
<style>
@import "../dashboard/icons.css";
</style>
