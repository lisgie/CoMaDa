/* Show and Hide of Border and Node Configuration*/
$(function(){
	$('#node_button').on('click', function(){
		$('#border_platform').hide();
		$('#node_platform').show();
		$('#platforms').show();
		$('#tunnel').hide();
	});
	$('#border_router_button').on('click', function(){
		$('#node_platform').hide();
		$('#border_platform').show();
		$('#platforms').show();
		$('#tunnel').hide();
	});
});

/* Platform Populators */
//load configs for nodes and border router
function loadConfigurations(){
	$.ajax({
		"url": "/contiki/getnodeconfiguration",
		context: this,
		dataType: "json",
		success: function(response){
			var nodeConfigurations = new Array(response.nodes.length);
			var borderConfigurations = new Array(response.borders.length);
			var platforms = new Array(response.platforms.length);
			for (var i = 0; i < response.nodes.length; i++){
				nodeConfigurations[i] = new Array(2);
				nodeConfigurations[i][0] = response.nodes[i].projectName;
				nodeConfigurations[i][1] = response.nodes[i].workingDr;
				localStorage.setItem(response.nodes[i].workingDr, response.nodes[i].description);
			}
			for (var i = 0; i < response.borders.length; i++){
				borderConfigurations[i] = new Array(2);
				borderConfigurations[i][0] = response.borders[i].projectName;
				borderConfigurations[i][1] = response.borders[i].workingDr;
				localStorage.setItem(response.borders[i].workingDr, response.borders[i].description);
			}
			for (var i = 0; i < response.platforms.length; i++){
				platforms[i] = response.platforms[i].platformName;
			}
			
			localStorage.setItem("others", "Here we have some random text.");
			constructPlatformsConfiguration(platforms);
			constructNodeConfiguration(nodeConfigurations, platforms);
			constructBorderConfigration(borderConfigurations, platforms);
		}
	});
}

//construct the innerHTML for nodes
function constructNodeConfiguration(nodeConfigurations, platforms){
	//Construct the selection view
	var select = '<div class = "selection_projects">Project: <br><select onchange = "updateNodeConfig()" size="';
	select = select.concat(nodeConfigurations.length + 1);
	select = select.concat('" id = "option_nodes">');
	for(var i = 0; i < nodeConfigurations.length; i++){
		select = select.concat('<option value="');
		select = select.concat(nodeConfigurations[i][1]);
		select = select.concat('">');
		select = select.concat(nodeConfigurations[i][0]);
		select = select.concat('</option>');
	}
	
	//Add the other option
	select = select.concat('<option value="">Others</option></select></div>');
	document.getElementById("node_platform_content").innerHTML = select;
}

//construct the innerHTML for border router
function constructBorderConfigration(borderConfigurations, platforms){
	var select = '<div class = "selection_projects">Projects:<br><select onchange = "updateBorderConfig()" size ="';
	select = select.concat(borderConfigurations.length + 1);
	select = select.concat('" id = "option_borders">');
	for(var i = 0; i < borderConfigurations.length; i++){
		select = select.concat('<option value="');
		select = select.concat(borderConfigurations[i][1]);
		select = select.concat('">');
		select = select.concat(borderConfigurations[i][0]);
		select = select.concat('</option><br>');
	}
	
	//Add other option
	select = select.concat('<option value="">Others</option></select></div>');
	document.getElementById("border_platform_content").innerHTML = select;
}

//Construct the innerHTML für the selection of the platform
function constructPlatformsConfiguration(platforms){
	var output = '<form action = "" id = "platform_selection">';
	for (var i = 0; i < platforms.length; i++){
		output = output.concat('<input type = "radio" name = "platform" value = "')
		output = output.concat(platforms[i]);
		output = output.concat('">');
		output = output.concat(platforms[i]);
	}
	output = output.concat('</form>');
	document.getElementById("platforms_content").innerHTML = output;
}

//update the input fields when something is chosen from the selection box of nodes
function updateNodeConfig(){
	var e = document.getElementById('option_nodes');
	var workingDr = e.options[e.selectedIndex].value;
	var information;
	if (workingDr){
		document.getElementById('makeNodeFilePath').value = workingDr;
		document.getElementById('makeNodeFilePath').disabled = true;
		information = localStorage.getItem(workingDr);
	}else{
		document.getElementById('makeNodeFilePath').value = "";
		document.getElementById('makeNodeFilePath').disabled = false;
		information = localStorage.getItem("others");
	}
	document.getElementById('informationNodeConfiguration').innerHTML = information;
}

//update the input fields when something is chosen from the selection box of borders
function updateBorderConfig(){
	var e = document.getElementById('option_borders');
	var workingDr = e.options[e.selectedIndex].value;
	var information;
	if (workingDr){
		document.getElementById('makeBorderFilePath').value = workingDr;
		document.getElementById('makeBorderFilePath').disabled = true;
		information = localStorage.getItem(workingDr);
	}else{
		document.getElementById('makeBorderFilePath').value = "";
		document.getElementById('makeBorderFilePath').disabled = false;
		information = localStorage.getItem("others");
	}
	document.getElementById('informationBorderConfiguration').innerHTML = information;
}

