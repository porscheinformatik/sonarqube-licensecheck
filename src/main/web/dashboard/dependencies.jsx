import {
  IconCheckCircle,
  IconError,
  IconTriangleDown,
  IconTriangleUp,
} from "@sonarsource/echoes-react";
import { useState } from "react";

const Dependencies = ({ dependencies }) => {
  const [sortBy, setSortBy] = useState("name");
  const [sortDirection, setSortDirection] = useState("asc");

  const sortedDependencies = [...dependencies].sort((a, b) => {
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
      <h2 className="sw-text-xl sw-font-bold sw-mb-4">Dependencies</h2>
      <div>
        <table className="sqlc-data">
          <caption>
            Here you see all project dependencies from Maven (including transitive) and NPM.
          </caption>
          <thead>
            <tr>
              <th onClick={() => sort("name")} scope="col">
                Name
                {sortBy === "name" &&
                  (sortDirection === "asc" ? <IconTriangleUp /> : <IconTriangleDown />)}
              </th>
              <th onClick={() => sort("version")} scope="col">
                Version
                {sortBy === "version" &&
                  (sortDirection === "asc" ? <IconTriangleUp /> : <IconTriangleDown />)}
              </th>
              <th onClick={() => sort("license")} scope="col">
                License
                {sortBy === "license" &&
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
            {sortedDependencies.map((dependency) => (
              <tr key={`${dependency.name}-${dependency.version}`}>
                <td className="sw-truncate">{dependency.name}</td>
                <td className="sw-truncate">{dependency.version}</td>
                <td className="sw-truncate">{dependency.license}</td>
                <td>
                  {dependency.status === "Allowed" ? (
                    <IconCheckCircle style={{ color: "green" }} />
                  ) : dependency.status === "Forbidden" ? (
                    <IconError style={{ color: "darkred" }} />
                  ) : (
                    <IconError style={{ color: "orange" }} />
                  )}
                  &nbsp;
                  {dependency.status}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Dependencies;
