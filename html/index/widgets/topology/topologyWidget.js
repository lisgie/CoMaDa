'use strict';

/* Directives */
wsnWidgets
    .directive('widgetTopology', function () {
        return {
            /*** directive definition ***/
            restrict: 'E',
            templateUrl: '/index/widgets/topology/topologyWidget.html',
            transclude: true,
            replace: true,
            scope: {}, // isolated scope

            /*** controller ***/
            controller: function ($scope) {
            },

            /*** link function ***/
            link: function ($scope, iElem, iAttrs, controller) {
        		/* show graph */
        		var graph = new topologyTpl(wsnnodes);
        		graph.appendTo($(iElem).find(".chart"));
        		
        		/* show node entries */
        		for (var nodeID in wsnnodes) {
        			var entry = new nodeTpl(wsnnodes[nodeID]);
        			entry.el.bind("mouseenter", {nodeID: nodeID}, function(eve) {
        				graph.highlightLinks(eve.data.nodeID);
        			}).bind("mouseleave", {nodeID: nodeID}, function(eve) {
        				graph.unhighlightLinks(eve.data.nodeID);
        			});
        			entry.appendTo($(iElem).find(".nodes"));
        		}
            }
        };
    })
