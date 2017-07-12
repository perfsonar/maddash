/**
 * File: maddash-webui.js
 * Description: This file contains classes that represent widgets used in the web interface
 * of the MaDDash Web-UI. Most of the widgets require the dojo toolkit. The dojo
 * requirement and layout specific nature of these classes distinguishes them from
 * those found in maddash.js file.
 *
 * Authors: Andy Lake <andy@es.net>
 */
require(["dijit/MenuBarItem", "dojox/timing", "dojo/date/locale", "dijit/CheckedMenuItem", "dijit/MenuBar","dijit/PopupMenuBarItem","dijit/MenuSeparator","dijit/DropDownMenu","dijit/MenuItem","dijit/TitlePane","dijit/form/Slider","dojo/_base/connect"]);

/**
 * Function: maddashSetCookie
 * Description: Utility function for setting a cookie
 *   Parameters:
 *       name: name of cookie to set
 *       value: value to set
 *       exdays: days until expiration
 */
function maddashSetCookie(name, value, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+ d.toUTCString();
    document.cookie = name + "=" + value + "; " + expires;
}

/**
 * Function: maddashDeleteCookie
 * Description: Utility function for deleting a cookie
 *   Parameters:
 *       name: name of cookie to delete
 */
function maddashDeleteCookie(name) {
    document.cookie = name + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC";
}

/**
 * Function: maddashGetCookie
 * Description: Utility function for getting a cookie
 *   Parameters:
 *       name: name of cookie to get
 */
function maddashGetCookie(name) {
    var cookie = document.cookie.split(';');
    for(var i = 0; i <cookie.length; i++) {
        var c = cookie[i];
        while (c.charAt(0)==' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name + "=") == 0) {
            return c.substring(name.length + 1,c.length);
        }
    }
    return "";
}

/**
 * Function: maddashCreateSpan
 * Description: Utility function for creating a span with text
 *   Parameters:
 *       className: The name of the css class to assign the span
 *       text: The text to include in the span
 *   Returns: Span element with given class and containing given text
 */
function maddashCreateSpan(className, text){
	var span = document.createElement("span");
	span.className = className;
	span.appendChild(document.createTextNode(text));
	return span;
}

/**
 * Function: maddashCreateStatusSpan
 * Description: Utility function for creating a span indicating the check status.
 *   Parameters:
 *      status: integer representation of status
 *   Returns: A span with string representation of status 
 *       belonging to preset CSS class based on status
 */
function maddashCreateStatusSpan(status, config){
    var span = null;
	if(status == 0){
		span = maddashCreateSpan("maddashStatusSummary", "OK");
	}else if(status == 1){
		span = maddashCreateSpan("maddashStatusSummary", "WARNING");
	}else if(status == 2){
		span = maddashCreateSpan("maddashStatusSummary", "CRITICAL");
	}else if(status == 3){
		span = maddashCreateSpan("maddashStatusSummary", "UNKNOWN");
	}else if(status == 4){
		span = maddashCreateSpan("maddashStatusSummary", "NOT RUN");
	}else if(status == 5){
		span = maddashCreateSpan("maddashStatusSummary", "MAINTENANCE");
	}else {
	    span = maddashCreateSpan("maddashStatusSummary", "CUSTOM");
	}
	
	var savedColorProfile = maddashGetCookie("color");
	var savedColorsFound = 0;
    if(savedColorProfile && config && config.alternateColors){
        for(var i = 0; i < config.alternateColors.length; i++){
            if(config.alternateColors[i].name == savedColorProfile){
                span.style.color = config.alternateColors[i].colors[status];
                savedColorsFound = 1;
                break;
            }
        }
    }
    
    if(!savedColorsFound){
        if(config != undefined && config.colors != undefined){
            span.style.color = config.colors[status];
        }else{
            span.style.color = (new MaDDashGrid()).getColorScale()[status]
        }
    }	
    
	return span;	
}

/**
 * Function: maddashCreateDiv
 * Description: Utility function for creating a div with text
 *   Parameters:
 *       className: The name of the css class to assign the div
 *       text: The text to include in the div
 *   Returns: Div element with given class and containing given text
 */
function maddashCreateDiv(className, text){
	var div = document.createElement("div");
	div.className = className;
	div.appendChild(document.createTextNode(text));
	return div;
}

