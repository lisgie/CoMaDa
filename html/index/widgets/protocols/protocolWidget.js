'use strict';

/* Directives */
wsnWidgets
    .directive('widgetProtocols', function () {
        return {
            /*** directive definition ***/
            restrict: 'E',
            templateUrl: '/index/widgets/protocols/protocolWidget.html',
            transclude: true,
            replace: true,
            scope: {}, // isolated scope

            /*** controller ***/
            controller: function ($scope) {
            },

            /*** link function ***/
            link: function ($scope, iElem, iAttrs, controller) {
            	var protocolsLogUpdateInterval=2000;
            	function update_log() {
            		$.ajax({
            			"url": "/index/protocolsupdate",
            			"data": {},
            			"dataType": "json",
            			success: function(data){
            				if (data!=null && data.text!=null) {
            					var pcntg = get_scroll_pcntg($(iElem).find(".exec_content").get(0));
            					var scroll = pcntg!=null && pcntg<1 ? $(iElem).find(".exec_content").scrollTop() : null;

            					$(iElem).find(".exec_content").val(data.text);
            					$(iElem).find(".exec_content").scrollTop(scroll!=null ? scroll : $(iElem).find(".exec_content").get(0).scrollHeight);
            				}
            				
            				setTimeout(update_log, protocolsLogUpdateInterval);
            			},
            			error: function(){},
            		});
            	}
        		update_log();
            }
        };
    })
