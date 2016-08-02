function DialogController($scope, $mdDialog) {
  $scope.hide = function () {
    $mdDialog.hide();
  };

  $scope.cancel = function () {
    $mdDialog.cancel();
  };

  $scope.answer = function (answer) {
    $mdDialog.hide(answer);
  };

  $scope.check = function (inputArray) {
    var inputIsValid = true;

    for (var i = 0; i < inputArray.length; i++) {
      if (isNullOrWhitespace(inputArray[i])) {
        inputIsValid = false;
      }
    }

    if (inputIsValid) {
      $mdDialog.hide();
    }

  };

  function isNullOrWhitespace(input) {
    return !input || !input.trim();
  };
};

