'use strict';

/* Directives */
wsnWidgets
    .directive('widgetCosm', function () {
        return {
            /*** directive definition ***/
            restrict: 'E',
            templateUrl: '/cosm/widgets/cosm/cosmWidget.html',
            transclude: true,
            replace: true,
            scope: {}, // isolated scope

            /*** controller ***/
            controller: function ($scope) {
    			$scope.toggle_cur = function() {
    				if ($("#feeds_cur .feeds").css("display") != "none") {
    					$("#feeds_cur .feeds").hide();
    				} else {
    					$("#feeds_cur .feeds").show();
    				}
    			}
    			$scope.toggle_arch = function() {
    				if ($("#feeds_archive .feeds").css("display") != "none") {
    					$("#feeds_archive .feeds").hide();
    				} else {
    					$("#feeds_archive .feeds").show();
    				}
    			}
            },

            /*** link function ***/
            link: function ($scope, iElem, iAttrs, controller) {
            	var wsnnodes={};
            	var node_assignments={};
            	var feed_assignments={};
            	var cosm_feeds={};
            	
            	
            	function create_feed(title) {
            		$(iElem).find(".create_wait").show();
            		$.ajax({
            			"url": "/cosm/createfeed",
            			data: {"title": title},
            			context: this,
            			dataType: "json",
            			success: function(data) {
            				$(iElem).find(".create_wait").hide();
            				if (data!=null && data.success) {
            					var feed = data.feed;
            					var entry = new feedTpl(feed);
            					entry.appendTo($(iElem).find("#feeds_cur .feeds"));
            					window.location.reload();
            				}
            			},
            			error: function() {
            				$(iElem).find(".create_wait").hide();
            			},
            			complete: function() {
            			}
            		});
            	}
            	var assigning=null;
            	function start_assigning(nodeID) {
            		assigning = nodeID;
            		
            		$(iElem).find(".feed .feed_name_highlight").show();
            		
            		$(iElem).find(".feed .title").bind("click", {nodeID: nodeID}, function(eve) {

            			if (confirm("Are you sure you want to assign Node #"+eve.data.nodeID+" to this feed?\nAll non fitting Datastreams will be deleted!")) {
            				do_assign($(iElem).find(this).attr("name"));
            			}

            			$(iElem).find(".feed .feed_name_highlight").hide();
            			eve.preventDefault();
            		})
            	}
            	function do_assign(feedID) {
            		var nodeID = assigning;
            		assigning=null;
            		
            		if (feedTpl.entries[feedID].feed.nodeID != null) return;
            		
            		if (pNodeTpl.entries[nodeID] != null)
            			pNodeTpl.entries[nodeID].remove();
            		
            		var feedEl = $(iElem).find(".feed .title[name=\""+feedID+"\"]").parent().parent(".feed");
            		feedEl.find(".streams").empty();
            		
            		feedEl.find(".wait").show();
            		
            		$.ajax({
            			"url": "/cosm/assign",
            			data: {"nodeid": nodeID, "feedid": feedID},
            			context: this,
            			dataType: "json",
            			success: function(data) {
            				$(iElem).find(".create_wait").hide();
            				if (data!=null && data.success) {
            					var feed = data.feed;
            					
            					feedTpl.entries[feed.id].remove();
            					var entry = new feedTpl(feed,wsnnodes[nodeID]);
            					entry.appendTo($(iElem).find("#feeds_cur .feeds"));
            				} else {
            					window.location.reload();
            				}
            			},
            			error: function() {
            				window.location.reload();
            			},
            			complete: function() {
            			}
            		});
            	}

            	var pNodeTpl = create_template($(iElem).find(".cosm_node"), function(wsnnode) {
            		
            		var _this=this;
            		this.wsnnode=wsnnode;
            		this.tpl.entries[wsnnode.nodeID] = this;
            		
            		
            		var entry = new nodeTpl(wsnnode);
            		entry.el.css("float","");
            		entry.appendTo(this.el.find(".node"));

            		this.el.find("a").bind("click", {nodeID: wsnnode.nodeID}, function(eve) {
            				start_assigning(eve.data.nodeID);
            				eve.preventDefault();
            			});

            		this.el.find(".assign_feed")
            			.bind("mouseenter", {nodeID: wsnnode.nodeID}, function(eve) {
            				if (nodeTpl.entries[eve.data.nodeID]!=null)
            					nodeTpl.entries[eve.data.nodeID].hover();
            			})
            			.bind("mouseleave", {nodeID: wsnnode.nodeID}, function(eve) {
            				if (nodeTpl.entries[eve.data.nodeID]!=null)
            					nodeTpl.entries[eve.data.nodeID].hoverend();
            			});
            	});
            	pNodeTpl.entries = {};
            	var streamTpl = create_template($(iElem).find(".stream"), function(streamID, feedID) {
            		this.streamID = streamID;
            		this.feedID = feedID;

            		this.imgUrl = this.el.find(".img").attr("src");
            		this.imgLinkUrl = this.el.find(".imglink").attr("href");
            		
            		
            		this.updateLinks();
            		
            		this.el.find(".graph-duration").bind("change", {stream: this}, function(eve){
            			if ($(iElem).find(this).val() == "-other-") {
            				eve.data.stream.el.find(".other_duration_container").show();
            			} else {
            				eve.data.stream.el.find(".other_duration_container").hide();
            				eve.data.stream.updateLinks();
            			}
            		});
            		this.el.find("input[name=\"other_duration_btn\"]").bind("click", {stream: this}, function(eve) {
            			eve.data.stream.updateLinks();
            		});
            	}, {
            		
            		updateLinks: function() {
            			var duration = this.el.find(".graph-duration").val();
            			if (duration == "-other-") {
            				duration = this.el.find("input[name=\"other_duration\"]").val().split(" ").join("");
            			}
            			duration = escape(duration);

            			this.el.find(".img")
            				.attr("src", this.imgUrl.replace(/\{FEEDID\}/g,this.feedID).replace(/\{STREAMID\}/g, this.streamID).replace(/\{DURATION\}/g,duration));
            			this.el.find(".imglink")
            				.attr("href", this.imgLinkUrl.replace(/\{FEEDID\}/g,this.feedID).replace(/\{STREAMID\}/g, this.streamID).replace(/\{DURATION\}/g,duration));
            		}
            		
            	});
            	var feedTpl = create_template($(iElem).find(".feed"), function(feed, wsnnode) {
            		
            		this.feed = feed;
            		this.wsnnode = wsnnode;
            		this.tpl.entries[feed.id] = this;
            		
            		
            		this.el.find("a.title")
            			.text(feed.title)
            			.attr("name", feed.id)
            			.attr("href", this.el.find("a.title").attr("href").replace("{ID}", feed.id));
            		
            		this.el.find(".visibility")
            			.text(feed['private'] ? "(Private)" : "(Public)")
            			.attr("title", "click here to set this feed " + (feed['private']?"public":"private"));
            		
            		if (this.wsnnode!=null) {
            			var nodeEntry = new nodeTpl(this.wsnnode);
            			nodeEntry.el.unbind("hover");
            			nodeEntry.appendTo(this.el.find(".node"));
            			
            			for (var i=0; i<feed.streams.length; i++) {
            				var entry = new streamTpl(feed.streams[i], feed.id);
            				
            				entry.el.find(".del-ds-link").bind("click", {"feedID": feed.id, "streamID": feed.streams[i]}, function(eve) {
            					eve.preventDefault();

            					if (confirm("Are you sure you want to delete '"+eve.data.streamID+"' of the '"+eve.data.feedID+"' feed?")) {
            						var feedEl = $(iElem).find(".feed .title[name=\""+eve.data.feedID+"\"]").parent().parent(".feed");
            						feedEl.find(".wait").show();
            						
            						$.ajax({
            							"url": "/cosm/deletestream",
            							data: {"streamid": eve.data.streamID, "feedid": eve.data.feedID},
            							context: this,
            							dataType: "json",
            							success: function(data) {
            								window.location.reload();
            							},
            							error: function() {
            								window.location.reload();
            							},
            							complete: function() {
            							}
            						});
            					}	
            				});
            				
            				entry.appendTo(this.el.find(".streams"));
            				//vtip(entry.el.find(".del-ds-link"));
            			}
            		}
            	});
            	feedTpl.entries = {};
            	
            	$(function() {
            		
            		$.ajax({
            			"url": "/cosm/getvars",
            			data: {},
            			context: this,
            			dataType: "json",
            			success: function(data) {
            				wsnnodes = data.wsnnodes;
            				node_assignments = data.node_assignments;
            				feed_assignments = data.feed_assignments;
            				cosm_feeds = data.cosm_feeds;
                    		
                    		$(iElem).find("#create_button").click(function(eve) {
                    			var title = $(iElem).find("input.new_feed").val();
                    			
                    			create_feed(title);
                    		});
                    		$(iElem).find("#saveinterval").change(function(eve) {
                    			$.ajax({url: "/cosm/setinterval",data:{"value": $(iElem).find("#saveinterval").val()}});
                    		});
                    		
                    		/* show node entries */
                    		for (var nodeID in wsnnodes) {

                    			if (node_assignments[nodeID]!=null) continue;

                    			var entry = new pNodeTpl(wsnnodes[nodeID]);
                    			entry.appendTo($(iElem).find("#nodes"));
                    		}
                    		
                    		/* show feeds */
                    		var activeFeeds=0;
                    		for (var i=0; i<cosm_feeds.length; i++) {
                    			var feed = cosm_feeds[i];
                    			
                    			var entry = new feedTpl(feed, feed_assignments[feed.id]!=null&&wsnnodes[feed_assignments[feed.id]]!=null ? wsnnodes[feed_assignments[feed.id]] : null);
                    			
                    			if (feed.active) {
                    				entry.appendTo($(iElem).find("#feeds_cur .feeds"));
                    				activeFeeds++;
                    			} else {
                    				entry.appendTo($(iElem).find("#feeds_archive .feeds"));
                    			}
                    		}
                    		if (activeFeeds <= 0) {
                    			$(iElem).find("#feeds_cur .feeds").hide();
                    		}
            			}
            		});
            	});
            }
        };
    })
