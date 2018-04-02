(function () {
    'use strict';

    angular
        .module('tttModule')

        .controller('gameController', ['$http', '$filter'
                , function ($http, $filter) {

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

            vm.message = {
                show: false,
                detail: ''
            }

            vm.size = 13;
            vm.array = getArray(vm.size);

            vm.percentString = String(100 / 13) + '%';

            vm.state = getArray2d(vm.size, 0);
            vm.class = getArray2d(vm.size, "");

            vm.postMove = function (xx, yy) {
                console.log("send");
                $http.post('/api/post/move', { x: xx, y: yy }
                ).then(function (data) {
                    console.log("done!");
                }, function (data) {
                    console.log("fail");
                    vm.message.show = true;
                    vm.message.detail = $filter('objectToArray')(angular.fromJson(data).data);
                });
            }


        }]);
    
}());