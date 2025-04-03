import saveAs from 'file-saverjs';
import { useEffect, useState } from 'react';
import Dependencies from './dependencies';
import buildExcel from './excel-builder';
import Licenses from './licenses';

const Dashboard = ({ options }) => {
  const [licenses, setLicenses] = useState([]);
  const [dependencies, setDependencies] = useState([]);
  const component = options.component;

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const request = {
      component: component.key,
      metricKeys: "licensecheck.license,licensecheck.dependency"
    };

    if (params.has("branch")) {
      request.branch = params.get("branch");
    } else if (params.has("pullRequest")) {
      request.pullRequest = params.get("pullRequest");
    }

    window.SonarRequest.getJSON("/api/measures/component", request).then((response) => {
      const processedLicenses = [];
      const processedDependencies = [];

      response.component.measures.forEach((measure) => {
        if (measure.metric === "licensecheck.license") {
          processedLicenses.push(...JSON.parse(measure.value));
        } else if (measure.metric === "licensecheck.dependency") {
          processedDependencies.push(...JSON.parse(measure.value));
        }
      });

      processedDependencies.forEach((dependency) => {
        dependency.status = "Unknown";
        processedLicenses.forEach((license) => {
          if (dependency.license === license.identifier) {
            dependency.status = license.status === "true" ? "Allowed" : "Forbidden";
          }
        });
      });

      setLicenses(processedLicenses);
      setDependencies(processedDependencies);
    });
  }, [component.key]);

  const exportExcel = () => {
    const blob = new Blob([buildExcel(dependencies, licenses)], {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    });
    saveAs(blob, `license-check-${component.key}.xls`);
  };

  return (
    <div className="page page-limited">
      <h1>License Check</h1>
      <div><a href="#" onClick={exportExcel}>Export to Excel</a></div>
      <p>&nbsp;</p>
      <Licenses licenses={licenses} />
      <p>&nbsp;</p>
      <Dependencies dependencies={dependencies} />
    </div>
  );
};

export default Dashboard;