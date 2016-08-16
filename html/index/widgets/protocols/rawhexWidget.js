'use strict';

/* Directives */
wsnWidgets
    .directive('widgetRawhex', function () {
        return {
            /*** directive definition ***/
            restrict: 'E',
            templateUrl: '/index/widgets/protocols/rawhexWidget.html',
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
            			"url": "/index/rawhexupdate",
            			"data": {},
            			"dataType": "json",
            			success: function(data){
            				if (data!=null && data.text!=null) {
            					var pcntg = get_scroll_pcntg($(iElem).find(".content").get(0));
            					var scroll = pcntg!=null && pcntg<1 ? $(iElem).find(".content").scrollTop() : null;

            					$(iElem).find(".content").val(data.text);
            					$(iElem).find(".content").scrollTop(scroll!=null ? scroll : $(iElem).find(".content").get(0).scrollHeight);
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
