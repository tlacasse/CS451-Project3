(function () {
    'use strict';

    angular
        .module('tttModule')

        .filter('replaceNewLines', function () {
            return function (value) {
                if (value == null) {
                    return "";
                }
                return value.toString().replace('\\r\\n','\n');
            };
        })

        .filter('objectToArray', function () {
            return function (value) {
                var array = [];
                for (var k in value) {
                    if (value.hasOwnProperty(k)) {
                        array.push([k,value[k]]);
                    }
                }
                return array;
            };
        })

    ;

}());