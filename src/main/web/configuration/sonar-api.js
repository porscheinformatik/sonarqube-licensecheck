import {KEYS} from "../property_keys";

function loadProjectPage(allProjects, pageIndex) {
  return window.SonarRequest
    .getJSON(`/api/components/search?qualifiers=TRK&ps=500&p=${pageIndex}`)
    .then(response => {
      const projects = response.components || [];
      allProjects = allProjects.concat(projects);
      if (response.paging && response.paging.total > pageIndex * 500) {
        return loadProjectPage(allProjects.concat(projects), pageIndex + 1);
      } else {
        return allProjects;
      }
    });
}

export function loadProjects() {
  return loadProjectPage([], 1);
}

export function loadLicenses() {
  return window.SonarRequest
    .getJSON(`/api/settings/values?keys=${KEYS.LICENSE_SET}`)
    .then(response => response.settings && response.settings.length > 0 ? response.settings[0].fieldValues : []);
}

export function saveLicenses(items) {
  return window.SonarRequest
    .post(`/api/settings/set`, {
      key: KEYS.LICENSE_SET,
      fieldValues: items.map(i => JSON.stringify(i)),
    });
}

export function loadProjectLicenses() {
  return window.SonarRequest
    .getJSON(`/api/settings/values?keys=${KEYS.PROJECT_LICENSE_SET}`)
    .then(response => response.settings && response.settings.length > 0 ? response.settings[0].fieldValues : []);
}

export function saveProjectLicenses(items) {
  return window.SonarRequest
    .post(`/api/settings/set`, {
      key: KEYS.PROJECT_LICENSE_SET,
      fieldValues: items.map(i => JSON.stringify(i)),
    })
}

export function loadLicenseMappings() {
  return window.SonarRequest
    .getJSON(`/api/settings/values?keys=${KEYS.LICENSE_MAPPING}`)
    .then(response => response.settings[0].fieldValues);
}

export function saveLicenseMappings(items) {
  return window.SonarRequest
    .post(`/api/settings/set`, {
      key: KEYS.LICENSE_MAPPING,
      fieldValues: items.map(i => JSON.stringify(i)),
    });
}

export function loadDependencyMappings() {
  return window.SonarRequest
    .getJSON(`/api/settings/values?keys=${KEYS.DEPENDENCY_MAPPING}`)
    .then(response => response.settings && response.settings.length > 0 ? response.settings[0].fieldValues : []);
}

export function saveDependencyMappings(items) {
  return window.SonarRequest
    .post(`/api/settings/set`, {
      key: KEYS.DEPENDENCY_MAPPING,
      fieldValues: items.map(i => JSON.stringify(i)),
    })
}
