require(["dojo/date/locale","dijit/MenuBar","dijit/PopupMenuBarItem","dijit/MenuSeparator","dijit/DropDownMenu","dijit/MenuItem","dijit/TitlePane","dijit/form/Slider","dojo/_base/connect"]);

function maddashCreateSpan(className, text){
	var span = document.createElement("span");
	span.className = className;
	span.appendChild(document.createTextNode(text));
	return span;
}


function maddashCreateStatusSpan(status){
	if(status == 0){
		return maddashCreateSpan("maddashStatusOK", "OK");
	}else if(status == 1){
		return maddashCreateSpan("maddashStatusWarning", "WARNING");
	}else if(status == 2){
		return maddashCreateSpan("maddashStatusCritical", "CRITICAL");
	}
	
	return maddashCreateSpan("maddashStatusUnknown", "UNKNOWN");	
}

function _maddashSetParent(parent){
	if(typeof(parent) == "string"){
		return document.getElementById(parent);
	}
	
	return parent;
}

var MadDashTitleSpan = function(parent, link){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	this.link = link;
	
	this.render = function(data){
		this.parent.innerHTML = "";
		if (data == null) {
			console.log("data is null");
			return;
		}
		
		var anchor = document.createElement("a");
		anchor.href = this.link;
		anchor.appendChild(document.createTextNode(data.title));
		anchor.className = "maddashTitleLink";
		var span = document.createElement("span");
		span.appendChild(anchor);
		this.parent.appendChild(span);
	}
}

var MadDashNavMenu = function(parent, link){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	this.link = link;
	
	this._followLink = function(href){
		window.location = href;	
	}	
	
	this.render = function(data){
		this.parent.innerHTML = "";
		if(data == null){
			console.log("data is null");
			return;
		}
		
		var menuBar = new dijit.MenuBar({});
		var dashDropMenu = new dijit.DropDownMenu({});
		for(var i=0; i < data.dashboards.length;i++){
			var tmpLink = this.link + "?dashboard=" +  data.dashboards[i].name;
			dashDropMenu.addChild(new dijit.MenuItem({
				label: data.dashboards[i].name,
				onClick: function(){window.location = instance.link + "?dashboard=" + encodeURIComponent(this.label);}
			}));
		}
		dashDropMenu.addChild(new dijit.MenuSeparator({}));
		
		var gridDropMenu = new dijit.DropDownMenu({});
		for (i = 0; i < data.grids.length; i++) {
			gridDropMenu.addChild(new dijit.MenuItem({
				label: data.grids[i].name,
				onClick: function(){window.location = instance.link + "?grid=" + encodeURIComponent(this.label);}
				
			}));
		}
		dashDropMenu.addChild(new dijit.PopupMenuItem({
				label: "All Grids",
				popup: gridDropMenu
			}));
		menuBar.addChild(new dijit.PopupMenuBarItem({
				label: "Dashboards",
				popup: dashDropMenu
			}));
		menuBar.placeAt(this.parent.id);
		menuBar.startup();
	}
}

var MaDDashCheckTitle = function(parent){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	
	this.render = function(data){
		this.parent.innerHTML = "";
		if(data == null){
			console.log("data is null");
			return;
		}
		this.parent.appendChild(maddashCreateSpan("maddashCheckTitle", data.rowName + " to " + data.colName + " (" + data.checkName + ")"));
	}
}

//Note: not used
var MaDDashCheckNav = function(parent, homeLink, gridLink){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	this.homeLink = homeLink;
	this.gridLink = gridLink;
	
	this.render = function(data){
		this.parent.innerHTML = "";
		if(data == null){
			console.log("data is null");
			return;
		}
		this.parent.appendChild(this._createLink("Home", this.homeLink));
		this.parent.appendChild(document.createTextNode(" > "));
		this.parent.appendChild(this._createLink(data.gridName, this.gridLink));
		this.parent.appendChild(document.createTextNode(" > "));
		this.parent.appendChild(document.createTextNode(data.rowName + " to " + data.colName + " (" + data.checkName + ")"));
	}
	
	this._createLink = function(label, href){
		var anchor = document.createElement("a");
		anchor.href = href;
		anchor.appendChild(document.createTextNode(label));
		return anchor;
	}
}

