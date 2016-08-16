(function () {
  'use strict';

  var mavenDependencyModule = angular.module('sqlc.maven-dependencies');
  var mavenLicenseModule = angular.module('sqlc.maven-licenses');
  var licenseModule = angular.module('sqlc.licenses');

  mavenDependencyModule.filter('searchForMavenDependency', function () {
    return function (arr, searchString) {
      if (!searchString) {
        return arr;
      }
      var result = [];
      searchString = searchString.toLowerCase();
      angular.forEach(arr, function (mavenDependency) {
        if (mavenDependency.key.toLowerCase().indexOf(searchString) !== -1) {
          result.push(mavenDependency);
        }
        else if (mavenDependency.license.toLowerCase().indexOf(searchString) !== -1) {
          result.push(mavenDependency);
        }
      });
      return result;
    };
  });

  licenseModule.filter('searchForLicense', function () {
    return function (arr, searchString) {
      if (!searchString) {
        return arr;
      }
      var result = [];
      searchString = searchString.toLowerCase();
      angular.forEach(arr, function (license) {
        if (license.name.toLowerCase().indexOf(searchString) !== -1) {
          result.push(license);
        }
        else if (license.identifier.toLowerCase().indexOf(searchString) !== -1) {
          result.push(license);
        }
        else if (license.status.toLowerCase().indexOf(searchString) !== -1) {
          result.push(license);
        }
      });
      return result;
    };
  });

  mavenLicenseModule.filter('searchForMavenLicense', function () {
    return function (arr, searchString) {
      if (!searchString) {
        return arr;
      }
      var result = [];
      searchString = searchString.toLowerCase();
      angular.forEach(arr, function (mavenLicense) {
        if (mavenLicense.key.toLowerCase().indexOf(searchString) !== -1) {
          result.push(mavenLicense);
        }
        else if (mavenLicense.regex.toLowerCase().indexOf(searchString) !== -1) {
          result.push(mavenLicense);
        }
      });
      return result;
    };
  });
} ());
