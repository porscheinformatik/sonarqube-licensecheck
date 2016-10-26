(function () {
  'use strict';

  var app = angular.module('sqlc.projectLicenses', ['ngMaterial', 'sqlc.common']);
  app.controller('projectLicenseCtrl', function ($scope, $http, $httpParamSerializerJQLike, $mdDialog) {

    var checkOutLogString = 'Please check out the log file for more information.';

    var base = window.baseUrl + '/static/licensecheck/projectLicenses/';

    var loadProjectLicenses = function () {
      $http.get(window.baseUrl + '/api/projectLicenses/show').then(function (response) {
        $scope.projectLicenses = response.data;
      });
    };

    var loadProjects = function () {
      $http.get(window.baseUrl + '/api/resources?format=json').then(function (response) {
        $scope.resources = response.data;
      });
    };

    var loadLicenses = function () {
      $http.get(window.baseUrl + '/api/licenses/show').then(function (response) {
        $scope.licenses = response.data;
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
      $scope.projectLicenseProjectEdit = projectLicense.projectKey;
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

          var changedProjectLicense = {
            projectKey: projectLicense.projectKey,
            license: projectLicense.license,
            status: $scope.projectLicenseStatusEdit
          };

          $http({
            url: window.baseUrl + '/api/projectLicenses/edit',
            method: 'POST',
            data: $httpParamSerializerJQLike(changedProjectLicense),
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            }
          }).then(
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
          var projectLicense = {
            license: $scope.projectLicenseLicenseAdd,
            status: $scope.projectLicenseStatusAdd,
            projectKey: $scope.projectLicenseProjectAdd
          };

          $http({
            url: window.baseUrl + '/api/projectLicenses/add',
            method: 'POST',
            data: $httpParamSerializerJQLike(projectLicense),
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            }
          }).then(
            function (response) {
              loadProjectLicenses();
            },
            function (response) {
              alert('Failed to add project license. ' + checkOutLogString);
            });
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
        var projectLicenseToDelete = {
          projectKey: projectLicense.projectKey,
          license: projectLicense.license
        };

        $http({
          url: window.baseUrl + '/api/projectLicenses/delete',
          method: 'POST',
          data: $httpParamSerializerJQLike(projectLicenseToDelete),
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        }).then(
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
