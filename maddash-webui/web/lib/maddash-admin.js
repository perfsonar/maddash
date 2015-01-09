/**
 * File: maddash-webui.js
 * Description: This file contains classes that represent widgets used in the web interface
 * of the MaDDash Web-UI. Most of the widgets require the dojo toolkit. The dojo
 * requirement and layout specific nature of these classes distinguishes them from
 * those found in maddash.js file.
 *
 * Authors: Andy Lake <andy@es.net>
 */
require(["dijit/form/FilteringSelect", "dijit/form/CheckBox", "dijit/form/ComboBox", "dojo/store/Memory", "dijit/form/Button", "dijit/form/DateTextBox", "dijit/form/TimeTextBox", "dojo/date", "dojo/_base/json", "dijit/layout/ContentPane"]);


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
		if (data == null) {
			console.log("data is null");
			//return;
		}
		
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
		if (data == null) {
			console.log("data is null");
			//return;
		}
		var name = this.parent.name;
		var label = this.parent.label;
		console.log("name=" + name);
	    console.log("label=" + label);
	    
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
        
        console.log(document.getElementById('reschedLoader'));
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
                console.log(data);
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
                console.log(error);
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
        console.log(json);
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
    updateTimezoneSpan('reschedTZ');
}