/**
 * Function: updateTimezoneSpan
 * Description: Set text in given span to browser timezone
 *   Parameters:
 *      spanId: Id of span to update
 *   Returns: nothing
 */
function updateTimezoneSpan(spanId){
    var tz = dojo.date.getTimezoneName(new Date())
    var span = document.getElementById(spanId);
    span.appendChild(document.createTextNode(tz));
}

/**
 * Function: _maddashSetParent
 * Description: Utility function for setting the parent element of a widget
 *   Parameters:
 *      parent: a string or object representing a container element
 *   Returns: The parent element as an object
 */
function _maddashSetParent(parent){
	if(typeof(parent) == "string"){
		return document.getElementById(parent);
	}
	
	return parent;
}


/**
 * Class: MadDashConfig
 * Description: Holds a JSON configuration
 *
 */
var MadDashConfig = function(){
	var instance = this;
	this.data = null;
	
	this.render = function(data){
		this.data = data;
	}
}

/**
 * Class: MadDashTitleSpan
 * Description: Widget that displays the title of the site
 *  Parameters:
 *      parent: a string or object representing a container element
 *      link: the url to the homepage of the dashboard
 *
 */
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

/**
 * Class: MadDashNavMenu
 * Description: Widget that generates a pull-down menu to naviagte through dashboards and grids
 *  Parameters:
 *      parent: a string or object representing a container element
 *      link: name of script that will load dashboards. Will be appended 
 *            with "dashboard=" and "grid=" get parameters
 *      gridSource: MaDDashDataSource that points to grids list URL(e.g. /maddash/grids)
 *
 */