/* Triggers for make commands */
//This function triggers the make of the node.
function makeNode(){
	var workingDr = document.getElementById('makeNodeFilePath').value;
	var platform = $('input[name=platform]:checked', '#platform_selection').val();
	loading();
	$('#makeNodeButton').hide();
	$('#node_configuration_content_and_information').hide();
	$('#resetNodeButton').show();
	$('#node_information').show();
	var informationNode = "Currently the server is running the make file and will show the install button, as soon the compiled software is ready for installation.";
	document.getElementById('node_information').innerHTML = informationNode;
	$.ajax({
		"url": "/contiki/domake",
		data: {"workingDr": workingDr,
			"platform": platform},
		context: this,
		dataType: "json",
		success: function(response){
			displayRAMROM(response);
			shellOutput(response);
			$('#installNodeButton').show();
		}	
	});
}

//This function triggers the make of the node.
function makeBorder(){
	var workingDr = document.getElementById('makeBorderFilePath').value;
	var platform = $('input[name=platform]:checked', '#platform_selection').val();
	loading();
	$('#makeBorderButton').hide();
	$('#border_configuration_content_and_information').hide();
	$('#resetBorderButton').show();
	$('#border_information').show();
	var informationBorder = "Currently the server is running the make file and will show the install button, as soon the compiled software is ready for installation.";
	document.getElementById('border_information').innerHTML = informationBorder;
	$.ajax({
		"url": "/contiki/domake",
		data: {"workingDr": workingDr,
			"platform": platform},
		context: this,
		dataType: "json",
		success: function(response){
			displayRAMROM(response);
			shellOutput(response);
			$('#installBorderButton').show();
		}	
	});
}

/*
 * Install functions for node and border
 * */
//Install function for Nodes, as workingDr is the hidden selection from 
//the configuration pannel chosen.
function installNode(){
	var workingDr = document.getElementById('makeNodeFilePath').value;
	var informationNode = "Currently the software is being installed on the node.";
	document.getElementById('node_information').innerHTML = informationNode;
	var platform = $('input[name=platform]:checked', '#platform_selection').val();
	loading();
	$('#installNodeButton').hide();
	$.ajax({
		"url": "/contiki/install",
		data: {"workingDr": workingDr,
			"platform": platform},
		context: this,
		dataType: "json",
		success: function(response){
			shellOutput(response);
		}	
	});	
}

function installBorder(){
	var workingDr = document.getElementById('makeBorderFilePath').value;
	var informationBorder = "Currently the software is being installed on the border router.";
	var platform = $('input[name=platform]:checked', '#platform_selection').val();
	document.getElementById('border_information').innerHTML = informationBorder;
	$('#installBorderButton').hide();
	$.ajax({
		"url": "/contiki/install",
		data: {"workingDr": workingDr,
			"platform": platform},
		context: this,
		dataType: "json",
		success: function(response){
			shellOutput(response);
		}	
	});	
}

/* Node Information */
//Display RAM and ROM in GUI
function displayRAMROM(usage) {
	var output = '<table width="300">\
		<tr>\
			<th>ROM</th><th>RAM</th>\
		</tr>\
		<tr>\
			<td align="center">';
	output = output.concat(usage.rom);
	output = output.concat(' kb</td>\
			<td align="center">');
	output = output.concat(usage.ram);
	output = output.concat(' kb</td>\
				</tr>\
			</table>');
	document.getElementById("contiki_information_content").innerHTML = output;
	$('#contiki_information').show();
}

/*
 * Reset function for the GUI of node and border
 * */
function resetNode(){
	$('#makeNodeButton').show();
	$('#node_configuration_content_and_information').show();
	$('#shell').hide();
	$('#resetNodeButton').hide();
	$('#contiki_information').hide();
	$('#node_information').hide();
	$('#installNodeButton').hide();
}
function resetBorder(){
	$('#makeBorderButton').show();
	$('#border_configuration_content_and_information').show();;
	$('#shell').hide();
	$('#resetBorderButton').hide();
	$('#contiki_information').hide();
	$('#border_information').hide();
	$('#installBorderButton').hide();
}

/* Shell output */
//Injects the response to the HTML file.
function shellOutput (response){
	var output = '';
	output = output.concat(response.output);
	document.getElementById("shell_content").innerHTML = output;
}

//Show loading in HTML file.
function loading(){
	var loadGIF = '<img class = "loading_gif" src="/contiki/img/loading.GIF" alt="Loading" style="width:64px;height:64px;">';
	document.getElementById("shell_content").innerHTML = loadGIF;
	$('#shell').show();
}

/* Activation for the tunnel */
function tunnel(){
	$('#tunnel').show();
	$('#node_platform').hide();
	$('#border_platform').hide();
	$('#shell').hide();
	$('#platforms').hide();
	$('#contiki_information').hide();
}

/* Tunnel Starter, it triggers startTunnel() */
$(function() {
	$('#startTunnelButton').on('click', function(){
		startTunnel();
    });
	$('#tunnel_password').keypress(function(eve){
		if (eve.keyCode == "13") {
			startTunnel();
		}
	});
});

/* Tunnel activation */
function startTunnel(){
	alert("Tunnel has been started");
	setTimeout(updateTunnel(), 1000);
}

/* Tunnel Update */
function updateTunnel(){
	alert("Tunnel has been updated");
}
