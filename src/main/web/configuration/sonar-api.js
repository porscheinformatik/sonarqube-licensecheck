import {KEYS} from "../property_keys";

export function loadProjects() {
  return window.SonarRequest
    .getJSON('/api/components/search?qualifiers=TRK&pageSize=500') // TODO > 500 projects?
    .then(response => response.components);
}

export function loadLicenses() {
  return window.SonarRequest
    .getJSON(`/api/settings/values?keys=${KEYS.LICENSE_SET}`)
    .then(response => response.settings[0].fieldValues);
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
    .then(response => response.settings[0].fieldValues);
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
    .then(response => response.settings[0].fieldValues);
}

export function saveDependencyMappings(items) {
  return window.SonarRequest
    .post(`/api/settings/set`, {
      key: KEYS.DEPENDENCY_MAPPING,
      fieldValues: items.map(i => JSON.stringify(i)),
    })
}
