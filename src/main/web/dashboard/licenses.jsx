import {
  IconCheckCircle,
  IconError,
  IconTriangleDown,
  IconTriangleUp,
} from "@sonarsource/echoes-react";
import { useState } from "react";

const Licenses = ({ licenses }) => {
  const [sortBy, setSortBy] = useState("identifier");
  const [sortDirection, setSortDirection] = useState("asc");

  const sortedLicenses = [...licenses].sort((a, b) => {
    const modifier = sortDirection === "desc" ? -1 : 1;
    if (a[sortBy] < b[sortBy]) return -1 * modifier;
    if (a[sortBy] > b[sortBy]) return modifier;
    return 0;
  });

  const sort = (param) => {
    if (param === sortBy) {
      setSortDirection(sortDirection === "asc" ? "desc" : "asc");
    }
    setSortBy(param);
  };

  return (
    <div>
      <h2 className="sw-text-xl sw-font-bold sw-mb-4">Licenses</h2>
      <div>
        <table className="sqlc-data">
          <caption>This is a list of all licenses used in any dependencies listed below.</caption>
          <thead>
            <tr>
              <th onClick={() => sort("identifier")} scope="col">
                Identifier
                {sortBy === "identifier" &&
                  (sortDirection === "asc" ? <IconTriangleUp /> : <IconTriangleDown />)}
              </th>
              <th onClick={() => sort("name")} scope="col">
                Name
                {sortBy === "name" &&
                  (sortDirection === "asc" ? <IconTriangleUp /> : <IconTriangleDown />)}
              </th>
              <th onClick={() => sort("status")} scope="col">
                Status
                {sortBy === "status" &&
                  (sortDirection === "asc" ? <IconTriangleUp /> : <IconTriangleDown />)}
              </th>
            </tr>
          </thead>
          <tbody>
            {sortedLicenses.map((license) => (
              <tr key={license.identifier}>
                <td className="sw-truncate">{license.identifier}</td>
                <td className="sw-truncate">{license.name}</td>
                <td>
                  {license.status === "true" ? (
                    <IconCheckCircle style={{ color: "green" }} />
                  ) : (
                    <IconError style={{ color: "darkred" }} />
                  )}
                  &nbsp;
                  {license.status === "true" ? "Allowed" : "Forbidden"}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Licenses;