var MadDashNavMenu = function(parent, link, config, userConfig, gridSource, refreshSource){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	this.link = link;
	this.gridSource = gridSource;
	this.refreshSource = refreshSource;
	this.refreshBoxes = [];
	this.refreshTime = 0;
	this.refreshTimer = null;
	this.colorBoxes = [];
	
	this._followLink = function(href){
		window.location = href;	
	}	
	
	this.setPageRefresh = function(refreshTime){
	    this.refreshTime = refreshTime;
	    
	    //uncheck boxes
	    for(var i = 0; i < this.refreshBoxes.length; i++){
	        if(this.refreshTime != this.refreshBoxes[i].value){
	            this.refreshBoxes[i].set("checked", false);
	        }
	    }
	    
	    //stop old timer
	    if(this.refreshTimer != null){
	        this.refreshTimer.stop();
	    }
	    
	    //create new timer
	    if(this.refreshTime > 0){
	        this.refreshTimer = new dojox.timing.Timer(this.refreshTime * 1000);
	        dojo.connect(this.refreshTimer, "onTick", this.refreshSource, 'render');
	        this.refreshTimer.start();
	    }
	}
	
	this.setColorScale = function(scale){
	    //uncheck boxes
	    for(var i = 0; i < this.colorBoxes.length; i++){
	        if(!scale || scale.name != this.colorBoxes[i].label){
	            this.colorBoxes[i].set("checked", false);
	        }
	    }
	    
	    if(scale){
            userConfig.colors = scale.colors;
            maddashSetCookie("color", scale.name, 3650);
        }else{
            userConfig.colors = undefined;
            maddashDeleteCookie("color");
            this.colorBoxes[0].set("checked", true);
        }
	    this.refreshSource.render();
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
		
		var reportDropMenu = new dijit.DropDownMenu({});
		for(var i=0; i < data.dashboards.length;i++){
			reportDropMenu.addChild(new dijit.MenuItem({
				label: data.dashboards[i].name,
				onClick: function(){window.location = "report.cgi?dashboard=" + encodeURIComponent(this.label);}
			}));
		}
		reportDropMenu.addChild(new dijit.MenuSeparator({}));
		
		var settingsDropMenu = new dijit.DropDownMenu({});
		var autoRefreshDropMenu = new dijit.DropDownMenu({});
		settingsDropMenu.addChild(new dijit.PopupMenuItem({
				label: "Auto Refresh",
				popup: autoRefreshDropMenu
			}));
        var refreshTimes= [60, 300, 600, 1800, 3600];
        for(var i = 0; i < refreshTimes.length; i++){
            var timeLabel = refreshTimes[i]/60 + " minute" +(refreshTimes[i] > 60 ? "s" : "");
            var checkMenuItem = new dijit.CheckedMenuItem({
                label: timeLabel, 
                value: refreshTimes[i],
                checked:false,
                onChange: function(checked){
                    instance.setPageRefresh(checked ? this.value : 0);
                }});
            this.refreshBoxes.push(checkMenuItem);
            autoRefreshDropMenu.addChild(checkMenuItem);
        }
        
        //set colors
        if(config != undefined && config.data != undefined && config.data.alternateColors){
            var colorsDropMenu = new dijit.DropDownMenu({});
            settingsDropMenu.addChild(new dijit.PopupMenuItem({
                    label: "Colors",
                    popup: colorsDropMenu
                }));
                
            //add user options
            var savedColorProfile = maddashGetCookie("color");
            var defaultCheckMenuItem = new dijit.CheckedMenuItem({
                label: "Default", 
                value: undefined,
                checked: (savedColorProfile ? false : true),
                onChange: function(checked){
                    instance.setColorScale(undefined);
            }});
            this.colorBoxes.push(defaultCheckMenuItem);
            colorsDropMenu.addChild(defaultCheckMenuItem);
            for(var i = 0; i < config.data.alternateColors.length; i++){
                var checkMenuItem = new dijit.CheckedMenuItem({
                    label: config.data.alternateColors[i].name, 
                    value: config.data.alternateColors[i],
                    checked: savedColorProfile == config.data.alternateColors[i].name,
                    onChange: function(checked){
                        instance.setColorScale(checked ? this.value : undefined);
                    }});
                this.colorBoxes.push(checkMenuItem);
                colorsDropMenu.addChild(checkMenuItem);
            }
        }
        
        if(config != undefined && config.data != undefined && config.data.enableAdminUI){
            settingsDropMenu.addChild(new dijit.MenuSeparator({}));
            settingsDropMenu.addChild(new dijit.MenuItem({
                    label: "Server Settings...",
                    onClick: function(){window.location = "/maddash-webui/admin";}
                }));
        }
        
			
		var gridDropMenu = new dijit.DropDownMenu({});
		var mdGridDropMenu = new MadDashGridDropMenu(gridDropMenu, link);
		this.gridSource.connect(mdGridDropMenu); 
		
		var gridReportDropMenu = new dijit.DropDownMenu({});
		var mdReportGridDropMenu = new MadDashGridDropMenu(gridReportDropMenu, "report.cgi");
		this.gridSource.connect(mdReportGridDropMenu); 
		
		this.gridSource.render();
		
		dashDropMenu.addChild(new dijit.PopupMenuItem({
				label: "All Grids",
				popup: gridDropMenu
			}));
		menuBar.addChild(new dijit.PopupMenuBarItem({
				label: "&#9776; Dashboards",
				popup: dashDropMenu
			}));
		reportDropMenu.addChild(new dijit.PopupMenuItem({
				label: "All Grids",
				popup: gridReportDropMenu
			}));
		menuBar.addChild(new dijit.PopupMenuBarItem({
				label: "&#119567; Reports",
				popup: reportDropMenu
			}));
		if(config != undefined && config.data != undefined && config.data.addNodeURL != undefined){
            menuBar.addChild(new dijit.MenuBarItem({
                    label: "&#9998; Add Your Node",
                    onClick: function(){window.location = config.data.addNodeURL;}
                }));
        }
		menuBar.addChild(new dijit.PopupMenuBarItem({
				label: "&#9881; Settings",
				popup: settingsDropMenu
			}));
			
		//add external links menu if configured
		if(config != undefined && config.data != undefined && config.data.externalLinksMenu){
		    var extLinksLabel = "External Resources";
		    if(config.data.externalLinksMenu.menuLabel){
		         extLinksLabel = config.data.externalLinksMenu.menuLabel;
		    }
            var extLinksDropMenu = new dijit.DropDownMenu({});
            for(var linki = 0; linki < config.data.externalLinksMenu.menuLinks.length; linki++){
                var currLink = config.data.externalLinksMenu.menuLinks[linki];
                if(!currLink.label){
                    console.log("Configuration error: External link missing label");
                    continue;
                }
                if(!currLink.url){
                    console.log("Configuration error: External link missing URL");
                    continue;
                }
                extLinksDropMenu.addChild(new dijit.MenuItem({
                    label: currLink.label,
                    extUrl: currLink.url, //custom prop
                    onClick: function(){window.open(this.extUrl)}
                }));
            }
            menuBar.addChild(new dijit.PopupMenuBarItem({
				label: "&#8689; " + extLinksLabel,
				popup: extLinksDropMenu
			}));
        }
        
		menuBar.placeAt(this.parent.id);
		menuBar.startup();
	}
}

