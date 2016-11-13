'use strict';


var wsnWidgets = angular.module('wsnApp.widgets', [])
	/**
	* UI:Data
	* 
	* Binds DOM-Content(-clone) to scope.$data.
	* May be used as <ui:data ng-transclude /> within a directive to access the given DOM via scope.$data.
	* Observes and propagates changes to the DOM.
	*
	* WARNING: problems with old browsers (like Opera, which don't support MutationObserver) known. Multiple/Too less propagations may occur.
	*/
	.directive('uiData', function () {
	    return {
	        /*** directive definition ***/
	        restrict: 'E',
	        replace: false,

	        /*** compile+link function ***/
	        compile: function compile($tElement, tAttrs, transclude) {

	            $tElement.css("display", "none");

	            return function link($scope, $iElem, iAttrs, controller) {

	                /**
	                * make DOM(-clone) available via $scope.$data
	                */
	                $scope.$data = $iElem.clone(true);

	                /**
	                * observe DOM modification to propagate data modification 
	                */
	                function dataChangeEvent() {
	                    $scope.$data = $iElem.clone(true);
	                    try {
	                        $scope.$digest();
	                    } catch (e) {
	                    }
	                }

	                // use MutationObserver to react to DOM-changes
	                var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || null;
	                if (MutationObserver != null) {

	                    var observer = new MutationObserver(function (mutations, observer) {
	                        dataChangeEvent();
	                    });

	                    observer.observe($iElem.get(0), {
	                        childList: true,
	                        subtree: true,
	                        attributes: true,
	                        characterData: true
	                    });

	                } else { // if MutationObserver is not available use DomManipulators

	                    // OPERA hackfix: use addEventListener instead of $.bind() or the event won't get fired..
	                    $iElem.get(0).addEventListener('DOMNodeInserted', function (eve) {
	                        dataChangeEvent();
	                    });
	                    $iElem.get(0).addEventListener('DOMNodeRemoved', function (eve) {
	                        dataChangeEvent();
	                    });
	                    $iElem.get(0).addEventListener('DOMAttrModified', function (eve) {
	                        dataChangeEvent();
	                    });
	                    $iElem.get(0).addEventListener('DOMCharacterDataModified', function (eve) {
	                        dataChangeEvent();
	                    });

	                }
	            }
	        }
	    };
	});;

// Declare app level module which depends on filters, and services
var wsnApp = angular.module('wsnApp', ['wsnApp.widgets'/*'btApp.filters', 'btApp.services', 'btApp.directives'*/]).
	run(function(){
	});