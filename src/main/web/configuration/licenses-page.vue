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
        <svgicon icon="magnify" width="15" height="16" style="padding-left: 5px; margin-top: 4px; fill: #999;"></svgicon>
        <input style="background: none; width: 100%; border: none" v-model="searchText" class="search-box-input" type="search" maxlength="100" placeholder="Search" autocomplete="off">
      </div>
    </div>
    <div>
      <table class="data zebra">
        <caption>Add and administer licenses, allow or disallow globally.</caption>
        <thead>
          <tr>
            <th @click="sort('identifier')" scope="col">Identifier<div class="arrow" v-if="sortBy === 'identifier'" v-bind:class="{ 'arrow_up' : sortDirection === 'asc', 'arrow_down' : sortDirection === 'desc'}"></div></th>
            <th @click="sort('name')" scope="col">Name<div class="arrow" v-if="sortBy === 'name'" v-bind:class="{ 'arrow_up' : sortDirection === 'asc', 'arrow_down' : sortDirection === 'desc'}"></div></th>
            <th @click="sort('status')" scope="col">Status<div class="arrow" v-if="sortBy === 'status'" v-bind:class="{ 'arrow_up' : sortDirection === 'asc', 'arrow_down' : sortDirection === 'desc'}"></div></th>
            <th scope="col">Actions</th>
           </tr>
        </thead>
        <tbody>
          <tr v-for="item in displayedItems" :key="item.identifier">
            <td class="thin">{{item.identifier}}</td>
            <td>{{item.name}}</td>
            <td>
              <span :class="{ 'icon-license-ok': item.status === 'true', 'icon-license-nok': item.status === 'false' }"></span>
              {{item.status === 'true' ? 'Allowed': 'Forbidden'}}
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
          <tr v-show="!displayedItems">
            <td colspan="4">no items found</td>
          </tr>
        </tbody>
      </table>
    </div>
    <modal-dialog :header="editMode === 'add' ? 'Add License' : 'Edit License'" :show="!!itemToEdit" @close="cancelEdit()">
      <div slot="body" v-if="itemToEdit">
        <div class="modal-field">
          <label for="itemIdentifierEdit">Identifier<em class="mandatory">*</em></label>
          <input required v-focus="editMode === 'add'" :disabled="editMode !== 'add'" v-model="itemToEdit.identifier" id="itemIdentifierEdit" name="itemIdentifierEdit" type="text" size="50"
            maxlength="255">
        </div>
        <div class="modal-field">
          <label for="itemNameEdit">Name<em class="mandatory">*</em></label>
          <input required v-focus="editMode !== 'add'" v-model="itemToEdit.name" id="itemNameEdit" name="itemNameEdit" type="text" size="50" maxlength="255">
        </div>
        <div class="modal-field">
          <label>Status<em class="mandatory">*</em></label>
          <label for="itemStatusEdit">
            <input type="checkbox" id="itemStatusEdit" name="itemStatusEdit" v-model="itemToEdit.status" true-value="true" false-value="false">
            Allowed
          </label>
        </div>
      </div>
      <span slot="footer"><button class="button" @click="saveItem(itemToEdit)">Save</button></span>
    </modal-dialog>
    <modal-dialog header="Delete license" :show="!!itemToDelete" @close="cancelDelete()">
      <div slot="body" v-if="itemToDelete">Are you sure you want to delete the license &quot;{{itemToDelete.identifier}}&quot; / &quot;{{itemToDelete.name}}&quot;?</div>
      <span slot="footer"><button class="button" @click="deleteItem(itemToDelete)">Delete</button></span>
    </modal-dialog>
  </div>
</template>

<script>
import "../../../compiled-icons";

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
      sortBy: "identifier",
      sortDirection: "asc"
};
  },
  computed: {
    displayedItems() {
      if (!this.searchText || this.searchText.length == 0) {
        return this.sortedItems;
      }

      let search = this.searchText.toLowerCase();
      return this.sortedItems.filter(
        item =>
          item.name.toLowerCase().indexOf(search) >= 0 ||
          item.identifier.toLowerCase().indexOf(search) >= 0
      );
	},
    sortedItems() {
      return this.items.sort((a, b) => {
        let modifier = 1;
        if (this.sortDirection === "desc") modifier = -1;
        if (a[this.sortBy] < b[this.sortBy]) return -1 * modifier;
        if (a[this.sortBy] > b[this.sortBy]) return 1 * modifier;
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
          this.items = response;
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
        .post(`/api/licensecheck/licenses/${this.editMode}`, item)
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
        .post('/api/licensecheck/licenses/delete', { identifier: item.identifier })
        .then(() => {
          this.load()
      });
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
