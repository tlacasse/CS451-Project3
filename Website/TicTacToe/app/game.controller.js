(function () {
    'use strict';

    angular
        .module('tttModule')

        .controller('gameController', ['$http'
                , function ($http) {
            var vm = this;

            vm.test = 'hello world';

        }]);
    
}());