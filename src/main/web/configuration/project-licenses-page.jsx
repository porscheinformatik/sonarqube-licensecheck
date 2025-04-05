import { useEffect, useState } from "react";
import "../dashboard/icons.css";
import { DeleteIcon, MagnifyIcon, PencilIcon } from "../icons";
import ModalDialog from "../modal-dialog";
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
    <div className="boxed-group boxed-group-inner">
      <header className="page-header">
        <h1 className="page-title">License Check - Project Licenses</h1>
        <div className="page-description">Allow/disallow licences for specific projects.</div>
        <div className="page-actions">
          <button className="button" id="license-add" onClick={showAddDialog}>
            Add License
          </button>
        </div>
      </header>

      <div className="panel panel-vertical bordered-bottom spacer-bottom">
        <div className="search-box">
          <MagnifyIcon
            width={15}
            height={16}
            style={{ paddingLeft: 5, marginTop: 4, fill: "#999" }}
          />
          <input
            style={{ background: "none", width: "100%", border: "none" }}
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
          <caption>This is a list of all project specific licenses</caption>
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
                  <span
                    className={item.allowed === "true" ? "icon-license-ok" : "icon-license-nok"}
                  />
                  {item.allowed === "true" ? "Allowed" : "Forbidden"}
                </td>
                <td className="thin nowrap">
                  <a className="button" onClick={() => showEditDialog(item)} title="Edit item">
                    <PencilIcon style={{ fill: "currentcolor" }} />
                  </a>
                  <a className="button" onClick={() => showDeleteDialog(item)} title="Delete item">
                    <DeleteIcon style={{ fill: "rgb(212, 51, 63)" }} />
                  </a>
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

      <ModalDialog
        header={editMode === "add" ? "Add License" : "Edit License"}
        show={!!itemToEdit}
        onClose={cancelEdit}
      >
        {itemToEdit && (
          <>
            <div className="modal-field">
              <label htmlFor="projectSelect">
                Project<em className="mandatory">*</em>
              </label>
              <select
                required
                disabled={editMode !== "add"}
                value={itemToEdit.projectKey || ""}
                onChange={(e) => setItemToEdit({ ...itemToEdit, projectKey: e.target.value })}
                id="projectSelect"
                name="projectSelect"
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
              <label htmlFor="licenseSelect">
                License<em className="mandatory">*</em>
              </label>
              <select
                required
                disabled={editMode !== "add"}
                value={itemToEdit.license || ""}
                onChange={(e) => setItemToEdit({ ...itemToEdit, license: e.target.value })}
                id="licenseSelect"
                name="licenseSelect"
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
              <label>
                Status<em className="mandatory">*</em>
              </label>
              <label htmlFor="itemStatusEdit">
                <input
                  type="checkbox"
                  id="itemStatusEdit"
                  name="itemStatusEdit"
                  checked={itemToEdit.allowed === "true"}
                  onChange={(e) =>
                    setItemToEdit({
                      ...itemToEdit,
                      allowed: e.target.checked ? "true" : "false",
                    })
                  }
                />
                Allowed
              </label>
            </div>
            <div className="modal-foot">
              <button className="button" onClick={() => saveItem(itemToEdit)}>
                Save
              </button>
            </div>
          </>
        )}
      </ModalDialog>

      <ModalDialog header="Delete License" show={!!itemToDelete} onClose={cancelDelete}>
        {itemToDelete && (
          <>
            <div>
              Are you sure you want to delete the license mapping "
              {findProjectName(itemToDelete.projectKey)}" / "{itemToDelete.license}"?
            </div>
            <div className="modal-foot">
              <button className="button" onClick={() => deleteItem(itemToDelete)}>
                Delete
              </button>
            </div>
          </>
        )}
      </ModalDialog>
    </div>
  );
};

export default ProjectLicensesPage;
