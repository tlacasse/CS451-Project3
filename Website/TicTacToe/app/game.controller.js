(function () {
    'use strict';

    angular
        .module('tttModule')

        .controller('gameController', ['$http', '$filter', '$timeout'
                , function ($http, $filter, $timeout) {

            var vm = this;

            var FIRST_PLAYER = 5;
            var START_GAME = 6;
            var CONNECTED = 7;

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

            function showError(data) {
                vm.message.show = true;
                vm.message.detail = $filter('objectToArray')(angular.fromJson(data).data);
            }

            function addLog(str) {
                vm.log = str + '<br>' + vm.log;
                document.getElementById('log').innerHTML = vm.log;
            }

            function getStatus() {
                $http.get('/api/status'
                ).then(function (data) {

                }, function (data) {
                    showError(data);
                    if (!vm.gameDone) {
                        $timeout(getStatus, 1000);
                    }
                });
            }

            vm.message = {
                show: false,
                detail: ''
            }

            vm.size = 13;
            vm.array = getArray(vm.size);

            vm.percentString = String(100 / vm.size) + '%';

            vm.inGame = false;
            vm.firstPlayerGo = false;
            vm.gameDone = false;

            vm.state = getArray2d(vm.size, 0);
            vm.class = getArray2d(vm.size, "");

            vm.log = "";

            vm.isTurn = false;

            vm.connect = function () {
                $http.get('/api/connect'
                ).then(function (data) {
                    addLog('Connected To Game.');
                    var mode = parseInt(angular.fromJson(data).data);
                    switch (mode) {
                        case FIRST_PLAYER:
                            vm.firstPlayerGo = true;
                            addLog('You are first player, continue when everyone is ready.');
                            break;
                        case CONNECTED:
                            vm.inGame = true;
                            break;
                    }
                }, function (data) {
                    showError(data);
                });
            }

            vm.startGame = function () {
                $http.get('/api/start'
                ).then(function (data) {
                    vm.inGame = true;
                    vm.firstPlayerGo = false;
                }, function (data) {
                    showError(data);
                });
            }

            vm.postMove = function (xx, yy) {
                if (vm.isTurn) {
                    $http.post('/api/move', { x: xx, y: yy }
                    ).then(function (data) {
                        addLog('Sent Move (' + String(xx) + ',' + String(yy) + '.');
                    }, function (data) {
                        showError(data);
                    });
                } else {
                    addLog('It is not your turn.');
                }
            }


        }]);
    
}());