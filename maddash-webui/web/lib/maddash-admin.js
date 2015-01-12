/**
 * File: maddash-webui.js
 * Description: This file contains classes that represent widgets used in the web interface
 * of the MaDDash Web-UI. Most of the widgets require the dojo toolkit. The dojo
 * requirement and layout specific nature of these classes distinguishes them from
 * those found in maddash.js file.
 *
 * Authors: Andy Lake <andy@es.net>
 */
require(["dojo/date/locale", "dijit/Dialog", "dijit/form/FilteringSelect", "dijit/form/CheckBox", "dijit/form/ComboBox", "dojo/store/Memory", "dijit/form/Button", "dijit/form/DateTextBox", "dijit/form/TimeTextBox", "dojo/date", "dojo/_base/json", "dijit/layout/ContentPane", "dijit/form/ValidationTextBox", "dijit/form/TextArea"]);


/**
 * Class: MadDashAdminCheckFilter
 * Description: Widget that contains list of checks. For exampl, used by MadDashHistory
 *  to display list of check results over time
 *  Parameters:
 *      parent: a string or object representing a container element
 */
var MadDashAdminCheckFilters = function(parent){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	this.rows = [];

	this.render = function(data){
		this.parent.innerHTML = "";
		
		this.rows.push(new MadDashAdminCheckFilterGroup(this.parent, "Grid", "grid"));
		this.rows.push(new MadDashAdminCheckFilterGroup(this.parent, "Row", "row"));
		this.rows.push(new MadDashAdminCheckFilterGroup(this.parent, "Column", "column"));
		this.rows.push(new MadDashAdminCheckFilterGroup(this.parent, "Check", "check"));
		for(var i = 0; i < this.rows.length; i++){
		    if(i > 0){
		        var andRow = document.createElement("div");
		        andRow.className = "mdAdminAndLabel";
		        andRow.appendChild(maddashCreateSpan("mdAdminAndLabel", "AND"));
		        andRow.id = "and" + i;
		        this.parent.appendChild(andRow);
		    }
		    this.rows[i].render(null, i);
		    
		}
	}
}

var MadDashAdminCheckFilterGroup = function(parent, name, label){
	var instance = this;
	this.parent = _maddashSetParent(parent);
	this.filters = [];
	this.filterCount = 1;
	this.name = name;
	this.label = label;
	this.row = null;
	
	this.render = function(data, rowNum){
	    
	    
	    //add static elements
	    this.row = document.createElement("div");
	    
	    if((rowNum % 2) == 0){
    	    this.row.className = "maddashRowEven";
    	}else{
    	    this.row.className = "maddashRowOdd";
    	}
	    this.row.id = (this.label + "_filters");
	    var filter = new MadDashAdminCheckFilter(this);
	    filter.render(null, 1);
	    this.filters.push(filter);
	    
	    this.parent.appendChild(this.row);
	}

}
var MadDashAdminCheckFilter = function(parent){
    var instance = this;
	this.parent = _maddashSetParent(parent);
	
	this.render = function(data, n){
		var name = this.parent.name;
		var label = this.parent.label;
	    
	    var filterCondRow = document.createElement("div");
        filterCondRow.appendChild(maddashCreateSpan("mdAdminFilterLabel", name + " name is "));
	    var comboElem = document.createElement("input");
	    comboElem.id = label + "_select" + n;
	    filterCondRow.appendChild(comboElem);
	    var addButtonElem = document.createElement("button");
	    var removeButtonElem = document.createElement("button");
	    addButtonElem.id = label + "_add"+ n;
	    removeButtonElem.id = label + "_remove"+ n;
	    filterCondRow.appendChild(addButtonElem);
	    filterCondRow.appendChild(removeButtonElem);
	    
	    //build combo box
	    var stateStore = new dojo.store.Memory({
            data: [
                {name:"any", id:"any"},
            ]
        });
	    var comboBox = new dijit.form.ComboBox({
            id: label + "_select"+ n,
            name: label,
            value: "any",
            store: stateStore,
            searchAttr: "name"
        }, comboElem);
        
        //add buttons
        var targetGroup = this.parent;
        var addButton = new dijit.form.Button({
            label: "+",
            onClick: function(){
                var filter = new MadDashAdminCheckFilter(targetGroup);
	            targetGroup.filters.push(filter);
	            targetGroup.filterCount++;
	            filter.render(null, targetGroup.filterCount);
            }
        }, addButtonElem);
        var removeButton = new dijit.form.Button({
            label: "-",
            onClick: function(){
                if(targetGroup.filters.length == 1){
                    alert("Must have one filter in list");
                    return;
                }
                targetGroup.row.removeChild(filterCondRow);
                targetGroup.filters.splice(targetGroup.filters.indexOf(filterCondRow), 1)
            }
        }, removeButtonElem);
        
        
        //add checkbox 
        if(label == "row" && targetGroup.filterCount == 1){
            var cb = document.createElement("input");
            cb.id = label + "_cb";
            cb.className = "maddashAdminRowColCheckBox";
            filterCondRow.appendChild(cb);
            var cbLabel = document.createElement("label");
            filterCondRow.appendChild(cbLabel);
            cbLabel.appendChild(document.createTextNode("Filter on row and column simultaneously"));
            var checkBox = new dijit.form.CheckBox({
                name: label + "_cb",
                value: "1",
                checked: false,
                onChange: function(checked){
                     if(checked){
                        document.getElementById("column_filters").style.visibility = "hidden";
                        document.getElementById("column_filters").style.display = "none";
                        document.getElementById("and3").style.visibility = "hidden";
                        document.getElementById("and3").style.display = "none";
                     }else{
                        document.getElementById("column_filters").style.visibility = "visible";
                        document.getElementById("column_filters").style.display = "inherit";
                        document.getElementById("and3").style.visibility = "visible";
                        document.getElementById("and3").style.display = "inherit";
                     }
                }
              }, cb);
        }
        
        this.parent.row.appendChild(filterCondRow);
	}
}