var MaDDashQuickStatus = function(parent){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	
	this.render = function(data){
		this.parent.innerHTML = "";
		if(data == null){
			console.log("data is null");
			return;
		}
		
		this.parent.appendChild(maddashCreateSpan("maddashFieldLabel", "Status: "));		
		this.parent.appendChild(maddashCreateStatusSpan(data.status));
		this.parent.appendChild(maddashCreateSpan("maddashQuickStatusCol", "Last Checked: "));	
		this.parent.appendChild(maddashCreateSpan("maddashFieldValue", this._formatTime(data.prevCheckTime)));
		this.parent.appendChild(maddashCreateSpan("maddashQuickStatusCol", "Next Check: "));
		this.parent.appendChild(maddashCreateSpan("maddashFieldValue", this._formatTime(data.nextCheckTime)));
	}
	
	this._formatTime = function(timestamp){
		if(timestamp == undefined || timestamp == null){
			return "N/A";
		}
		var date = new Date(timestamp * 1000);
		var fmt="MMMM dd, yyy HH:mm:ss a z";
		
		return dojo.date.locale.format( date, {selector:"date", datePattern:fmt } );;
	}
}

var MaDDashCheckSummary = function(parent){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	
	this.render = function(data){
		this.parent.innerHTML = "";
		if (data == null) {
			console.log("data is null");
			return;
		}
		
		this.parent.appendChild(maddashCreateSpan("maddashFieldLabel", "Current Status: "));		
		this.parent.appendChild(maddashCreateStatusSpan(data.status));
		this.parent.appendChild(document.createElement("br"));
		this.parent.appendChild(maddashCreateSpan("maddashFieldLabel", "Result of last check: "));		
		this.parent.appendChild(maddashCreateStatusSpan(data.returnCode));
		if(data.returnCode != data.status){
			this.parent.appendChild(document.createTextNode("(seen " + 
				data.resultCount + "/" + data.retryAttempts + 
				" times before state change)"));
		}
		this.parent.appendChild(document.createElement("br"));
		this.parent.appendChild(maddashCreateSpan("maddashFieldLabel", "Message from last check: "));
		this.parent.appendChild(maddashCreateSpan("maddashFieldValue", data.message));
		this.parent.appendChild(document.createElement("br"));
	}
}

var MaDDashCheckStatistics = function(parent){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	this.historyItem = 0;
	
	this.setHistoryItem = function(i){
		this.historyItem = i;
	}
	
	this.render = function(data){
		this.parent.innerHTML = "";
		if (data == null) {
			console.log("data is null");
			return;
		}
		
		if(data.history == undefined || data.history == null || data.history.length == 0 ||
			data.history[this.historyItem] == null || data.history[this.historyItem].returnParams == undefined || 
			data.history[this.historyItem].returnParams == null){
			this.parent.appendChild(maddashCreateSpan("maddashFieldLabel", "No statistics to report"));
			return;
		}
		JSON.stringify(data.history[this.historyItem].returnParams, this._formatStats);
	}
	
	this._formatStats = function(key, value){
		if (typeof(value) == "string") {
			instance.parent.appendChild(maddashCreateSpan("maddashFieldLabel", key + ": "));
			instance.parent.appendChild(maddashCreateSpan("maddashFieldValue", value));
			instance.parent.appendChild(document.createElement("br"));
		}
		return value;
	}
}

var MaDDashGraphPane = function(parent){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	
	this.render = function(data){
		this.parent.innerHTML = "";
		if (data == null) {
			console.log("data is null");
			return;
		}
		
		if(data.history == undefined || data.history == null || data.history.length == 0 ||
			data.history[0] == null || data.history[0].returnParams == undefined || 
			data.history[0].returnParams == null || data.history[0].returnParams.graphUrl == undefined ||
			data.history[0].returnParams.graphUrl == null){
			this.parent.appendChild(maddashCreateSpan("maddashFieldLabel", "No graph available"));
			return;
		}
		
		var iframe = document.createElement("iframe");
		iframe.className = "maddashGraphFrame";
		iframe.src = data.history[0].returnParams.graphUrl;
		this.parent.appendChild(iframe);
	}
}

