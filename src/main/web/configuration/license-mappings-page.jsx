import {
  Button,
  ButtonIcon,
  IconDelete,
  IconEdit,
  IconSearch,
  IconTriangleDown,
  IconTriangleUp,
  Label,
  Modal,
  TextInput,
} from "@sonarsource/echoes-react";
import { useEffect, useState } from "react";
import { loadLicenseMappings, loadLicenses, saveLicenseMappings } from "./sonar-api";

const LicenseMappingsPage = () => {
  const [items, setItems] = useState([]);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [itemToEdit, setItemToEdit] = useState(null);
  const [editMode, setEditMode] = useState(null);
  const [searchText, setSearchText] = useState("");
  const [licenses, setLicenses] = useState([]);
  const [sortBy, setSortBy] = useState("regex");
  const [sortDirection, setSortDirection] = useState("asc");

  useEffect(() => {
    load();
  }, []);

  const load = () => {
    return Promise.all([
      loadLicenses().then((l) => setLicenses(l)),
      loadLicenseMappings().then((ml) => setItems(ml)),
    ]);
  };

  const findLicenseName = (license) => {
    const licenseItem = licenses.find((l) => l.id === license);
    return licenseItem ? licenseItem.name : "-";
  };

  const showAddDialog = () => {
    setItemToEdit({});
    setEditMode("add");
  };

  const showEditDialog = (item) => {
    setItemToEdit({ ...item, old_regex: item.regex });
    setEditMode("edit");
  };

  const cancelEdit = () => {
    setItemToEdit(null);
  };

  const saveItems = (items) => {
    saveLicenseMappings(items).then(() => {
      loadLicenseMappings().then((ml) => setItems(ml));
      setItemToEdit(null);
      setItemToDelete(null);
    });
  };

  const saveItem = (item) => {
    if (editMode === "add") {
      saveItems([...items, item]);
    } else {
      const newItems = [...items];
      const itemToChange = newItems.find((i) => i.regex === item.old_regex);
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
    saveItems(items.filter((i) => i.regex !== item.regex));
  };

  const sort = (param) => {
    if (param === sortBy) {
      setSortDirection(sortDirection === "asc" ? "desc" : "asc");
    }
    setSortBy(param);
  };

  const sortedItems = [...items].sort((a, b) => {
    const modifier = sortDirection === "desc" ? -1 : 1;
    if (a[sortBy] < b[sortBy]) return -modifier;
    if (a[sortBy] > b[sortBy]) return modifier;
    return 0;
  });

  const displayedItems = !searchText
    ? sortedItems
    : sortedItems.filter(
        (item) =>
          item.regex.toLowerCase().includes(searchText.toLowerCase()) ||
          item.license.toLowerCase().includes(searchText.toLowerCase()),
      );

  return (
    <div>
      <header className="sw-mb-5">
        <h1 className="sw-mb-4">License Check - License Mappings</h1>
        <div className="sw-mb-4">Maps a license name (with regex) to a license</div>
        <div className="page-actions">
          <Button onClick={showAddDialog}>Add License Mapping</Button>
        </div>
      </header>

      <div className="sw-mb-4">
        <TextInput
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          prefix={<IconSearch key="IconSearch" />}
        />
      </div>

      <div>
        <table className="sqlc-data">
          <thead>
            <tr>
              <th onClick={() => sort("regex")} scope="col">
                License Text Regex
                {sortBy === "regex" &&
                  (sortDirection === "asc" ? <IconTriangleUp /> : <IconTriangleDown />)}
              </th>
              <th onClick={() => sort("license")} scope="col">
                License
                {sortBy === "license" &&
                  (sortDirection === "asc" ? <IconTriangleUp /> : <IconTriangleDown />)}
              </th>
              <th scope="col">Actions</th>
            </tr>
          </thead>
          <tbody>
            {displayedItems.map((item) => (
              <tr key={item.regex}>
                <td className="sw-truncate">{item.regex}</td>
                <td className="sw-truncate">
                  {item.license} / {findLicenseName(item.license)}
                </td>
                <td className="sw-whitespace-nowrap">
                  <ButtonIcon
                    variety="default-ghost"
                    ariaLabel="Edit"
                    Icon={IconEdit}
                    onClick={() => showEditDialog(item)}
                  />
                  {items.length > 1 && (
                    <ButtonIcon
                      variety="default-ghost"
                      ariaLabel="Delete"
                      Icon={IconDelete}
                      onClick={() => showDeleteDialog(item)}
                    />
                  )}
                </td>
              </tr>
            ))}
            {!displayedItems.length && (
              <tr>
                <td colSpan="3">No license mappings available</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <Modal
        title={editMode === "add" ? "Add License Mapping" : "Edit License Mapping"}
        isOpen={!!itemToEdit}
        onOpenChange={(isOpen) => {
          if (!isOpen) {
            cancelEdit();
          }
        }}
        primaryButton={<Button onClick={() => saveItem(itemToEdit)}>Save</Button>}
        secondaryButton={
          <Button variety="default-ghost" onClick={cancelEdit}>
            Cancel
          </Button>
        }
        content={
          itemToEdit && (
            <div>
              <div className="modal-field">
                <Label htmlFor="regexEdit">
                  License Text Regex<em className="mandatory">*</em>
                </Label>
                <TextInput
                  required
                  autoFocus
                  value={itemToEdit.regex || ""}
                  onChange={(e) => setItemToEdit({ ...itemToEdit, regex: e.target.value })}
                  id="regexEdit"
                  name="regexEdit"
                  maxLength="255"
                />
              </div>
              <div className="modal-field">
                <Label htmlFor="licenseSelect">
                  License<em className="mandatory">*</em>
                </Label>
                <select
                  required
                  value={itemToEdit.license || ""}
                  onChange={(e) => setItemToEdit({ ...itemToEdit, license: e.target.value })}
                  id="licenseSelect"
                  name="licenseSelect"
                  className="sw-w-full sw-px-3 sw-py-2 sw-border sw-border-neutral-200 sw-rounded"
                >
                  <option value="">Select a license</option>
                  {licenses.map((license) => (
                    <option key={license.id} value={license.id}>
                      {license.id} / {license.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          )
        }
      />

      <Modal
        title="Delete License Mapping"
        isOpen={!!itemToDelete}
        onOpenChange={(isOpen) => {
          if (!isOpen) {
            cancelDelete();
          }
        }}
        description={`Are you sure you want to delete the license mapping "${itemToDelete?.regex}" / "${itemToDelete?.license}"?`}
        primaryButton={<Button onClick={() => deleteItem(itemToDelete)}>Delete</Button>}
        secondaryButton={
          <Button variety="default-ghost" onClick={cancelDelete}>
            Cancel
          </Button>
        }
      />
    </div>
  );
};

export default LicenseMappingsPage;
