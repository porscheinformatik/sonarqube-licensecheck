<template>
  <div>
    <header class="page-header">
      <h1 class="page-title">License Check - Licenses</h1>
      <div class="page-description">Add and administer licenses, allow or disallow globally.</div>
      <div class="page-actions">
        <div class="button-group">
          <button id="license-add" @click="showAddLicenseDialog()">Add License</button>
        </div>
      </div>
    </header>
    <div id="licenses-search">
      <div class="panel panel-vertical bordered-bottom spacer-bottom">
        <button id="license-search-submit" class="search-box-submit button-clean"><i class="icon-search"></i></button>
        <input v-model="searchText" id="license-search-query" class="search-box-input" type="search" maxlength="100" placeholder="Search" autocomplete="off">
      </div>
    </div>
    <div>
      <table class="data zebra" width="100%">
        <thead>
          <tr>
            <th>Identifier</th>
            <th>Name</th>
            <th>Allowed</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="license in displayedLicenses" :key="license.identifier">
            <td class="thin">{{license.identifier}}</td>
            <td>{{license.name}}</td>
            <td class="thin">{{license.status}}</td>
            <td class="thin">
              <a class="icon-edit" @click="showEditLicenseDialog(license)" title="Edit License"></a>
              <a class="icon-delete" @click="showDeleteLicenseDialog(license)" title="Delete License"></a>
            </td>
          </tr>
          <tr v-show="!displayedLicenses">
            <td colspan="4">no licenses found</td>
          </tr>
        </tbody>
      </table>
    </div>
    <modal-dialog :header="editMode === 'add' ? 'Add License' : 'Edit License'" :show="!!licenseToEdit" @close="cancelEdit()">
      <div slot="body" v-if="licenseToEdit">
        <div class="modal-field">
          <label for="licenseIdentifierEdit">Identifier<em class="mandatory">*</em></label>
          <input required v-focus="editMode === 'add'" :disabled="editMode !== 'add'" v-model="licenseToEdit.identifier" id="licenseIdentifierEdit" name="licenseIdentifierEdit" type="text" size="50"
            maxlength="255">
        </div>
        <div class="modal-field">
          <label for="licenseNameEdit">Name<em class="mandatory">*</em></label>
          <input required v-focus="editMode !== 'add'" v-model="licenseToEdit.name" id="licenseNameEdit" name="licenseNameEdit" type="text" size="50" maxlength="255">
        </div>
        <div class="modal-field">
          <label for="licenseStatusEdit">Status<em class="mandatory">*</em></label>
          <select required v-model="licenseToEdit.status" id="licenseStatusEdit" name="licenseStatusEdit">
            <option value="true">true</option>
            <option value="false">false</option>
          </select>
        </div>
      </div>
      <span slot="footer"><button @click="saveLicense(licenseToEdit)">Save</button></span>
    </modal-dialog>
    <modal-dialog header="Delete License" :show="!!licenseToDelete" @close="cancelDelete()">
      <div slot="body" v-if="licenseToDelete">Are you sure you want to delete the license {{licenseToDelete.identifier}} {{licenseToDelete.name}}?</div>
      <span slot="footer"><button @click="deleteLicense(licenseToDelete)">Delete</button></span>
    </modal-dialog>
  </div>
</template>

<script>
import ModalDialog from './modal-dialog';

const focus = {
  inserted(el, binding) {
    if (binding.value)
      el.focus();
  },
};

export default {
  data() {
    return {
      licenses: [],
      licenseToDelete: null,
      licenseToEdit: null,
      editMode: null,
      searchText: null
    };
  },
  computed: {
    displayedLicenses() {
      if (!this.searchText || this.searchText.length == 0) {
        return this.licenses;
      }

      let search = this.searchText.toLowerCase();
      return this.licenses.filter(
        license =>
          license.name.toLowerCase().indexOf(search) >= 0 ||
          license.identifier.toLowerCase().indexOf(search) >= 0
      );
    }
  },
  created() {
    this.loadLicenses();
  },
  methods: {
    loadLicenses() {
      window.SonarRequest
        .getJSON("/api/licensecheck/licenses/show")
        .then(response => {
          this.licenses = response;
        });
    },
    showAddLicenseDialog() {
      this.licenseToEdit = {};
      this.editMode = 'add';
    },
    showEditLicenseDialog(license) {
      this.licenseToEdit = Object.assign({}, license);
      this.editMode = 'edit';
    },
    cancelEdit() {
      this.licenseToEdit = null;
    },
    saveLicense(license) {
      window.SonarRequest
        .post(`/api/licensecheck/licenses/${this.editMode}`, license)
        .then(() => {
          this.loadLicenses()
        });
      this.licenseToEdit = null;
    },
    showDeleteLicenseDialog(license) {
      this.licenseToDelete = license;
    },
    cancelDelete() {
      this.licenseToDelete = null;
    },
    deleteLicense(license) {
      window.SonarRequest
        .post('/api/licensecheck/licenses/delete', { identifier: license.identifier })
        .then(() => {
          this.loadLicenses()
        });
      this.licenseToDelete = null;
    }
  },
  components: { ModalDialog },
  directives: { focus }
};
</script>
