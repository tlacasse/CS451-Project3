(function () {
    'use strict';

    angular
        .module('tttModule')

        .controller('gameController', ['$http'
                , function ($http) {
            var vm = this;

            function getArray(n) {
                var a = [];
                for (var i = 0; i < n; i++) {
                    a.push(i);
                }
                return a;
            }

            function getArray2d(dim, val) {
                var a = [];
                for(var i = 0; i < dim; i++){
                    var b = [];
                    for(var i = 0; i < dim; i++){
                        b.push(val);
                    }
                    a.push(b);
                }
            }

            vm.size = 13;
            vm.array = getArray(vm.size);

            vm.percentString = String(100 / 13) + '%';

            vm.state = getArray2d(vm.size, 0);
            vm.class = getArray2d(vm.size, "");


        }]);
    
}());