var MaDDashDashboardPane = function(parent, type, name){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	this.type = type;
	this.name = name;
	
	this.render = function(data){

		this.parent.innerHTML = "";
		if (data == null) {
			console.log("data is null");
			return;
		}
		
		//set defaults
		if(this.type == null){
			this.type = "dashboard";
		}
		if(this.name == null && this.type == "dashboard"){
			this.name = data.defaultDashboard;
		}
		if(this.name == null){
			console.log("Unable to determine dashboard to render");
			return;	
		}
		
		//get the list of grids that need to be drawn
		var gridNameList = new Array();
		if(this.type == "dashboard"){
			var dashFound = false;
			for(var i = 0;i < data.dashboards.length && !dashFound;i++){
				if(data.dashboards[i].name == this.name){
					for(var j=0;j<data.dashboards[i].grids.length;j++){
						gridNameList[data.dashboards[i].grids[j].name] = data.dashboards[i].grids[j];
					}
					dashFound = true;
				}
			}
			if(!dashFound){
				console.log("Dashboard " + this.name + " not found");
			}
		}else if(this.type == "grid"){
			gridNameList[this.name] = 1;
		}else{
			console.log("Unable to render dashboard. Invalid type " + this.type);
			return;
		}
		
		//start loading grids
		for(var i=0;i<data.grids.length;i++){
			if (gridNameList[data.grids[i].name] != undefined) {
				var div = document.createElement("div");
				div.id = "maddashgrid_top" + i;
				div.className = "maddashgridTopContainer";
				var titleDiv = document.createElement("div");
				titleDiv.id = "maddashgrid_title" + i;
				titleDiv.className = "maddashgridTitleDiv";
				titleDiv.appendChild(maddashCreateSpan("maddashgridTitle", data.grids[i].name));
				div.appendChild(titleDiv);
				var gridDiv = document.createElement("div");
				gridDiv.id = "maddashgrid_grid" + i;
				gridDiv.className = "maddashgridPane";;
				div.appendChild(gridDiv);
				this.parent.appendChild(div);
				
				var ds = new MaDDashDataSource(data.grids[i].uri);
				var mdGrid = new MaDDashGrid(gridDiv.id);
				ds.connect(mdGrid);
				if(gridNameList[data.grids[i].name].cellWidth != undefined){
					mdGrid.setCellWidth(gridNameList[data.grids[i].name].cellWidth);
				}
				if(gridNameList[data.grids[i].name].cellHeight != undefined){
					mdGrid.setCellHeight(gridNameList[data.grids[i].name].cellHeight);
				}
				
				ds.render();
			}
		}
	}
}

var MaDDashCheckDetails = function(parent){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	
	this.render = function(data){
		this.parent.innerHTML = "";
		var table = document.createElement("table");
		table.className = "maddashCheckDetailsTable";
		
		table.appendChild(this._createTextRow("Check Type", data.type));
		table.appendChild(this._createTextRow("Description", data.description));
		table.appendChild(this._createStatsRow("Check Parameters", data.params));
		table.appendChild(this._createTextRow("Check Interval", "This check is run every " + data.checkInterval + " seconds when there is no change in the result from the previous run"));
		table.appendChild(this._createTextRow("Retry Attempts", "A result must be seen " + data.retryAttempts + " times before changing the status."));
		table.appendChild(this._createTextRow("Retry Interval", "When a change is observed the check will be run every " + data.retryInterval + " seconds"));
		
		this.parent.appendChild(table);
	}
	
	this._convertTime = function(timestamp){
		var date = new Date(timestamp * 1000);
		var dateString = (date.getMonth()+1) + "/" + date.getDate() + "/" + 
		date.getFullYear() + " " + date.getHours() + ":" + date.getMinutes() + 
		":" + date.getSeconds() + " " + date.getTimezoneOffset();
		
		return dateString;
	}
	
	this._createTextRow = function(label, value){;
		var tr = document.createElement("tr");
		var td1 = document.createElement("td");
		td1.className = "maddashFieldLabel";
		td1Text = document.createTextNode(label);
		td1.appendChild(td1Text);
		var td2 = document.createElement("td");
		td2.className = "maddashFieldValue";
		td2Text = document.createTextNode(value);
		td2.appendChild(td2Text);
		tr.appendChild(td1);
		tr.appendChild(td2);
		return tr;
	}
	
	this._createStatsRow = function(label, stats){
		var tr = document.createElement("tr");
		var td1 = document.createElement("td");
		td1.className = "maddashFieldLabel";
		td1Text = document.createTextNode(label);
		td1.appendChild(td1Text);
		var td2 = document.createElement("td");
		JSON.stringify(stats, function(key, value){
			var statsTable = document.createElement("table");
			if (typeof(value) == "string") {
				statsTable.appendChild(instance._createTextRow(key,value));
			}
			td2.appendChild(statsTable);
			return value;
		});
		tr.appendChild(td1);
		tr.appendChild(td2);
		return tr;
	}
}