/**
 * Class: MadDashGridDropMenu
 * Description: Drop-down menu item with all the grids
 *  Parameters:
 *      parent: a string or object representing a container element
 *      link: name of script that will load grids. Will be appended 
 *            "grid=" get parameter
 */
var MadDashGridDropMenu = function(parent, link){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	this.link = link;
	
	this.render = function(data){
		for (var i = 0; i < data.grids.length; i++) {
			this.parent.addChild(new dijit.MenuItem({
				label: data.grids[i].name,
				onClick: function(){window.location = instance.link + "?grid=" + encodeURIComponent(this.label);}	
			}));
		}
		
	}
}

/**
 * Class: MaDDashCheckTitle
 * Description: Widget that displays the title of a individual check
 *  Parameters:
 *      parent: a string or object representing a container element
 */
var MaDDashCheckTitle = function(parent){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	
	this.render = function(data){
		this.parent.innerHTML = "";
		if(data == null){
			console.log("data is null");
			return;
		}
		this.parent.appendChild(maddashCreateDiv("maddashCheckTitle", data.rowName + " to " + data.colName + " (" + data.checkName + ")"));
		this.parent.appendChild(maddashCreateDiv("maddashCheckSubtitle", data.description));
	}
}

/**
 * Class: MaDDashQuickStatus
 * Description: Widget that displays the status, previous, and next check times.
 *  Parameters:
 *      parent: a string or object representing a container element
 */
