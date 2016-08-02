(function () {
  'use strict';

  var app = angular.module('sqlc.maven-licenses', ['ngMaterial']);
  app.controller('listMavenLicensesCtrl', function ($scope, $http, $mdDialog) {

    var checkOutLogString = "Please check out the log file for more information.";

    var base = '../static/licensecheck/mavenLicenses/';

    var loadMavenLicenses = function () {
      $http.get("/api/mavenLicenses/show").then(function (response) {
        $scope.mavenLicenses = response.data.mavenLicenses;
      });
    };

    var loadLicenses = function () {
      $http.get("/api/licenses/show").then(function (response) {
        $scope.licenses = response.data.licenses;
      });
    };

    loadMavenLicenses();
    loadLicenses();

    $scope.editMavenLicense = function (ev, mavenLicense) {

      $scope.mavenLicenseRegexEdit = mavenLicense.regex;
      $scope.mavenLicenseKeyEdit = mavenLicense.key;

      $mdDialog.show({
        templateUrl: base + 'mavenLicensesEditModal.html',
        targetEvent: ev,
        scope: $scope,
        clickOutsideToClose: true,
        preserveScope: true,
        controller: DialogController
      })
        .then(function (answer) {
          var newMavenLicense = new Object();
          newMavenLicense.oldRegex = mavenLicense.regex;
          newMavenLicense.newRegex = $scope.mavenLicenseRegexEdit;
          newMavenLicense.newKey = $scope.mavenLicenseKeyEdit;
          $http.post('/api/mavenLicenses/edit?mavenLicense=' + JSON.stringify(newMavenLicense))
            .then(
            function (response) {
              alert("Maven license edited");
              loadMavenLicenses();
            },
            function (response) {
              alert("Failed to edit maven license. " + checkOutLogString);
            });
        }, function () {
          // console.log("edit maven license canceled");
        });
    };

    $scope.addMavenLicense = function (ev) {

      $mdDialog.show({
        templateUrl: base + 'mavenLicensesAddModal.html',
        targetEvent: ev,
        scope: $scope,
        clickOutsideToClose: true,
        preserveScope: true,
        controller: DialogController
      })
        .then(function (answer) {
          var mavenLicense = new Object();
          mavenLicense.regex = $scope.mavenLicenseRegexAdd;
          mavenLicense.key = $scope.mavenLicenseKeyAdd;
          $http.post('/api/mavenLicenses/add?mavenLicense=' + JSON.stringify(mavenLicense))
            .then(
            function (response) {
              alert("Maven license added");
              loadMavenLicenses();
            },
            function (response) {
              alert("Failed to add maven license. " + checkOutLogString);
            })
        }, function () {
          // console.log("add maven license canceled");
        });

      $scope.mavenLicenseRegexAdd = "";
      $scope.mavenLicenseKeyAdd = "";
    };

    $scope.deleteMavenLicense = function (ev, mavenLicense) {

      $scope.mavenLicenseKeyDelete = mavenLicense.key;
      $scope.mavenLicenseRegexDelete = mavenLicense.regex;

      $mdDialog.show({
        templateUrl: base + 'mavenLicensesDeleteModal.html',
        targetEvent: ev,
        scope: $scope,
        clickOutsideToClose: true,
        preserveScope: true,
        controller: DialogController
      }).then(function () {
        $http.post('/api/mavenLicenses/delete?mavenLicense=' + JSON.stringify(mavenLicense))
          .then(
          function (response) {
            alert("Maven license deleted");
            loadMavenLicenses();
          },
          function (response) {
            alert("Failed to delete maven license. " + checkOutLogString);
          });
      },
        function () {
          // console.log("deletion aborted");
        });
    };

  });

  app.filter('searchFor', function () {
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
