import { useEffect, useState } from 'react';
import '../dashboard/icons.css';
import { DeleteIcon, MagnifyIcon, PencilIcon } from '../icons';
import ModalDialog from '../modal-dialog';
import { loadLicenses, saveLicenses } from './sonar-api';

const LicensesPage = () => {
  const [items, setItems] = useState([]);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [itemToEdit, setItemToEdit] = useState(null);
  const [editMode, setEditMode] = useState(null);
  const [searchText, setSearchText] = useState('');
  const [sortBy, setSortBy] = useState('id');
  const [sortDirection, setSortDirection] = useState('asc');

  useEffect(() => {
    load();
  }, []);

  const load = () => {
    return loadLicenses().then(l => setItems(l));
  };

  const showAddDialog = () => {
    setItemToEdit({ allowed: false });
    setEditMode('add');
  };

  const showEditDialog = (item) => {
    setItemToEdit({ ...item });
    setEditMode('edit');
  };

  const cancelEdit = () => {
    setItemToEdit(null);
  };

  const importSpdx = () => {
    fetch('https://raw.githubusercontent.com/spdx/license-list-data/main/json/licenses.json')
      .then(r => r.json())
      .then(data => {
        const licenses = data.licenses.map(l => ({
          id: l.licenseId,
          name: l.name,
          allowed: false
        }));
        saveItems(licenses);
      });
  };

  const saveItems = (items) => {
    saveLicenses(items).then(() => {
      load();
      setItemToEdit(null);
      setItemToDelete(null);
    });
  };

  const saveItem = (item) => {
    if (editMode === 'add') {
      saveItems([...items, item]);
    } else {
      const newItems = [...items];
      const itemToChange = newItems.find(i => i.id === item.id);
      itemToChange.name = item.name;
      itemToChange.allowed = item.allowed;
      saveItems(newItems);
    }
  };

  const showDeleteDialog = (item) => {
    setItemToDelete(item);
  };

  const cancelDelete = () => {
    setItemToDelete(null);
  };

  const deleteItem = (item) => {
    saveItems(items.filter(i => i.id !== item.id));
  };

  const sort = (param) => {
    if (param === sortBy) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    }
    setSortBy(param);
  };

  const sortedItems = [...items].sort((a, b) => {
    const modifier = sortDirection === 'desc' ? -1 : 1;
    if (a[sortBy] < b[sortBy]) return -modifier;
    if (a[sortBy] > b[sortBy]) return modifier;
    return 0;
  });

  const displayedItems = !searchText ? sortedItems :
    sortedItems.filter(item => 
      item.name.toLowerCase().includes(searchText.toLowerCase()) ||
      item.id.toLowerCase().includes(searchText.toLowerCase())
    );

  return (
    <div className="boxed-group boxed-group-inner">
      <header className="page-header">
        <h1 className="page-title">License Check - Licenses</h1>
        <div className="page-description">Add and administer licenses, allow or disallow globally.</div>
        <div className="page-actions">
          <button id="item-add" onClick={showAddDialog} className="button">Add License</button>
        </div>
      </header>

      {items.length === 0 ? (
        <div className="panel">
          <p>
            Currently, you have no licenses defined. You can add all licenses from
            <a href="https://github.com/spdx/license-list-data">SPDX</a>.
          </p>
          <button className="button" onClick={importSpdx}>Add SPDX list</button>
        </div>
      ) : (
        <>
          <div className="panel panel-vertical bordered-bottom spacer-bottom">
            <div className="search-box">
              <MagnifyIcon width={15} height={16} style={{ paddingLeft: 5, marginTop: 4, fill: '#999' }} />
              <input
                style={{ background: 'none', width: '100%', border: 'none' }}
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
                className="search-box-input"
                type="search"
                maxLength="100"
                placeholder="Search"
                autoComplete="off"
              />
            </div>
          </div>

          <div>
            <table className="data zebra">
              <caption>
                Add and administer licenses, allow or disallow globally.
              </caption>
              <thead>
                <tr>
                  <th onClick={() => sort('id')} scope="col">
                    Identifier
                    {sortBy === 'id' && (
                      <div className={`arrow ${sortDirection === 'asc' ? 'arrow_up' : 'arrow_down'}`} />
                    )}
                  </th>
                  <th onClick={() => sort('name')} scope="col">
                    Name
                    {sortBy === 'name' && (
                      <div className={`arrow ${sortDirection === 'asc' ? 'arrow_up' : 'arrow_down'}`} />
                    )}
                  </th>
                  <th onClick={() => sort('allowed')} scope="col">
                    Status
                    {sortBy === 'allowed' && (
                      <div className={`arrow ${sortDirection === 'asc' ? 'arrow_up' : 'arrow_down'}`} />
                    )}
                  </th>
                  <th scope="col">Actions</th>
                </tr>
              </thead>
              <tbody>
                {displayedItems.map(item => (
                  <tr key={item.id}>
                    <td className="thin">{item.id}</td>
                    <td>{item.name}</td>
                    <td>
                      <span className={item.allowed === 'true' ? 'icon-license-ok' : 'icon-license-nok'} />
                      {item.allowed === 'true' ? 'Allowed' : 'Forbidden'}
                    </td>
                    <td className="thin nowrap">
                      <a className="button" onClick={() => showEditDialog(item)} title="Edit item">
                        <PencilIcon style={{ fill: 'currentcolor' }} />
                      </a>
                      {items.length > 1 && (
                        <a className="button" onClick={() => showDeleteDialog(item)} title="Delete item">
                          <DeleteIcon style={{ fill: 'rgb(212, 51, 63)' }} />
                        </a>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      <ModalDialog
        header={editMode === 'add' ? 'Add License' : 'Edit License'}
        show={!!itemToEdit}
        onClose={cancelEdit}
      >
        {itemToEdit && (
          <>
            <div className="modal-field">
              <label htmlFor="itemIdEdit">
                Identifier<em className="mandatory">*</em>
              </label>
              <input
                required
                autoFocus={editMode === 'add'}
                disabled={editMode !== 'add'}
                value={itemToEdit.id || ''}
                onChange={(e) => setItemToEdit({ ...itemToEdit, id: e.target.value })}
                id="itemIdEdit"
                name="itemIdEdit"
                type="text"
                size="50"
                maxLength="255"
              />
            </div>
            <div className="modal-field">
              <label htmlFor="itemNameEdit">
                Name<em className="mandatory">*</em>
              </label>
              <input
                required
                autoFocus={editMode !== 'add'}
                value={itemToEdit.name || ''}
                onChange={(e) => setItemToEdit({ ...itemToEdit, name: e.target.value })}
                id="itemNameEdit"
                name="itemNameEdit"
                type="text"
                size="50"
                maxLength="255"
              />
            </div>
            <div className="modal-field">
              <label>Status<em className="mandatory">*</em></label>
              <label htmlFor="itemAllowedEdit">
                <input
                  type="checkbox"
                  id="itemAllowedEdit"
                  name="itemAllowedEdit"
                  checked={itemToEdit.allowed === 'true'}
                  onChange={(e) => setItemToEdit({
                    ...itemToEdit,
                    allowed: e.target.checked ? 'true' : 'false'
                  })}
                />
                Allowed
              </label>
            </div>
            <div className="modal-foot">
              <button className="button" onClick={() => saveItem(itemToEdit)}>Save</button>
            </div>
          </>
        )}
      </ModalDialog>

      <ModalDialog
        header="Delete license"
        show={!!itemToDelete}
        onClose={cancelDelete}
      >
        {itemToDelete && (
          <>
            <div>
              Are you sure you want to delete the license "{itemToDelete.id}" / "{itemToDelete.name}"?
            </div>
            <div className="modal-foot">
              <button className="button" onClick={() => deleteItem(itemToDelete)}>Delete</button>
            </div>
          </>
        )}
      </ModalDialog>
    </div>
  );
};

export default LicensesPage;