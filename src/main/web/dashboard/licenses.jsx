import { useState } from "react";
import "./icons.css";

const Licenses = ({ licenses }) => {
  const [sortBy, setSortBy] = useState("status");
  const [sortDirection, setSortDirection] = useState("asc");

  const columns = licenses.length > 0 ? Object.keys(licenses[0]) : [];

  const sortedLicenses = [...licenses].sort((a, b) => {
    const modifier = sortDirection === "desc" ? -1 : 1;
    if (a[sortBy] < b[sortBy]) return -1 * modifier;
    if (a[sortBy] > b[sortBy]) return 1 * modifier;
    return 0;
  });

  const sort = (param) => {
    if (param === sortBy) {
      setSortDirection(sortDirection === "asc" ? "desc" : "asc");
    }
    setSortBy(param);
  };

  return (
    <div className="boxed-group boxed-group-inner">
      <h3>Licenses</h3>
      <table className="data zebra">
        <caption>This is a list of all licenses used in any dependencies listed below.</caption>
        <thead>
          <tr>
            {columns.map((license) => (
              <th key={license} onClick={() => sort(license)} scope="col">
                {license}
                {license === sortBy && (
                  <div className={`arrow ${sortDirection === "asc" ? "arrow_up" : "arrow_down"}`} />
                )}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {sortedLicenses.map((license) => (
            <tr key={license.identifier}>
              <td>{license.identifier}</td>
              <td>{license.name}</td>
              <td>
                <span
                  className={`${license.status === "true" ? "icon-license-ok" : "icon-license-nok"}`}
                />
                {license.status === "true" ? "Allowed" : "Forbidden"}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default Licenses;
