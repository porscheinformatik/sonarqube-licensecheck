<template>
  <div class="boxed-group boxed-group-inner">
    <header class="page-header">
      <h1 class="page-title">License Check - Licenses</h1>
      <div class="page-description">Add and administer licenses, allow or disallow globally.</div>
      <div class="page-actions">
        <button id="item-add" @click="showAddDialog()" class="button">Add License</button>
      </div>
    </header>
    <div class="panel panel-vertical bordered-bottom spacer-bottom">
      <div class="search-box">
        <svgicon icon="magnify" width="15" height="16"
                 style="padding-left: 5px; margin-top: 4px; fill: #999;"></svgicon>
        <input style="background: none; width: 100%; border: none" v-model="searchText" class="search-box-input"
               type="search" maxlength="100" placeholder="Search" autocomplete="off">
      </div>
    </div>
    <div>
      <table class="data zebra">
        <caption>Add and administer licenses, allow or disallow globally.</caption>
        <thead>
        <tr>
          <th @click="sort('id')" scope="col">Identifier
            <div class="arrow" v-if="sortBy === 'identifier'"
                 v-bind:class="{ 'arrow_up' : sortDirection === 'asc', 'arrow_down' : sortDirection === 'desc'}"></div>
          </th>
          <th @click="sort('name')" scope="col">Name
            <div class="arrow" v-if="sortBy === 'name'"
                 v-bind:class="{ 'arrow_up' : sortDirection === 'asc', 'arrow_down' : sortDirection === 'desc'}"></div>
          </th>
          <th @click="sort('allowed')" scope="col">Status
            <div class="arrow" v-if="sortBy === 'allowed'"
                 v-bind:class="{ 'arrow_up' : sortDirection === 'asc', 'arrow_down' : sortDirection === 'desc'}"></div>
          </th>
          <th scope="col">Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in displayedItems" :key="item.id">
          <td class="thin">{{ item.id }}</td>
          <td>{{ item.name }}</td>
          <td>
            <span
              :class="{ 'icon-license-ok': item.allowed === 'true', 'icon-license-nok': item.allowed !== 'true' }"></span>
            {{ item.allowed === 'true' ? 'Allowed' : 'Forbidden' }}
          </td>
          <td class="thin nowrap">
            <a class="button" @click="showEditDialog(item)" title="Edit item">
              <svgicon icon="pencil" width="16" height="16" style="fill: currentcolor"></svgicon>
            </a>
            <a class="button" v-if="items.length > 1" @click="showDeleteDialog(item)" title="Delete item">
              <svgicon icon="delete" width="16" height="16" style="fill: rgb(212, 51, 63)"></svgicon>
            </a>
          </td>
        </tr>
        <tr v-show="!displayedItems">
          <td colspan="4">no items found</td>
        </tr>
        </tbody>
      </table>
    </div>
    <modal-dialog :header="editMode === 'add' ? 'Add License' : 'Edit License'" :show="!!itemToEdit"
                  @close="cancelEdit()">
      <div slot="body" v-if="itemToEdit">
        <div class="modal-field">
          <label for="itemIdEdit">Identifier<em class="mandatory">*</em></label>
          <input required v-focus="editMode === 'add'" :disabled="editMode !== 'add'" v-model="itemToEdit.id"
                 id="itemIdEdit" name="itemIdEdit" type="text" size="50"
                 maxlength="255">
        </div>
        <div class="modal-field">
          <label for="itemNameEdit">Name<em class="mandatory">*</em></label>
          <input required v-focus="editMode !== 'add'" v-model="itemToEdit.name" id="itemNameEdit" name="itemNameEdit"
                 type="text" size="50" maxlength="255">
        </div>
        <div class="modal-field">
          <label>Status<em class="mandatory">*</em></label>
          <label for="itemAllowedEdit">
            <input type="checkbox" id="itemAllowedEdit" name="itemAllowedEdit" v-model="itemToEdit.allowed"
                   true-value="true" false-value="false">
            Allowed
          </label>
        </div>
      </div>
      <span slot="footer"><button class="button" @click="saveItem(itemToEdit)">Save</button></span>
    </modal-dialog>
    <modal-dialog header="Delete license" :show="!!itemToDelete" @close="cancelDelete()">
      <div slot="body" v-if="itemToDelete">Are you sure you want to delete the license
        &quot;{{ itemToDelete.id }}&quot; / &quot;{{ itemToDelete.name }}&quot;?
      </div>
      <span slot="footer"><button class="button" @click="deleteItem(itemToDelete)">Delete</button></span>
    </modal-dialog>
  </div>
</template>

<script>
import "../../../compiled-icons";
import {loadLicenses, saveLicenses} from "./sonar-api";

const focus = {
  inserted(el, binding) {
    if (binding.value)
      el.focus();
  },
};

export default {
  data() {
    return {
      items: [],
      itemToDelete: null,
      itemToEdit: null,
      editMode: null,
      searchText: null,
      sortBy: "id",
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
          item.name.toLowerCase().indexOf(search) >= 0 ||
          item.id.toLowerCase().indexOf(search) >= 0
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
      return loadLicenses().then(l => this.items = l);
    },
    showAddDialog() {
      this.itemToEdit = {
        allowed: false
      };
      this.editMode = 'add';
    },
    showEditDialog(item) {
      this.itemToEdit = Object.assign({}, item);
      this.editMode = 'edit';
    },
    cancelEdit() {
      this.itemToEdit = null;
    },
    saveItems(items) {
      saveLicenses(items)
        .then(() => {
          this.load()
          this.itemToEdit = null;
          this.itemToDelete = null;
        });
    },
    saveItem(item) {
      if (this.editMode === 'add') {
        this.saveItems([...this.items, item]);
      } else {
        const itemToChange = this.items.find(i => i.id === item.id);
        itemToChange.name = item.name;
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
      this.saveItems(this.items.filter(i => i.id !== item.id));
    },
    sort(param) {
      if (param === this.sortBy) {
        this.sortDirection = this.sortDirection === "asc" ? "desc" : "asc";
      }
      this.sortBy = param;
    }
  },
  directives: {focus}
};
</script>
<style>
@import "../dashboard/icons.css";
</style>
