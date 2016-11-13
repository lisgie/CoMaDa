'use strict';

/* Directives */
wsnWidgets
    .directive('uiGrid', function () {
        return {
            /*** directive definition ***/
            restrict: 'E',
            templateUrl: '/index/widgets/grid/gridUI.html',
            transclude: true,
            scope: {
            }, // isolated scope

            /*** controller ***/
            controller: function ($scope) {
                $scope.content = {left: {el: null, width: 0, height: 0}, right: {el: null, width: 0, height: 0}}; // content to show, bound by link()
            },
            
            compile: function compile($tElement, tAttrs, transclude) {

            	//console.log(tAttrs);
            	
	            return function link($scope, iElem, iAttrs, controller) {

                /**
                * extract WIDGET:GRID-LEFT / WIDGET:GRID-RIGHT elements from $scope.$data (derived via ui:data)
                */
                function parseContent() {

                	var left = $scope.$data.find("ui\\:grid-left");
                    $scope.left = {el: left.contents().detach(), width: left.attr("width"), height: left.attr("height")};
                    $(iElem).find(".left").append($scope.left.el.children());
                    var right = $scope.$data.find("ui\\:grid-right");
                    $scope.right = {el: right.contents().detach(), width: right.attr("width"), height: right.attr("height")};
                    $(iElem).find(".right").append($scope.right.el.children());
                    
                }
                /*$scope.$watch("$data", function () {
                    parseContent();
                });*/
                parseContent();

	            }
            }
        };
    })
