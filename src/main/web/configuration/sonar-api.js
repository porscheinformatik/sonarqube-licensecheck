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

export function loadMavenLicenses() {
  return window.SonarRequest
    .getJSON(`/api/settings/values?keys=${KEYS.MAVEN_LICENSE_MAPPING}`)
    .then(response => response.settings[0].fieldValues);
}

export function saveMavenLicenses(items) {
  return window.SonarRequest
    .post(`/api/settings/set`, {
      key: KEYS.MAVEN_LICENSE_MAPPING,
      fieldValues: items.map(i => JSON.stringify(i)),
    });
}

export function loadMavenDependencies() {
  return window.SonarRequest
    .getJSON(`/api/settings/values?keys=${KEYS.MAVEN_DEPENDENCY_MAPPING}`)
    .then(response => response.settings[0].fieldValues);
}

export function saveMavenDependencies(items) {
  return window.SonarRequest
    .post(`/api/settings/set`, {
      key: KEYS.MAVEN_DEPENDENCY_MAPPING,
      fieldValues: items.map(i => JSON.stringify(i)),
    })
}
