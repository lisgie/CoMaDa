/**
Vertigo Tip by www.vertigo-project.com
Requires jQuery
*/
this.vtip=function(){
	this.xOffset=-10;
	this.yOffset=10;
	$(".vtip").unbind().hover(function(a){
		//a.preventDefault();
		this.t=$(this).attr("title");
		this.title="";
		this.top=(a.pageY+yOffset);
		this.left=(a.pageX+xOffset);
		$("body").append('<p id="vtip">'+vtip.decode_title(this.t)+"</p>");
		$("p#vtip #vtipArrow").attr("src","/_img/vtip_arrow.png");
		$("p#vtip").css("top",this.top+"px").css("left",this.left+"px").show()/*.fadeIn("slow")*/
	}, function(a){
		//a.preventDefault();
		this.title=this.t;
		$("p#vtip")/*.fadeOut("slow")*/.remove()
	}).mousemove(function(a){
		//a.preventDefault();
		this.top=(a.pageY+yOffset);
		this.left=(a.pageX+xOffset);
		$("p#vtip").css("top",this.top+"px").css("left",this.left+"px")
	});
};
this.vtip.decode_title = function(title) {
	if (title == null) return "";
	return title.replace(/\\n/g, "<br/>").replace(/\\t/g, "&nbsp;&nbsp;&nbsp;");
}
jQuery(document).ready(function(a){vtip()});