function buildFilters(){
    var filterTypes = [ 'grid', 'row', 'column', 'check'];
    var filters = {};
    for (var i = 0; i < filterTypes.length; i++){
        for(var j = 1; dojo.byId(filterTypes[i] + '_select' + j) != null; j++){
            var filterKey = filterTypes[i] + "Name";
            if(filterTypes[i] == 'row' && dijit.byId("row_cb").checked){
                filterKey = "dimensionName";
            }else if(filterTypes[i] == 'column' && dijit.byId("row_cb").checked){
                continue;
            }
            if(filters[filterKey] == undefined){
                filters[filterKey] = [];
            }
            if(dojo.byId(filterTypes[i] + '_select' + j).value == 'any'){
                filters[filterKey] = '*';
                break;
            }
            filters[filterKey].push(dojo.byId(filterTypes[i] + '_select' + j).value);
        }
    }
    
    return filters;
}

function buildDateTime(dateId, timeId){
    var dateObj = dijit.byId(dateId).value;
    var dateObjNew = new Date(0); //cancel out any time info that sneaks its way into widget
    dateObjNew.setFullYear(dateObj.getFullYear());
    dateObjNew.setMonth(dateObj.getMonth());
    dateObjNew.setDate(dateObj.getDate());
    
    var timeObj = dijit.byId(timeId).value;
    var timeObjNew = new Date(0); //cancel out any date info that sneaks its way into widget
    timeObjNew.setHours(timeObj.getHours());
    timeObjNew.setMinutes(timeObj.getMinutes());
    
    return dateObjNew.getTime()/1000 + timeObjNew.getTime()/1000;
}

var MadDashRescheduler = function(){
    var instance = this;
    
    this.reschedule = function(){
        var filters = buildFilters();
        var nextCheckTime = buildDateTime("reschedDate", "reschedTime");
        
        var json = dojo.toJson({"checkFilters": filters, "nextCheckTime": nextCheckTime});
        
        document.getElementById('reschedLoader').style.display = "inherit";
        document.getElementById('reschedButton').style.display = "none";
        document.getElementById("reschedButton").style.visibility = "hidden";
        dojo.xhrPost({
            url: '/maddash/admin/schedule',
            postData: json,
            timeout: 30000,
            handleAs: 'json',
            headers: {'Content-Type': 'application/json'},
            load: function(data){
                document.getElementById('reschedLoader').style.display = "none";
                document.getElementById('reschedButton').style.display = "inherit";
                document.getElementById("reschedButton").style.visibility = "visible";
                if(data.status == 0){
                    document.getElementById('reschedStatus').className= "success";
                }else{
                    document.getElementById('reschedStatus').className= "error";
                }
                document.getElementById('reschedStatus').innerHTML = data.message;
                document.getElementById('reschedStatus').style.display = "inherit";
            },
            error: function(error){
                document.getElementById('reschedLoader').style.display = "none";
                document.getElementById('reschedButton').style.display = "inherit";
                document.getElementById("reschedButton").style.visibility = "visible";
                document.getElementById('reschedStatus').className= "error";
                document.getElementById('reschedStatus').style.display = "inherit";
                if(error.status == 503){
                    document.getElementById('reschedStatus').innerHTML = "Unable to reach MaDDash server. If the server does not return within a few minutes have the server administrator restart MaDDash and Apache.";
                }else{
                    document.getElementById('reschedStatus').innerHTML = error.message;
                }
            }
        });
    }

}