var MadDashHistory = function(parent){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	
	this.render = function(data, uri){
		this.parent.innerHTML = "";
		if (data == null) {
			console.log("data is null");
			return;
		}
		
		var topSliderDiv = document.createElement("div");
		topSliderDiv.className = "maddashHistSlider";	
		var topPageNumSpan = document.createElement("span");
		topPageNumSpan.className = "maddashFieldValue";
		topPageNumSpan.appendChild(document.createTextNode("1"));
		var botSliderDiv = document.createElement("div");
		botSliderDiv.className = "maddashHistSlider";
		var botPageNumSpan = document.createElement("span");
		botPageNumSpan.className = "maddashFieldValue";
		botPageNumSpan.appendChild(document.createTextNode("1"));
		var historyListDiv = document.createElement("div");
		var historyListItems = new MadDashCheckList(historyListDiv);
		historyListItems.render(data);
		this.parent.appendChild(topSliderDiv);
		this.parent.appendChild(this._createPageNumDiv(topPageNumSpan));
		this.parent.appendChild(historyListDiv);
		this.parent.appendChild(botSliderDiv);
		this.parent.appendChild(this._createPageNumDiv(botPageNumSpan));
		var ds = new MaDDashDataSource(this.uri); 
		ds.connect(historyListItems);
		
		//create sliders
		if (data.historyPageCount > 1) {
			var topSlider = new dijit.form.HorizontalSlider({
				name: "topSlider",
				value: 1,
				minimum: 1,
				maximum: data.historyPageCount,
				discreteValues: data.historyPageCount,
				intermediateChanges: false,
			
			}, topSliderDiv);
			var botSlider = new dijit.form.HorizontalSlider({
				name: "botSlider",
				value: 1,
				minimum: 1,
				maximum: data.historyPageCount,
				discreteValues: data.historyPageCount,
				intermediateChanges: false,
			
			}, botSliderDiv);
			dojo.connect(topSlider, "onChange", function(value){
				topPageNumSpan.innerHTML = "";
				topPageNumSpan.appendChild(document.createTextNode(value));
				botPageNumSpan.innerHTML = "";
				botPageNumSpan.appendChild(document.createTextNode(value));
				botSlider.setValue(value);
				ds.setURL(uri + "?page=" + (value - 1))
				ds.render();
			});
			dojo.connect(botSlider, "onChange", function(value){
				//this will fire onChange of topSlider
				topSlider.setValue(value);
			});
		}
		
	}
	
	this._createPageNumDiv = function(numSpan){
		var div = document.createElement("div");
		div.className = "maddashPageNum";
		var span = document.createElement("span");
		span.className = "maddashFieldLabel";
		span.appendChild(document.createTextNode("Page: "));
		div.appendChild(span);
		div.appendChild(numSpan);
		return div;
	}
}

var MadDashCheckList = function(parent){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	
	this.render = function(data){
		this.parent.innerHTML = "";
		if (data == null) {
			console.log("data is null");
			return;
		}
		
		for(var i=0;i<data.history.length;i++){
			var historyItem = document.createElement("div");
			historyItem.className = "historyItem";
			historyItem.appendChild(maddashCreateSpan("maddashFieldLabel", "Time: "));
			historyItem.appendChild(maddashCreateSpan("maddashFieldValue", this._formatTime(data.history[i].time)));
			historyItem.appendChild(document.createElement("br"));
			historyItem.appendChild(maddashCreateSpan("maddashFieldLabel", "Status: "));		
			historyItem.appendChild(maddashCreateStatusSpan(data.history[i].status));
			historyItem.appendChild(document.createElement("br"));
			historyItem.appendChild(maddashCreateSpan("maddashFieldLabel", "Check result: "));		
			historyItem.appendChild(maddashCreateStatusSpan(data.history[i].returnCode));
			if(data.history[i].returnCode != data.history[i].status){
				historyItem.appendChild(document.createTextNode("(" + data.history[i].count + 
					"/" + data.retryAttempts + " times before state change)"));
			}
			historyItem.appendChild(document.createElement("br"));
			historyItem.appendChild(maddashCreateSpan("maddashFieldLabel", "Message: "));
			historyItem.appendChild(maddashCreateSpan("maddashFieldValue", data.history[i].message));
			historyItem.appendChild(document.createElement("br"));
			var statsDiv = document.createElement("div");
			var titlePane = new dijit.TitlePane({title: "Statistics", open: false, content: statsDiv});
			historyItem.appendChild(titlePane.domNode);
			var stats = new MaDDashCheckStatistics(statsDiv);
			stats.setHistoryItem(i);
			stats.render(data);
			this.parent.appendChild(historyItem);
		}
	}
	
	this._formatTime = function(timestamp){
		if(timestamp == undefined || timestamp == null){
			return "N/A";
		}
		var date = new Date(timestamp * 1000);
		var fmt="MMMM dd, yyy HH:mm:ss a z";
		
		return dojo.date.locale.format( date, {selector:"date", datePattern:fmt } );;
	}
}



