import { useState } from 'react';
import './icons.css';

const Dependencies = ({ dependencies }) => {
  const [sortByDep, setSortByDep] = useState('status');
  const [sortDirectionDep, setSortDirectionDep] = useState('desc');

  const columns = dependencies.length > 0 ? Object.keys(dependencies[0]) : [];

  const sortedDependencies = [...dependencies].sort((a, b) => {
    const modifier = sortDirectionDep === 'desc' ? -1 : 1;
    if (a[sortByDep] < b[sortByDep]) return -1 * modifier;
    if (a[sortByDep] > b[sortByDep]) return modifier;
    return 0;
  });

  const sort = (param) => {
    if (param === sortByDep) {
      setSortDirectionDep(sortDirectionDep === 'asc' ? 'desc' : 'asc');
    }
    setSortByDep(param);
  };

  return (
    <div className="boxed-group boxed-group-inner">
      <h3>Dependencies</h3>
      <table className="data zebra">
        <caption>
          Here you see all project dependencies from Maven (including transitive) and NPM.
        </caption>
        <thead>
          <tr>
            {columns.map((dependency) => (
              <th key={dependency} onClick={() => sort(dependency)} scope="col">
                {dependency}
                {dependency === sortByDep && (
                  <div className={`arrow ${sortDirectionDep === 'asc' ? 'arrow_up' : 'arrow_down'}`} />
                )}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {sortedDependencies.map((dependency) => (
            <tr key={dependency.name}>
              <td>{dependency.name}</td>
              <td>{dependency.version}</td>
              <td>{dependency.license}</td>
              <td>
                <span className={
                  dependency.status === 'Allowed' ? 'icon-license-ok' :
                  dependency.status === 'Forbidden' ? 'icon-license-nok' :
                  'icon-license-unknown'
                } />
                {dependency.status}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default Dependencies;