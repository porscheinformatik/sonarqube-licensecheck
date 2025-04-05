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
import { loadLicenses, loadProjectLicenses, loadProjects, saveProjectLicenses } from "./sonar-api";

const ProjectLicensesPage = () => {
  const [items, setItems] = useState([]);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [itemToEdit, setItemToEdit] = useState(null);
  const [editMode, setEditMode] = useState(null);
  const [searchText, setSearchText] = useState("");
  const [licenses, setLicenses] = useState([]);
  const [projects, setProjects] = useState([]);
  const [sortBy, setSortBy] = useState("status");
  const [sortDirection, setSortDirection] = useState("asc");

  useEffect(() => {
    load();
  }, []);

  const load = () => {
    return Promise.all([
      loadLicenses().then((l) => setLicenses(l)),
      loadProjects().then((p) => setProjects(p)),
      loadProjectLicenses().then((pl) => setItems(pl)),
    ]);
  };

  const findProjectName = (projectKey) => {
    const projectItem = projects.find((p) => p.key === projectKey);
    return projectItem ? projectItem.name : "-";
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
    setItemToEdit({ ...item });
    setEditMode("edit");
  };

  const cancelEdit = () => {
    setItemToEdit(null);
  };

  const saveItems = (items) => {
    return saveProjectLicenses(items).then(() => {
      loadProjectLicenses().then((pl) => setItems(pl));
      setItemToEdit(null);
      setItemToDelete(null);
    });
  };

  const saveItem = (item) => {
    if (editMode === "add") {
      saveItems([...items, item]);
    } else {
      const newItems = [...items];
      const itemToChange = newItems.find(
        (i) => i.projectKey === item.projectKey && i.license === item.license,
      );
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
    saveItems(items.filter((i) => i.projectKey !== item.projectKey || i.license !== item.license));
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
          item.projectKey.toLowerCase().includes(searchText.toLowerCase()) ||
          item.license.toLowerCase().includes(searchText.toLowerCase()),
      );

  return (
    <div>
      <header className="sw-mb-5">
        <h1 className="sw-mb-4">License Check - Project Licenses</h1>
        <div className="sw-mb-4">Allow/disallow licences for specific projects.</div>
        <div className="page-actions">
          <Button onClick={showAddDialog}>Add Project License</Button>
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
        <table className="data zebra">
          <thead>
            <tr>
              <th onClick={() => sort("projectName")} scope="col">
                Project
                {sortBy === "projectName" && (
                  <div className={`arrow ${sortDirection === "asc" ? "arrow_up" : "arrow_down"}`} />
                )}
              </th>
              <th onClick={() => sort("license")} scope="col">
                License
                {sortBy === "license" && (
                  <div className={`arrow ${sortDirection === "asc" ? "arrow_up" : "arrow_down"}`} />
                )}
              </th>
              <th onClick={() => sort("status")} scope="col">
                Status
                {sortBy === "status" && (
                  <div className={`arrow ${sortDirection === "asc" ? "arrow_up" : "arrow_down"}`} />
                )}
              </th>
              <th scope="col">Actions</th>
            </tr>
          </thead>
          <tbody>
            {displayedItems.map((item) => (
              <tr key={`${item.projectKey}-${item.license}`}>
                <td>
                  <span title={item.projectKey}>{findProjectName(item.projectKey)}</span>
                </td>
                <td>
                  {item.license} / {findLicenseName(item.license)}
                </td>
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
                  <ButtonIcon
                    variety="default-ghost"
                    ariaLabel="Delete"
                    Icon={IconDelete}
                    onClick={() => showDeleteDialog(item)}
                  />
                </td>
              </tr>
            ))}
            {!displayedItems.length && (
              <tr>
                <td colSpan="4">No project licenses available</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <Modal
        title={editMode === "add" ? "Add Project License" : "Edit Project License"}
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
                <Label htmlFor="projectSelect">
                  Project<em className="mandatory">*</em>
                </Label>
                <select
                  required
                  disabled={editMode !== "add"}
                  value={itemToEdit.projectKey || ""}
                  onChange={(e) => setItemToEdit({ ...itemToEdit, projectKey: e.target.value })}
                  id="projectSelect"
                  name="projectSelect"
                  className="sw-w-full sw-px-3 sw-py-2 sw-border sw-border-neutral-200 sw-rounded"
                >
                  <option value="">Select a project</option>
                  {projects.map((project) => (
                    <option key={project.key} value={project.key}>
                      {project.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="modal-field">
                <Label htmlFor="licenseSelect">
                  License<em className="mandatory">*</em>
                </Label>
                <select
                  required
                  disabled={editMode !== "add"}
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
                <Label>Status</Label>
                <div>
                  <Checkbox
                    label="Allowed"
                    name="allowed"
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
      />

      <Modal
        title="Delete Project License"
        isOpen={!!itemToDelete}
        onOpenChange={(isOpen) => {
          if (!isOpen) {
            cancelDelete();
          }
        }}
        description={`Are you sure you want to delete the project license "${findProjectName(
          itemToDelete?.projectKey,
        )}" / "${itemToDelete?.license}"?`}
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

export default ProjectLicensesPage;
