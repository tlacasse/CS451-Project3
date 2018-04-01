(function () {
    'use strict';

    angular
        .module('tttModule')

        .directive('errorMessage', function () {
            return {
                restrict: 'E',
                scope: {
                    controller: '='
                },
                templateUrl: '/views/message/message.html'
            };
        })

    ;  
}());

