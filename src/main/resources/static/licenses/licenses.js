(function () {
  'use strict';

  var app = angular.module('sqlc.licenses', ['ngMaterial', 'sqlc.common']);
  app.controller('listLicensesCtrl', function ($scope, $http, $mdDialog) {

    var checkOutLogString = 'Please check out the log file for more information.';

    var base = '../static/licensecheck/licenses/';

    var loadLicenses = function () {
      $http.get('/api/licenses/show').then(function (response) {
        $scope.licenses = response.data.licenses;
      });
    };

    loadLicenses();

    $scope.editLicense = function (ev, license) {

      $scope.licenseNameEdit = license.name;
      $scope.licenseIdentifierEdit = license.identifier;
      $scope.licenseStatusEdit = license.status;

      $mdDialog.show({
        templateUrl: base + 'licensesEditModal.html',
        targetEvent: ev,
        scope: $scope,
        clickOutsideToClose: true,
        preserveScope: true,
        controller: 'DialogController'
      })
        .then(function (answer) {
          var newLicense = new Object();
          newLicense.oldIdentifier = license.identifier;
          newLicense.newName = $scope.licenseNameEdit;
          newLicense.newIdentifier = $scope.licenseIdentifierEdit;
          newLicense.newStatus = $scope.licenseStatusEdit;
          $http.post('/api/licenses/edit?license=' + JSON.stringify(newLicense))
            .then(
            function (response) {
              alert('License edited');
              loadLicenses();
            },
            function (response) {
              alert('Failed to edit license. ' + checkOutLogString);
            });
        }, function () {
          // console.log('edit license canceled');
        });
    };

    $scope.addLicense = function (ev) {

      $mdDialog.show({
        templateUrl: base + 'licensesAddModal.html',
        targetEvent: ev,
        scope: $scope,
        clickOutsideToClose: true,
        preserveScope: true,
        controller: 'DialogController'
      })
        .then(function (answer) {
          var license = new Object();
          license.name = $scope.licenseNameAdd;
          license.identifier = $scope.licenseIdentifierAdd;
          license.status = $scope.licenseStatusAdd;
          $http.post('/api/licenses/add?license=' + JSON.stringify(license))
            .then(
            function (response) {
              alert('License added');
              loadLicenses();
            },
            function (response) {
              alert('Failed to add license. ' + checkOutLogString);
            })
        }, function () {
          // console.log('add license canceled');
        });

      $scope.licenseNameAdd = '';
      $scope.licenseIdentifierAdd = '';
      $scope.licenseStatusAdd = '';
    };

    $scope.deleteLicense = function (ev, license) {

      $scope.licenseNameDelete = license.name;
      $scope.licenseIdentifierDelete = license.identifier;
      $scope.licenseStatusDelete = license.status;

      $mdDialog.show({
        templateUrl: base + 'licensesDeleteModal.html',
        targetEvent: ev,
        scope: $scope,
        clickOutsideToClose: true,
        preserveScope: true,
        controller: 'DialogController'
      }).then(function () {
        $http.post('/api/licenses/delete?license=' + JSON.stringify(license))
          .then(
          function (response) {
            alert('License deleted');
            loadLicenses();
          },
          function (response) {
            alert('Failed to delete license. ' + checkOutLogString);
          });
      },
        function () {
          // console.log('deletion aborted');
        });
    };
  });
} ());
