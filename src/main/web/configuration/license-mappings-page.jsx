import { useEffect, useState } from 'react';
import '../dashboard/icons.css';
import { DeleteIcon, MagnifyIcon, PencilIcon } from '../icons';
import ModalDialog from '../modal-dialog';
import { loadLicenseMappings, loadLicenses, saveLicenseMappings } from './sonar-api';

const LicenseMappingsPage = () => {
  const [items, setItems] = useState([]);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [itemToEdit, setItemToEdit] = useState(null);
  const [editMode, setEditMode] = useState(null);
  const [searchText, setSearchText] = useState('');
  const [licenses, setLicenses] = useState([]);
  const [sortBy, setSortBy] = useState('regex');
  const [sortDirection, setSortDirection] = useState('asc');

  useEffect(() => {
    load();
  }, []);

  const load = () => {
    return Promise.all([
      loadLicenses().then(l => setLicenses(l)),
      loadLicenseMappings().then(ml => setItems(ml))
    ]);
  };

  const findLicenseName = (license) => {
    const licenseItem = licenses.find(l => l.id === license);
    return licenseItem ? licenseItem.name : '-';
  };

  const showAddDialog = () => {
    setItemToEdit({});
    setEditMode('add');
  };

  const showEditDialog = (item) => {
    setItemToEdit({ ...item, old_regex: item.regex });
    setEditMode('edit');
  };

  const cancelEdit = () => {
    setItemToEdit(null);
  };

  const saveItems = (items) => {
    saveLicenseMappings(items).then(() => {
      loadLicenseMappings().then(ml => setItems(ml));
      setItemToEdit(null);
      setItemToDelete(null);
    });
  };

  const saveItem = (item) => {
    if (editMode === 'add') {
      saveItems([...items, item]);
    } else {
      const newItems = [...items];
      const itemToChange = newItems.find(i => i.regex === item.old_regex);
      itemToChange.regex = item.regex;
      itemToChange.license = item.license;
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
    saveItems(items.filter(i => i.regex !== item.regex));
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
      item.regex.toLowerCase().includes(searchText.toLowerCase()) ||
      item.license.toLowerCase().includes(searchText.toLowerCase())
    );

  return (
    <div className="boxed-group boxed-group-inner">
      <header className="page-header">
        <h1 className="page-title">License Check - License Mappings</h1>
        <div className="page-description">Maps a license name (with regex) to a license</div>
        <div className="page-actions">
          <button id="license-add" onClick={showAddDialog} className="button">
            Add License Mapping
          </button>
        </div>
      </header>

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
            License mapping - license name (with regex) to a license
          </caption>
          <thead>
            <tr>
              <th onClick={() => sort('regex')} scope="col">
                License Text Regex
                {sortBy === 'regex' && (
                  <div className={`arrow ${sortDirection === 'asc' ? 'arrow_up' : 'arrow_down'}`} />
                )}
              </th>
              <th onClick={() => sort('license')} scope="col">
                License
                {sortBy === 'license' && (
                  <div className={`arrow ${sortDirection === 'asc' ? 'arrow_up' : 'arrow_down'}`} />
                )}
              </th>
              <th scope="col">Actions</th>
            </tr>
          </thead>
          <tbody>
            {displayedItems.map(item => (
              <tr key={item.regex}>
                <td>{item.regex}</td>
                <td>{item.license} / {findLicenseName(item.license)}</td>
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
            {!displayedItems.length && (
              <tr>
                <td colSpan="3">No Maven licenses available</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <ModalDialog
        header={editMode === 'add' ? 'Add Maven License' : 'Edit Maven License'}
        show={!!itemToEdit}
        onClose={cancelEdit}
      >
        {itemToEdit && (
          <>
            <div className="modal-field">
              <label htmlFor="regexEdit">
                License Text Regex<em className="mandatory">*</em>
              </label>
              <input
                required
                autoFocus
                value={itemToEdit.regex || ''}
                onChange={(e) => setItemToEdit({ ...itemToEdit, regex: e.target.value })}
                id="regexEdit"
                name="regexEdit"
                type="text"
                size="50"
                maxLength="255"
              />
            </div>
            <div className="modal-field">
              <label htmlFor="licenseSelect">
                License<em className="mandatory">*</em>
              </label>
              <select
                required
                value={itemToEdit.license || ''}
                onChange={(e) => setItemToEdit({ ...itemToEdit, license: e.target.value })}
                id="licenseSelect"
                name="licenseSelect"
              >
                {licenses.map(license => (
                  <option key={license.id} value={license.id}>
                    {license.id} / {license.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="modal-foot">
              <button className="button" onClick={() => saveItem(itemToEdit)}>Save</button>
            </div>
          </>
        )}
      </ModalDialog>

      <ModalDialog
        header="Delete Maven License"
        show={!!itemToDelete}
        onClose={cancelDelete}
      >
        {itemToDelete && (
          <>
            <div>
              Are you sure you want to delete the Maven license mapping "{itemToDelete.regex}" / "{itemToDelete.license}"?
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

export default LicenseMappingsPage;