var MaDDashQuickStatus = function(parent, config){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	this.config = config;
	
	this.render = function(data){
		this.parent.innerHTML = "";
		if(data == null){
			console.log("data is null");
			return;
		}
		
		this.parent.appendChild(maddashCreateSpan("maddashFieldLabel", "Status: "));		
		this.parent.appendChild(maddashCreateStatusSpan(data.status, this.config));
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

/**
 * Class: MaDDashCheckSummary
 * Description: Widget that displays the table with check status and infor about the last result
 *  Parameters:
 *      parent: a string or object representing a container element
 */
var MaDDashCheckSummary = function(parent, config){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	this.config = config;
	this.reportCount = 0;
	
	this.render = function(data){
		this.parent.innerHTML = "";
		if (data == null) {
			console.log("data is null");
			return;
		}
		
		this.parent.appendChild(maddashCreateSpan("maddashFieldLabel", "Current Status: "));		
		this.parent.appendChild(maddashCreateStatusSpan(data.status, this.config));
		this.parent.appendChild(document.createElement("br"));
		this.parent.appendChild(maddashCreateSpan("maddashFieldLabel", "Result of last check: "));		
		this.parent.appendChild(maddashCreateStatusSpan(data.returnCode, this.config));
		if(data.returnCode != data.status){
			this.parent.appendChild(document.createTextNode("(seen " + 
				data.returnCodeCount + "/" + data.retryAttempts + 
				" times before state change)"));
		}
		this.parent.appendChild(document.createElement("br"));
		this.parent.appendChild(maddashCreateSpan("maddashFieldLabel", "Message For Current Status: "));
		this.parent.appendChild(maddashCreateSpan("maddashFieldValue", data.message));
		this.parent.appendChild(document.createElement("br"));
		this.parent.appendChild(maddashCreateSpan("maddashFieldLabel", "Reports: "));
		
		var reportsDiv = document.createElement("div");
		var reportBuilder = new MaDDashReport("maddashSummaryReportsDiv", null);
		reportsDiv.className = "maddashSummaryReportsDiv";
		this.addReport(reportBuilder, reportsDiv, "Grid Reports", data.globalReport);
		this.addReport(reportBuilder, reportsDiv, data.rowName, data.rowReport);
		this.addReport(reportBuilder, reportsDiv, data.colName, data.colReport);
		if(this.reportCount == 0){
		    this.parent.appendChild(maddashCreateSpan("maddashFieldValue", "No reports found for this check"));
		}
		this.parent.appendChild(reportsDiv);
		
		this.parent.appendChild(maddashCreateSpan("maddashFieldLabel", "Events: "));
		var eventsDiv = document.createElement("div");
		eventsDiv.className = "maddashSummaryEventsDiv";
		this.parent.appendChild(eventsDiv);
		
		//get events
		var mdEventsList = new MaDDashEventsSummary(eventsDiv);
		var eventsURI = "/maddash/events?gridName=" + data.gridName + "&rowName=" + data.rowName + 
		    "&columnName=" + data.colName + "&checkName=" + data.checkName;
		var eventsSource = new MaDDashDataSource(eventsURI);
		eventsSource.connect(mdEventsList);
		eventsSource.render();
	}
	
    this.addReport = function(builder, div, label, data){
        if(data != null && data.problems != null && data.problems.length > 0){
            div.appendChild(builder.createReport( label, data));
            this.reportCount++;
        }
    }
}

var MaDDashEventsSummary = function(parent){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	
	this.render = function(data){
        if(data == null || data.events == null){
            console.log("data is null");
			return;
        }else{
            var heading = document.createElement("div");
            heading.className = "maddashEventListRowHeading";
            heading.appendChild(maddashCreateDiv("maddashEventListCol", "Name"));
            heading.appendChild(maddashCreateDiv("maddashEventListCol", "Description"));
            heading.appendChild(maddashCreateDiv("maddashEventListCol", "Start"));
            heading.appendChild(maddashCreateDiv("maddashEventListCol", "End"));
            heading.appendChild(maddashCreateDiv("maddashEventListCol", "Check Down"));
            this.parent.appendChild(heading);
            if(data.events.length == 0){
                this.parent.appendChild(maddashCreateDiv("maddashAdminEventListRow", "No events currently scheduled."));
            }else{
                for(var i = 0; i < data.events.length; i++){
                    var row = document.createElement("div");
                    row.className = "maddashEventListRow";
                    row.appendChild(maddashCreateDiv("maddashEventListCol", data.events[i].name));
                    row.appendChild(maddashCreateDiv("maddashEventListCol", data.events[i].description));
                    row.appendChild(maddashCreateDiv("maddashEventListCol", this._formatTime(data.events[i].startTime)));
                    row.appendChild(maddashCreateDiv("maddashEventListCol", this._formatTime(data.events[i].endTime)));
                    row.appendChild(maddashCreateDiv("maddashEventListCol", (data.events[i].changeStatus ? "Yes" : "No")));
                    this.parent.appendChild(row);
                }
            }
        }
    }
    
    this._formatTime = function(timestamp){
		if(timestamp == undefined || timestamp == null){
			return "N/A";
		}
		var date = new Date(timestamp * 1000);
		var fmt="MMMM dd, yyy HH:mm:ss a z";
		
		return dojo.date.locale.format( date, {selector:"date", datePattern:fmt } );
	}
}

/**
 * Class: MaDDashCheckStatistics
 * Description: Widget that displays statistics returned by previous check
 *  Parameters:
 *      parent: a string or object representing a container element
 */
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

/**
 * Class: MaDDashGraphPane
 * Description: Widget that contains iframe with a graph on an external site
 *  Parameters:
 *      parent: a string or object representing a container element
 */
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

/**
 * Class: MaDDashDashboardPane
 * Description: Widget that contains one or more MaDDashGrid widgets
 *  Parameters:
 *      parent: a string or object representing a container element
 *      type: "grid" or "dashboard". A dashboard is just multiple grids.
 *      name: the name of the dashboard or grid to load
 *      config: data object from MaDDashConfig that has grid style parameters
 *      clickHandler: optional function to be called when cell is clicked. Passed cell object.
 */
var MaDDashDashboardPane = function(parent, type, name, config, userConfig, clickHandler){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	this.type = (type == null ? "dashboard" : type);
	this.name = ((name == null && this.type == "dashboard")? config.defaultDashboard : decodeURIComponent(name));
    this.clickHandler = clickHandler;
    
	this.render = function(data){

		this.parent.innerHTML = "";
		if (data == null) {
			console.log("data is null");
			return;
		}
		
		//figure out if we have any cookie data
		var savedColorProfile = maddashGetCookie("color");
		if(savedColorProfile && config && config.alternateColors){
		    for(var i = 0; i < config.alternateColors.length; i++){
		        if(config.alternateColors[i].name == savedColorProfile){
		            userConfig.colors = config.alternateColors[i].colors;
		            break;
		        }
		    }
		}
		
		//get the list of grids that need to be drawn
		var gridList = new Array();
		if(this.type == "dashboard"){
			var dashFound = false;
			if (this.name) {
				for(var i = 0;i < data.dashboards.length && !dashFound;i++){
					if(data.dashboards[i].name == this.name){
						gridList = data.dashboards[i].grids;
						dashFound = true;
					}
				}
			}
			else {
				this.name = data.dashboards[0].name;
				gridList = data.dashboards[0].grids;
				if (gridList) {
					dashFound = true;
				}
			}
			if(!dashFound){
				if(this.name){
					console.log("Dashboard " + this.name + " not found");
				}
				else {
					console.log("No dashboards not found");
				}
			}
		}else if(this.type == "grid"){
		    var gridFound = false;
			for(var i = 0;i < data.grids.length && !gridFound;i++){
			    if(data.grids[i].name == this.name){
					gridList.push(data.grids[i]);
					gridFound = true;
				}
			}
			if(!gridFound){
				console.log("Grid " + this.name + " not found");
			}
		}else{
			console.log("Unable to render dashboard. Invalid type " + this.type);
			return;
		}
		
		//start loading grids
		if(dashFound){
		    d3.select("#" + this.parent.id).append("div").attr("class", "maddashDashboardName").text(this.name + " Dashboard");
		}
		for(var i=0;i<gridList.length;i++){
            var grid_id = "grid-" + i;
            var legend_id = "legend-" + i;
            var report_id = "report-" + i;
            var container = d3.select("#" + this.parent.id).append("div")
                .attr('class', function(){return 'grid-container'})
            container.append("div").attr("class", "maddashGridName").text(gridList[i].name)
            var legend = container.append("div").attr('class', 'legends').attr("id", legend_id);
            var report = container.append("div").attr("id", report_id);
            var gridDiv = container.append("div").attr("id", grid_id)
            gridDiv.append("img").attr("src", "images/loader.gif").attr("height", "20")
                .attr("width", "20").attr("class", "loader")
                .attr("style", "position:relative;left:49%;top:49%");
            
            var ds = new MaDDashDataSource(gridList[i].uri);
            
            var mdGrid = new MaDDashGrid(grid_id, legend_id, report_id);
            if(userConfig.colors != undefined){
                mdGrid.setColorScale(userConfig.colors);
            }else if(config.colors != undefined){
                mdGrid.setColorScale(config.colors);
            }
            if(this.clickHandler != undefined && this.clickHandler != null){
                mdGrid.setClickHandler(this.clickHandler);
            }
            ds.connect(mdGrid);
            
            //load grid configs
            if(config.grids != undefined && config.grids != null &&  
                config.grids[gridList[i].name] != undefined  && config.grids[gridList[i].name] != null ){
                
                //set cell size
               if(config.grids[gridList[i].name].cellSize != undefined && 
                        config.grids[gridList[i].name].cellSize != null){
                    mdGrid.setCellSize(config.grids[gridList[i].name].cellSize);
                }
                
                //set cell padding
                if(config.grids[gridList[i].name].cellPadding != undefined && 
                        config.grids[gridList[i].name].cellPadding != null){
                    mdGrid.setCellPadding(config.grids[gridList[i].name].cellPadding);
                }
            }
            
            ds.render();
		}
	}
}

/**
 * Class: MaDDashReportPane
 * Description: Widget that contains a report for a grid and optionally a host
 *  Parameters:
 *      parent: a string or object representing a container element
 *      grid: the name of the grid for which to display reports
 *      host: optionally the host for which to display a report
 */
var MaDDashReportPane = function(parent, type, name, host, config){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	this.type = (type == null ? "dashboard" : type);
	this.name = ((name == null && this.type == "dashboard")? config.defaultDashboard : decodeURIComponent(name));
    this.host = decodeURIComponent(host);
    
	this.render = function(data){

		this.parent.innerHTML = "";
		if (data == null) {
			console.log("data is null");
			return;
		}
		
		//get the list of grids that need to be drawn
		var gridList = new Array();
		if(this.type == "dashboard"){
			var dashFound = false;
			if (this.name) {
				for(var i = 0;i < data.dashboards.length && !dashFound;i++){
					if(data.dashboards[i].name == this.name){
						gridList = data.dashboards[i].grids;
						dashFound = true;
					}
				}
			}
			else {
				this.name = data.dashboards[0].name;
				gridList = data.dashboards[0].grids;
				if (gridList) {
					dashFound = true;
				}
			}
			if(!dashFound){
				if(this.name){
					console.log("Dashboard " + this.name + " not found");
				}
				else {
					console.log("No dashboards not found");
				}
			}
		}else if(this.type == "grid"){
		    var gridFound = false;
			for(var i = 0;i < data.grids.length && !gridFound;i++){
			    if(data.grids[i].name == this.name){
					gridList.push(data.grids[i]);
					gridFound = true;
				}
			}
			if(!gridFound){
				console.log("Grid " + this.name + " not found");
			}
		}else{
			console.log("Unable to render dashboard. Invalid type " + this.type);
			return;
		}
		
		//start loading grids
		if(dashFound){
		    d3.select("#" + this.parent.id)
		        .append("div").attr("class", "maddashReportDashboardName")
		        .append("a").attr("href", "index.cgi?dashboard=" + encodeURIComponent(this.name))
		        .text(this.name + " Dashboard");
		}
		for(var i=0;i<gridList.length;i++){
            var report_id = "report-" + i;
            var container = d3.select("#" + this.parent.id).append("div")
                .attr('class', function(){return 'report-container'});
            container.append("div").attr("class", "maddashReportGridName")
                .append("a").attr("href", "index.cgi?grid=" + encodeURIComponent(gridList[i].name))
                .text(gridList[i].name);
            var reportDiv = container.append("div").attr("id", report_id)
            reportDiv.append("img").attr("src", "images/loader.gif").attr("height", "20")
                .attr("width", "20").attr("class", "loader")
                .attr("style", "position:relative;left:49%;top:49%");
            
            var ds = new MaDDashDataSource(gridList[i].uri);
            var mdReport= new MaDDashReport(report_id, host);
            ds.connect(mdReport);
            ds.render();
		}
	}
}

/**
 * Class: MaDDashReport
 * Description: Widget that displays details of a report
 * Parameters:
 *      parentId: id string of the container element
 *      host: optional host limiter that displays just the host we want displayed
 */
var MaDDashReport = function(parent, host){
    var instance = this;
    this.parent = _maddashSetParent(parent);
    this.host = host;

    this.render = function (data){
        this.parent.innerHTML = "";
        if(data.report == null){
            this.parent.appendChild(maddashCreateSpan("maddashReportNoData", "No report data available."));
            return;
        }
        
        var problemFound = false;
        
        if(data.report.global != null && data.report.global.problems != null && data.report.global.problems.length > 0){
            problemFound = true;
            this.parent.appendChild(this.createReport("Global", data.report.global));
        }
        
        
        //create header
        if(data.report.sites != null){
            for(var site in data.report.sites){
                //don't render sites without problems
                if(data.report.sites[site].problems == null || data.report.sites[site].problems.length == 0){
                    continue;
                }else if(host && host != site){
                    continue;
                }
                
                problemFound = true;
                this.parent.appendChild(this.createReport(site, data.report.sites[site]));
            }
        }
        
        if(!problemFound){
            this.parent.appendChild(this.createStatusRow(0));
        }
    }
    
    this.createReport = function (name, report){
        //create container
        var reportDiv = document.createElement("div");
        reportDiv.className = "maddashReportReport";
        
        //create header
        var scopeHeaderDiv = document.createElement("div");
        scopeHeaderDiv.className = "maddashReportScopeTitle";
        scopeHeaderDiv.appendChild(maddashCreateSpan("maddashReportScopeTitleSpan", name));
        reportDiv.appendChild(scopeHeaderDiv);
        
        //add status line
        //reportDiv.appendChild(this.createStatusRow(report.severity));
        
        //add problems
        if(report.problems != null && report.problems.length > 0){
            var probsDiv = document.createElement("div");
            probsDiv.className = "maddashReportProblems";
            for(var i = 0; i < report.problems.length; i++){
                var probDiv = document.createElement("div");
                probDiv.className = "maddashReportProblem";
                probDiv.appendChild(this.createStatusRow(report.problems[i].severity, report.problems[i].name));
                probDiv.appendChild(this.createLabelRow("Category", report.problems[i].category, "maddashReportCategory"));
                if(report.problems[i].solutions != null && report.problems[i].solutions.length > 0){
                    probDiv.appendChild(this.createHeadingRow("maddashReportLabel", "Potential Solutions:"));
                    var solutionDiv = document.createElement("div");
                    solutionDiv.className = "maddashReportSolutions";
                    var solutionList = document.createElement("ul");
                    solutionList.className = "maddashReportSolutionsList";
                    for(var j = 0; j < report.problems[i].solutions.length; j++){
                        solutionList.appendChild(this.createSolution(report.problems[i].solutions[j]));
                    }
                    solutionDiv.appendChild(solutionList);
                    probDiv.appendChild(solutionDiv);
                }
                probsDiv.appendChild(probDiv);
            }
            reportDiv.appendChild(probsDiv);
        }
        
        return reportDiv;
    }
    
    this.createStatusRow = function(status, descr) {
        var div = document.createElement("div");
        div.className = "maddashReportStatusRow";
        if(status == 0){
            div.appendChild(this.createStatusImage("images/success.png"));
            div.appendChild(maddashCreateSpan("maddashReportStatusRowOK", (descr ? descr : "Status is OK")));
        }else if(status == 1){
            div.appendChild(this.createStatusImage("images/warning.png"));
            div.appendChild(maddashCreateSpan("maddashReportStatusRowWarn", (descr ? descr : "Status is WARNING. See report(s) for details.")));
        }else if(status >= 2){
            div.appendChild(this.createStatusImage("images/error.png"));
            div.appendChild(maddashCreateSpan("maddashReportStatusRowCritical", (descr ? descr : "Status is CRITICAL. See report(s) for details.")));
        }
        return div;
    }
    
    this.createStatusImage = function(src) {
        var img = document.createElement("img");
        img.className = "maddashReportStatusRowImg";
        img.src = src;
        return img;
    }
    
    this.createSolution = function(text) {
        if(text == null){
            return;
        }
        var li = document.createElement("li");
        li.className = "maddashReportSolutionsListItem";
        li.appendChild(document.createTextNode(text));
        return li;
    }
    
    this.createHeadingRow = function(textClass, text) {
        if(text == null){
            return;
        }
        var div = document.createElement("div");
        div.className = "maddashReportRow";
        div.appendChild(maddashCreateSpan(textClass, text));
        return div;
    }
    
    this.createLabelRow = function(label, text, textClass) {
        if(text == null){
            return;
        }
        var div = document.createElement("div");
        div.className = "maddashReportRow";
        div.appendChild(maddashCreateSpan("maddashReportLabel", label + ":"));
        div.appendChild(maddashCreateSpan(textClass, text));
        return div;
    }
  }
  
/**
 * Class: MaDDashCheckDetails
 * Description: Widget that contains configuration information about a check
 *  Parameters:
 *      parent: a string or object representing a container element
 */
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

/**
 * Class: MadDashHistory
 * Description: Widget that contains history information of a check
 *  Parameters:
 *      parent: a string or object representing a container element
 */
var MadDashHistory = function(parent, config){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	this.config = config;
	
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
		var historyListItems = new MadDashCheckList(historyListDiv, this.config);
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

/**
 * Class: MadDashCheckList
 * Description: Widget that contains list of checks. For exampl, used by MadDashHistory
 *  to display list of check results over time
 *  Parameters:
 *      parent: a string or object representing a container element
 */
var MadDashCheckList = function(parent, config){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	this.config = config;
	
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
			historyItem.appendChild(maddashCreateStatusSpan(data.history[i].status, this.config));
			historyItem.appendChild(document.createElement("br"));
			historyItem.appendChild(maddashCreateSpan("maddashFieldLabel", "Check result: "));		
			historyItem.appendChild(maddashCreateStatusSpan(data.history[i].returnCode, this.config));
			if(data.history[i].returnCode != data.history[i].status){
				historyItem.appendChild(document.createTextNode("(" + data.history[i].returnCodeCount + 
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
		
		return dojo.date.locale.format( date, {selector:"date", datePattern:fmt } );
	}
}

var MaDDashRefreshLabel = function(parent){
	var instance = this;
	this.parent = _maddashSetParent(parent);
    
	this.render = function(data){
        var date = new Date();
		var fmt="MMMM dd, yyy HH:mm:ss a z";
		var dateString = dojo.date.locale.format( date, {selector:"date", datePattern:fmt } );
		
		this.parent.innerHTML = "";
		this.parent.appendChild(maddashCreateSpan("maddashRefreshLabel", "Last page refresh time: " + dateString));
	}
}



