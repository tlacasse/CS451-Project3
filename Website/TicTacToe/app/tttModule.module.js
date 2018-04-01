(function () {
    'use strict';

    angular
        .module('tttModule', ['ui.router'])
        .config(['$stateProvider', '$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {

            $urlRouterProvider.otherwise('/tictactoe');

            $stateProvider
            .state('main', {
                url: '/tictactoe',
                templateUrl: 'views/tictactoe.html',
                controller: 'gameController',
                controllerAs: 'vm'
            })

            ;

        }]);
}());