function formatEventTime(timestamp){
    if(timestamp == undefined || timestamp == null){
        return "N/A";
    }else if(timestamp < 0){
        return "None";
    }
    var date = new Date(timestamp * 1000);
    var fmt="MMM dd, yyyy HH:mm a z";

    return dojo.date.locale.format( date, {selector:"date", datePattern:fmt } );
}
	
var MadDashAdminEvents = function(){
    var instance = this;
    
    this.schedule = function(){
        var filters = buildFilters();
        var name = dijit.byId("eventName").value;
        if(!name){
            document.getElementById('eventSchedStatus').className= "error";
            document.getElementById('eventSchedStatus').style.display = "inherit";
            document.getElementById('eventSchedStatus').innerHTML = "Missing required field 'name'";
            return;
        }
        var description = dijit.byId("eventDescription").value;
        if(!description){
            document.getElementById('eventSchedStatus').className= "error";
            document.getElementById('eventSchedStatus').style.display = "inherit";
            document.getElementById('eventSchedStatus').innerHTML = "Missing required field 'description'";
            return;
        }
        var startTime = buildDateTime("eventStartDate", "eventStartTime");
        var endTime   = buildDateTime("eventEndDate", "eventEndTime");
        var changeStatus = dijit.byId("eventIsDown").checked;
        var json = dojo.toJson({
            "checkFilters": filters, 
            "name": name,
            "description": description,
            "startTime": startTime,
            "endTime": endTime,
            "changeStatus": changeStatus,});
        
        document.getElementById('eventSchedLoader').style.display = "inherit";
        document.getElementById('eventSchedButton').style.display = "none";
        document.getElementById("eventSchedButton").style.visibility = "hidden";
        dojo.xhrPost({
            url: '/maddash/admin/events',
            postData: json,
            timeout: 30000,
            handleAs: 'json',
            headers: {'Content-Type': 'application/json'},
            load: function(data){
                document.getElementById('eventSchedLoader').style.display = "none";
                document.getElementById('eventSchedButton').style.display = "inherit";
                document.getElementById("eventSchedButton").style.visibility = "visible";
                document.getElementById('eventSchedStatus').className= "success";
                document.getElementById('eventSchedStatus').innerHTML = "Successfully created event";
                document.getElementById('eventSchedStatus').style.display = "inherit";
            },
            error: function(error){
                document.getElementById('eventSchedLoader').style.display = "none";
                document.getElementById('eventSchedButton').style.display = "inherit";
                document.getElementById("eventSchedButton").style.visibility = "visible";
                document.getElementById('eventSchedStatus').className= "error";
                document.getElementById('eventSchedStatus').style.display = "inherit";
                if(error.status == 503){
                    document.getElementById('eventSchedStatus').innerHTML = "Unable to reach MaDDash server. If the server does not return within a few minutes have the server administrator restart MaDDash and Apache.";
                }else{
                    document.getElementById('eventSchedStatus').innerHTML = error.message;
                }
            }
        });
    }
    
    this.list = function(){
        dojo.xhrGet({
            url: '/maddash/admin/events',
            timeout: 30000,
            handleAs: 'json',
            load: function(data){
                if(data.events == null){
                    document.getElementById('eventListStatus').className= "error";
                    document.getElementById('eventListStatus').style.display = "inherit";
                    document.getElementById('eventListStatus').innerHTML = "";
                }else{
                    var heading = document.createElement("div");
                    heading.className = "maddashAdminEventListRowHeading";
                    heading.appendChild(maddashCreateDiv("maddashAdminEventListCol", "Name"));
                    heading.appendChild(maddashCreateDiv("maddashAdminEventListCol", "Description"));
                    heading.appendChild(maddashCreateDiv("maddashAdminEventListCol", "Start"));
                    heading.appendChild(maddashCreateDiv("maddashAdminEventListCol", "End"));
                    heading.appendChild(maddashCreateDiv("maddashAdminEventListCol", "Mark Down"));
                    heading.appendChild(maddashCreateDiv("maddashAdminEventListCol", "Remove Event"));
                    document.getElementById("maddashAdminEventList").appendChild(heading);
                    if(data.events.length == 0){
                        document.getElementById("maddashAdminEventList").appendChild(maddashCreateDiv("maddashAdminEventListRow", "No events currently scheduled."));
                    }else{
                        for(var i = 0; i < data.events.length; i++){
                            var row = document.createElement("div");
                            row.className = "maddashAdminEventListRow";
                            row.appendChild(maddashCreateDiv("maddashAdminEventListCol", data.events[i].name));
                            row.appendChild(maddashCreateDiv("maddashAdminEventListCol", data.events[i].description));
                            row.appendChild(maddashCreateDiv("maddashAdminEventListCol", formatEventTime(data.events[i].startTime)));
                            row.appendChild(maddashCreateDiv("maddashAdminEventListCol", formatEventTime(data.events[i].endTime)));
                            row.appendChild(maddashCreateDiv("maddashAdminEventListCol", (data.events[i].changeStatus ? "Yes" : "No")));
                            var event = new MadDashAdminEvent(data.events[i].uri);
                            var removeCol = document.createElement("div");
                            var removeButtonElem = document.createElement("button");
                            removeCol.className = "maddashAdminEventListCol";
                            removeCol.appendChild(removeButtonElem);
                            var removeButton = new dijit.form.Button({
                                label: "-",
                            }, removeButtonElem);
                            dojo.connect(removeButton, 'onClick', event, 'removeAsk');
                            row.appendChild(removeCol);
                            document.getElementById("maddashAdminEventList").appendChild(row);
                        }
                    }
                }
            },
            error: function(error){
                document.getElementById('eventListStatus').className= "error";
                document.getElementById('eventListStatus').style.display = "inherit";
                if(error.status == 503){
                    document.getElementById('eventListStatus').innerHTML = "Unable to reach MaDDash server. If the server does not return within a few minutes have the server administrator restart MaDDash and Apache.";
                }else{
                    document.getElementById('eventListStatus').innerHTML = error.message;
                }
            }
        });
    }
}

