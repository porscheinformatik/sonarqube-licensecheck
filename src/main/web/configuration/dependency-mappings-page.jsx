import {
  Button,
  ButtonIcon,
  Checkbox,
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
import { loadDependencyMappings, loadLicenses, saveDependencyMappings } from "./sonar-api";

const DependencyMappingsPage = () => {
  const [items, setItems] = useState([]);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [itemToEdit, setItemToEdit] = useState(null);
  const [editMode, setEditMode] = useState(null);
  const [searchText, setSearchText] = useState("");
  const [licenses, setLicenses] = useState([]);
  const [sortBy, setSortBy] = useState("key");
  const [sortDirection, setSortDirection] = useState("asc");

  useEffect(() => {
    load();
  }, []);

  const load = () => {
    return Promise.all([
      loadLicenses().then((l) => setLicenses(l)),
      loadDependencyMappings().then((md) => setItems(md)),
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
    setItemToEdit({ ...item, old_key: item.key });
    setEditMode("edit");
  };

  const cancelEdit = () => {
    setItemToEdit(null);
  };

  const saveItems = (items) => {
    saveDependencyMappings(items).then(() => {
      loadDependencyMappings().then((md) => setItems(md));
      setItemToEdit(null);
      setItemToDelete(null);
    });
  };

  const saveItem = (item) => {
    if (editMode === "add") {
      saveItems([...items, item]);
    } else {
      const newItems = [...items];
      const itemToChange = newItems.find((i) => i.key === item.old_key);
      itemToChange.key = item.key;
      itemToChange.license = item.license;
      itemToChange.overwrite = item.overwrite;
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
    saveItems(items.filter((i) => i.key !== item.key));
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
          item.key.toLowerCase().includes(searchText.toLowerCase()) ||
          item.license.toLowerCase().includes(searchText.toLowerCase()),
      );

  return (
    <div>
      <header className="sw-mb-5">
        <h1 className="sw-mb-4">License Check - Dependency Mappings</h1>
        <div className="sw-mb-4">Maps a dependency name/key (with regex) to a license</div>
        <div className="page-actions">
          <Button onClick={showAddDialog}>Add Dependency Mapping</Button>
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
              <th onClick={() => sort("key")} scope="col">
                Key Regex
                {sortBy === "key" &&
                  (sortDirection === "asc" ? <IconTriangleUp /> : <IconTriangleDown />)}
              </th>
              <th onClick={() => sort("license")} scope="col">
                License
                {sortBy === "license" &&
                  (sortDirection === "asc" ? <IconTriangleUp /> : <IconTriangleDown />)}
              </th>
              <th onClick={() => sort("overwrite")} scope="col">
                Overwrite License
                {sortBy === "overwrite" &&
                  (sortDirection === "asc" ? <IconTriangleUp /> : <IconTriangleDown />)}
              </th>
              <th scope="col">Actions</th>
            </tr>
          </thead>
          <tbody>
            {displayedItems.map((item) => (
              <tr key={item.key}>
                <td className="sw-truncate">{item.key}</td>
                <td className="sw-truncate">
                  {item.license} / {findLicenseName(item.license)}
                </td>
                <td>{item.overwrite === "true" ? "Yes" : "No"}</td>
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
                <td colSpan="4">No Maven dependencies available</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <Modal
        title={editMode === "add" ? "Add Maven Dependency" : "Edit Maven Dependency"}
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
                <Label htmlFor="keyEdit">
                  Key Regex<em className="mandatory">*</em>
                </Label>
                <TextInput
                  required
                  autoFocus={true}
                  value={itemToEdit.key || ""}
                  onChange={(e) => setItemToEdit({ ...itemToEdit, key: e.target.value })}
                  id="keyEdit"
                  name="keyEdit"
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
              <div className="modal-field">
                <Label>Overwrite License</Label>
                <div>
                  <Checkbox
                    label="Overwrite"
                    name="overwrite"
                    checked={itemToEdit.overwrite === "true"}
                    onCheck={(checked) =>
                      setItemToEdit({
                        ...itemToEdit,
                        overwrite: checked ? "true" : "false",
                      })
                    }
                  />
                </div>
              </div>
            </div>
          )
        }
      />

      <Modal
        title="Delete Maven Dependency"
        isOpen={!!itemToDelete}
        onOpenChange={(isOpen) => {
          if (!isOpen) {
            cancelDelete();
          }
        }}
        description={`Are you sure you want to delete the Maven dependency mapping "${itemToDelete?.key}" / "${itemToDelete?.license}"?`}
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

export default DependencyMappingsPage;
