import {
  Button,
  ButtonIcon,
  Checkbox,
  IconCheckCircle,
  IconDelete,
  IconEdit,
  IconError,
  IconSearch,
  Label,
  Modal,
  TextInput,
} from "@sonarsource/echoes-react";
import { useEffect, useState } from "react";
import "../dashboard/icons.css";
import "./configuration.css";
import { loadLicenses, saveLicenses } from "./sonar-api";

const LicensesPage = () => {
  const [items, setItems] = useState([]);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [itemToEdit, setItemToEdit] = useState(null);
  const [editMode, setEditMode] = useState(null);
  const [searchText, setSearchText] = useState("");
  const [sortBy, setSortBy] = useState("id");
  const [sortDirection, setSortDirection] = useState("asc");

  useEffect(() => {
    load();
  }, []);

  const load = () => {
    return loadLicenses().then((l) => setItems(l));
  };

  const showAddDialog = () => {
    setItemToEdit({ allowed: false });
    setEditMode("add");
  };

  const showEditDialog = (item) => {
    setItemToEdit({ ...item });
    setEditMode("edit");
  };

  const cancelEdit = () => {
    setItemToEdit(null);
  };

  const importSpdx = () => {
    fetch("https://raw.githubusercontent.com/spdx/license-list-data/main/json/licenses.json")
      .then((r) => r.json())
      .then((data) => {
        const licenses = data.licenses.map((l) => ({
          id: l.licenseId,
          name: l.name,
          allowed: false,
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
    if (editMode === "add") {
      saveItems([...items, item]);
    } else {
      const newItems = [...items];
      const itemToChange = newItems.find((i) => i.id === item.id);
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
    saveItems(items.filter((i) => i.id !== item.id));
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
          item.name.toLowerCase().includes(searchText.toLowerCase()) ||
          item.id.toLowerCase().includes(searchText.toLowerCase()),
      );

  return (
    <div>
      <header className="sw-mb-5">
        <h1 className="sw-mb-4">License Check - Licenses</h1>
        <div className="sw-mb-4">Add and administer licenses, allow or disallow globally.</div>
        <div className="page-actions">
          <Button onClick={showAddDialog}>Add License</Button>
        </div>
      </header>

      {items.length === 0 ? (
        <div className="panel">
          <p>
            Currently, you have no licenses defined. You can add all licenses from
            <a href="https://github.com/spdx/license-list-data">SPDX</a>.
          </p>
          <Button className="button" onClick={importSpdx}>
            Add SPDX list
          </Button>
        </div>
      ) : (
        <>
          <div className="sw-mb-4">
            <TextInput
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              prefix={<IconSearch key="IconSearch" />}
            ></TextInput>
          </div>

          <div>
            <table className="data zebra">
              <thead>
                <tr>
                  <th onClick={() => sort("id")} scope="col">
                    Identifier
                    {sortBy === "id" && (
                      <div
                        className={`arrow ${sortDirection === "asc" ? "arrow_up" : "arrow_down"}`}
                      />
                    )}
                  </th>
                  <th onClick={() => sort("name")} scope="col">
                    Name
                    {sortBy === "name" && (
                      <div
                        className={`arrow ${sortDirection === "asc" ? "arrow_up" : "arrow_down"}`}
                      />
                    )}
                  </th>
                  <th onClick={() => sort("allowed")} scope="col">
                    Status
                    {sortBy === "allowed" && (
                      <div
                        className={`arrow ${sortDirection === "asc" ? "arrow_up" : "arrow_down"}`}
                      />
                    )}
                  </th>
                  <th scope="col">Actions</th>
                </tr>
              </thead>
              <tbody>
                {displayedItems.map((item) => (
                  <tr key={item.id}>
                    <td className="sw-truncate">{item.id}</td>
                    <td className="sw-truncate">{item.name}</td>
                    <td>
                      {item.allowed === "true" ? (
                        <IconCheckCircle style={{ color: "green" }} />
                      ) : (
                        <IconError style={{ color: "darkred" }} />
                      )}
                      &nbsp;
                      {item.allowed === "true" ? "Allowed" : "Forbidden"}
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
              </tbody>
            </table>
          </div>
        </>
      )}

      <Modal
        title={editMode === "add" ? "Add License" : "Edit License"}
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
                <Label htmlFor="itemIdEdit">
                  Identifier<em className="mandatory">*</em>
                </Label>
                <TextInput
                  required
                  autoFocus={editMode === "add"}
                  disabled={editMode !== "add"}
                  value={itemToEdit.id || ""}
                  onChange={(e) => setItemToEdit({ ...itemToEdit, id: e.target.value })}
                  id="itemIdEdit"
                  name="itemIdEdit"
                  type="text"
                  size="50"
                  maxLength="255"
                />
              </div>
              <div className="modal-field">
                <Label htmlFor="itemNameEdit">
                  Name<em className="mandatory">*</em>
                </Label>
                <TextInput
                  required
                  autoFocus={editMode !== "add"}
                  value={itemToEdit.name || ""}
                  onChange={(e) => setItemToEdit({ ...itemToEdit, name: e.target.value })}
                  id="itemNameEdit"
                  name="itemNameEdit"
                  type="text"
                  size="50"
                  maxLength="255"
                />
              </div>
              <div className="modal-field">
                <Label>
                  Status<em className="mandatory">*</em>
                </Label>
                <div>
                  <Checkbox
                    label="Allowed"
                    name="itemAllowedEdit"
                    checked={itemToEdit.allowed === "true"}
                    onCheck={(checked) =>
                      setItemToEdit({
                        ...itemToEdit,
                        allowed: checked ? "true" : "false",
                      })
                    }
                  />
                </div>
              </div>
            </div>
          )
        }
      ></Modal>

      <Modal
        title="Delete license"
        description={`Are you sure you want to delete the license "${itemToDelete?.id}" / "${itemToDelete?.name}"?`}
        primaryButton={<Button onClick={() => deleteItem(itemToDelete)}>Delete</Button>}
        secondaryButton={
          <Button variety="default-ghost" onClick={cancelDelete}>
            Cancel
          </Button>
        }
        isOpen={!!itemToDelete}
        onOpenChange={(isOpen) => {
          if (!isOpen) cancelDelete();
        }}
      ></Modal>
    </div>
  );
};

export default LicensesPage;
