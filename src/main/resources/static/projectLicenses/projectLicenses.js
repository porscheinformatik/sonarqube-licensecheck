(function () {
  'use strict';

  var app = angular.module('sqlc.projectLicenses', ['ngMaterial', 'sqlc.common']);
  app.controller('projectLicenseCtrl', function ($scope, $http, $mdDialog) {

    var checkOutLogString = 'Please check out the log file for more information.';

    var base = '../static/licensecheck/projectLicenses/';

    var loadProjectLicenses = function () {
      $http.get('/api/projectLicenses/show').then(function (response) {
        $scope.projectLicenses = response.data.projectLicenses;
      });
    };

    var loadProjects = function () {
      $http.get('/api/resources?format=json').then(function (response) {
        $scope.resources = response.data;
      });
    };

    var loadLicenses = function () {
      $http.get('/api/licenses/show').then(function (response) {
        $scope.licenses = response.data.licenses;
      });
    };

    var getProjectKey = function (projectName) {
      for (var i = 0; i < $scope.resources.length; i++) {
        var obj = $scope.resources[i];
        if (obj.name === projectName) {
          return obj.key;
        }
      }
    };

    loadProjectLicenses();
    loadProjects();
    loadLicenses();

    $scope.editProjectLicense = function (ev, projectLicense) {

      $scope.projectLicenseLicenseEdit = projectLicense.license;
      $scope.projectLicenseProjectEdit = projectLicense.projectName;
      $scope.projectLicenseStatusEdit = projectLicense.status;

      $mdDialog.show({
        templateUrl: base + 'projectLicensesEditModal.html',
        targetEvent: ev,
        scope: $scope,
        clickOutsideToClose: true,
        preserveScope: true,
        controller: 'DialogController'
      })
        .then(function (answer) {
          var newProjectLicense = new Object();
          newProjectLicense.oldLicense = projectLicense.license;
          newProjectLicense.oldProjectName = projectLicense.projectName;
          newProjectLicense.oldStatus = projectLicense.status;
          newProjectLicense.oldProjectKey = getProjectKey(projectLicense.projectName);
          newProjectLicense.newLicense = $scope.projectLicenseLicenseEdit;
          newProjectLicense.newProjectName = $scope.projectLicenseProjectEdit;
          newProjectLicense.newStatus = $scope.projectLicenseStatusEdit;
          newProjectLicense.newProjectKey = getProjectKey(newProjectLicense.newProjectName);

          $http.post('/api/projectLicenses/edit?projectLicense=' + JSON.stringify(newProjectLicense))
            .then(
            function (response) {
              loadProjectLicenses();
            },
            function (response) {
              alert('Failed to edit project license. ' + checkOutLogString);
            });
        }, function () {
          // console.log('edit license canceled');
        });
    };

    $scope.addProjectLicense = function (ev) {

      $mdDialog.show({
        templateUrl: base + 'projectLicensesAddModal.html',
        targetEvent: ev,
        scope: $scope,
        clickOutsideToClose: true,
        preserveScope: true,
        controller: 'DialogController'
      })
        .then(function (answer) {
          var projectLicense = new Object();
          projectLicense.license = $scope.projectLicenseLicenseAdd;
          projectLicense.projectName = $scope.projectLicenseProjectAdd;
          projectLicense.status = $scope.projectLicenseStatusAdd;
          projectLicense.projectKey = getProjectKey(projectLicense.projectName);

          $http.post('/api/projectLicenses/add?projectLicense=' + JSON.stringify(projectLicense))
            .then(
            function (response) {
              loadProjectLicenses();
            },
            function (response) {
              alert('Failed to add project license. ' + checkOutLogString);
            })
        }, function () {
          // console.log('add license canceled');
        });

      $scope.projectLicenseLicenseAdd = '';
      $scope.projectLicenseProjectAdd = '';
      $scope.projectLicenseStatusAdd = '';
    };

    $scope.deleteProjectLicense = function (ev, projectLicense) {

      $scope.projectLicenseLicenseDelete = projectLicense.license;
      $scope.projectLicenseProjectDelete = projectLicense.projectName;
      $scope.projectLicenseStatusDelete = projectLicense.status;

      $mdDialog.show({
        templateUrl: base + 'projectLicensesDeleteModal.html',
        targetEvent: ev,
        scope: $scope,
        clickOutsideToClose: true,
        preserveScope: true,
        controller: 'DialogController'
      }).then(function () {
        $http.post('/api/projectLicenses/delete?projectLicense=' + JSON.stringify(projectLicense))
          .then(
          function (response) {
            loadProjectLicenses();
          },
          function (response) {
            alert('Failed to delete project license. ' + checkOutLogString);
          });
      },
        function () {
          // console.log('deletion aborted');
        });
    };
  });
} ());
