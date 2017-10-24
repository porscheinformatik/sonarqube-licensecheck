(function () {
  'use strict';

  var app = angular.module('sqlc.licenses', ['ngMaterial', 'sqlc.common']);
  app.controller('listLicensesCtrl', function ($scope, $http, $httpParamSerializerJQLike, $mdDialog) {

    var checkOutLogString = 'Please check out the log file for more information.';

    var base = window.baseUrl + '/static/licensecheck/licenses/';

    var loadLicenses = function () {
      $http.get(window.baseUrl + '/api/licensecheck/licenses/show').then(function (response) {
        $scope.licenses = response.data;
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
        .then(function () {
          var changedLicense = {
            identifier: license.identifier,
            name: $scope.licenseNameEdit,
            status: $scope.licenseStatusEdit
          };
          $http({
            url: window.baseUrl + '/api/licensecheck/licenses/edit',
            method: 'POST',
            data: $httpParamSerializerJQLike(changedLicense),
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            }
          }).then(
            function () {
              loadLicenses();
            },
            function () {
              alert('Failed to edit license. ' + checkOutLogString);
            });
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
      }).then(function () {
        var license = {
          name: $scope.licenseNameAdd,
          identifier: $scope.licenseIdentifierAdd,
          status: $scope.licenseStatusAdd
        };
        $http({
          url: window.baseUrl + '/api/licensecheck/licenses/add',
          method: 'POST',
          data: $httpParamSerializerJQLike(license),
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        }).then(
          function () {
            loadLicenses();
          },
          function () {
            alert('Failed to add license. ' + checkOutLogString);
          });
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
          $http.post(window.baseUrl + '/api/licensecheck/licenses/delete?identifier=' + license.identifier)
            .then(
              function () {
                loadLicenses();
              },
              function () {
                alert('Failed to delete license. ' + checkOutLogString);
              });
        });
    };
  });
}());
