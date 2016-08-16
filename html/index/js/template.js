/**
 * !!! attention: variables from "proto" need to be initialized again in constructor, otherwise they are static
 * 
 * 
 * @param jqueryElement
 * @param constructor
 * @param proto
 * @returns {Function}
 */
function create_template(jqueryElement, constructor, proto) {
	
	/* detach DOM element */
	jqueryElement.detach();
	
	/* create template */
	// assemble constructor
	var template = function() {
		this.el = jqueryElement.clone(true); // clone element
		
		if (typeof constructor == "function") { // call given constructor
			constructor.apply(this, arguments);
		}
	};
	template.el = jqueryElement;
	
	// assemble prototype
	template.prototype = {
		el: null, // reference to underlying jQuery element
		tpl: template, // reference to template
	
		appendTo: function(jqueryEl) {	// append to jQuery element
			this.el.appendTo(jqueryEl);
			return this;
		},
			
		remove: function() {	// remove from DOM tree
			this.el.remove();
			return this;
		},
	};
	if (proto != null) {
		for (var e in proto) {
			template.prototype[e] = proto[e];
		}
	}
	
	return template;
}