var MadDashAdminEvent = function(uri){
    var instance = this;
    this.uri = uri;
    this.removeHandle = null;
    this.closeDialogHandle = null;
    
    this.removeAsk = function() {
        if(this.removeHandle == null){
            this.removeHandle = dojo.connect(dijit.byId("removeDialogButton"), 'onClick', this, 'remove');
        }
        if(this.closeDialogHandle == null){
            this.closeDialogHandle = dojo.connect(dijit.byId("removeDialog"), 'onHide', this, 'cancelRemove');
        }
        dijit.byId("removeDialog").show();
    }
    
    this.cancelRemove = function() {
        if(this.removeHandle != null){
            dojo.disconnect(this.removeHandle);
            this.removeHandle = null;
        }
        if(this.closeDialogHandle != null){
            dojo.disconnect(this.closeDialogHandle);
            this.closeDialogHandle = null;
        }
    }
    
    this.remove = function(){
        dojo.xhrDelete({
            url: uri,
            timeout: 30000,
            handleAs: 'json',
            load: function(data){
                dijit.byId("removeDialog").hide();
                loadContent("maddashAdminContent", "widgets/eventList.html", renderEventList);
            },
            error: function(error){
                document.getElementById('eventListStatus').className= "error";
                document.getElementById('eventListStatus').style.display = "inherit";
                if(error.status == 503){
                    document.getElementById('eventListStatus').innerHTML = "Unable to reach MaDDash server. If the server does not return within a few minutes have the server administrator restart MaDDash and Apache.";
                }else{
                    document.getElementById('eventListStatus').innerHTML = error.message;
                }
            }
        });
        
    }
}

//content loading
function loadContent(divId, url, renderFunction){
    var div = dijit.byId(divId);
    div.set("href", url);
    div.set("onDownloadEnd", function(){
        if(renderFunction != null){
            renderFunction();
        }
    });
}

function renderCheckReschedule(){
    var filters = new MadDashAdminCheckFilters("maddashAdminFilters");
    filters.render();
    updateTimezoneSpan('reschedTZ');
}

function renderEventSchedule(){
    var filters = new MadDashAdminCheckFilters("maddashAdminFilters");
    filters.render();
    updateTimezoneSpan('startTZ');
    updateTimezoneSpan('endTZ');
}

function renderEventList(){
    var events = new MadDashAdminEvents();
    events.list();
}
