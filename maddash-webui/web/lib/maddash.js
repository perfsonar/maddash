/**
 * File: maddash.js
 * Description: This file contains bases classes for MaDDash framework.
 *
 * Authors: Andy Lake <andy@es.net>
 */


/**
 * Class: MaDDashDataSource
 * Description: Connects MadDash widgets to backen data. Created
 *   with a URI that specifies the location of some REST data. You then
 *   call the "connect" function which is given a Javascript object. That 
 *   Javascript MUST implement a render function. When MaDDashDataSource.render()
 *   is called it will asynchronously retrieve the data and call each connected objects
 *   render(data, uri) function.
 * Parameters:
 *   url: the URL to the rest data that will be rendered by connected objects
 */
var MaDDashDataSource = function(url, async){
	var instance = this;
	this.xhr = null;
	this.url = url;
	this.data = null;
	this.callbacks = new Array();
	this.async = (async == null ? true : async);
	
	this.setURL = function(url){
		this.url = url;
	}
	
	this.render = function() {
		this._initXhr();
		this.xhr.open("GET", this.url, this.async);
		this.xhr.send();
	}
	
	this.connect = function(obj){
		this.callbacks.push(obj);
	}
	/**
	 * Private methods
	 */
	this._initXhr = function() {
		//return if xhr already inited
		if(this.xhr != null){
			return;
		}
		
		if (window.XMLHttpRequest){
			// code for IE7+, Firefox, Chrome, Opera, Safari
  			this.xhr=new XMLHttpRequest();
	  	}else{
			// code for IE6, IE5
	  		this.xhr=new ActiveXObject("Microsoft.XMLHTTP");
	  	}
		
		//set response handler
		this.xhr.onreadystatechange = this._xhrResponseHandler;
	}
	
	this._xhrResponseHandler = function(){
		if(instance.xhr.readyState == 4 && instance.xhr.status==200){
			this.data = eval('(' + instance.xhr.responseText + ')');
			for(i=0; i < instance.callbacks.length; i++){
				instance.callbacks[i].render(this.data, instance.url);
			}
		}	
	}
	
}

/**
 * Class: MaDDashGrid
 * Description: Widget that displays grid of checks. Uses
 *   protovis to draw the grid.
 * Parameters:
 *      parent: a string or object representing a container element
 */
var MaDDashGrid = function(parent){
	var instance = this;
	this.vis = new pv.Panel();
	this.parent = document.getElementById(parent);
	
	/* Set urls */
	this.cellUrl = "details.cgi";
	this.passCellUrlGetParams = true;
	
	/* Table margins from top and left of div  */
	this.t = 125;
	this.l = 125;
	/* The cell dimensions. */
	this.w = 36
	this.h = 18;
	
	/* Various style settings */
	this.okColor = "#00FF00";
	this.warnColor = "yellow";
	this.criticalColor = "red";
	this.unknownColor = "orange";
	this.emptyColor = "#CCCCCC";
	this.defaultLabelColor = "#000000";
	this.defaultLabelFont = "10px sans-serif";
	this.selectedLabelFont = "bold 12px sans-serif";
	this.selectedLabelColor = "#000000";
	
	this.setCellUrl = function (url) {
		this.cellUrl = url;
	}
	
	this.setPassCellUrlGetParams = function (passCellUrlGetParams) {
		this.passCellUrlGetParams = passCellUrlGetParams;
	}
	
	this.setTopMargin = function (t) {
		this.t = t;
	}
	
	this.setLeftMargin = function (l) {
		this.l = l;
	}
	
	this.setCellWidth = function (w) {
		this.w = w;
	}
	
	this.setCellHeight = function (h) {
		this.h = h;
	}
	
	this.setOkColor = function (color) {
		this.okColor = color;
	}
	
	this.setWarnColor = function (color) {
		this.warnColor = color;
	}
	
	this.setCriticalColor = function (color) {
		this.criticalColor = color;
	}
	
	this.setEmptyColor = function (color) {
		this.emptyColor = color;
	}
	
	this.setDefaultLabelFont = function (font) {
		this.defaultLabelFont = font;
	}
	
	this.setDefaultLabelColor = function (color) {
		this.defaultLabelColor = color;
	}
	
	this.setSelectedLabelFont = function (font) {
		this.selectedLabelFont = font;
	}
	
	this.setSelectedLabelColor = function (color) {
		this.selectedLabelColor = color;
	}
	
	this._getFill = function( d, f ){		
		if(d == null){
			return instance.emptyColor;
		}else if(d.status == 0){
			return instance.okColor;
		}else if(d.status == 1){
			return instance.warnColor;
		}else if(d.status == 2){
			return instance.criticalColor;
		}
		return instance.unknownColor;
	}
	
	this._setCellLink = function(d, f){
		if(d == null){ return; }
		var loc = instance.cellUrl;
		if (instance.passCellUrlGetParams) {
			loc += "?uri=" + d.uri;
		}
		window.location = loc; 
	}
	
	this._setCellCursor = function(d, f){
		if(d == null){
			return "default";
		}
		return "pointer";
	}
	
	this._setLabelStyle = function( hLabels, vLabels, hIndex, vIndex ){
		hLabels.textStyle(function(){
			if(this.index == hIndex){
				return instance.selectedLabelColor;
			}else{
				return instance.defaultLabelColor;
			}
		}).font(function(){
			if(this.index == hIndex){
				return instance.selectedLabelFont;
			}else{
				return instance.defaultLabelFont;
			}
		});
		vLabels.textStyle(function(d){
			if(this.index== vIndex){
				return instance.selectedLabelColor;
			}else{
				return instance.defaultLabelColor;
			}
		}).font(function(d){
			if(this.index == vIndex){
				return instance.selectedLabelFont;
			}else{
				return instance.defaultLabelFont;
			}
		});
		instance.vis.render();
	}
	
	this.render = function( data ) {
		//localize a few vars
		var h = this.h;
		var w = this.w;
		
		//prepare the data and get the column names
		var colNames = data.columnNames;
		var rows = data.rows;
		var checkNames = data.checkNames;
		//console.log(data.grid[0]);
		//data = data.map(function(d){ return pv.dict(cols, function(s){ return d[this.index]; });});
		//cols.shift();
		
		//set the labels
		var hLabels = this.vis.add(pv.Label)
		    .data(colNames)
		    .left(function(){ return this.index * w + w / 2; })
		    .textAngle(-Math.PI / 2)
		    .textBaseline("middle")
			.font(this.defaultLabelFont)
			.textStyle(this.defaultLabelColor);
		
		var vLabels = this.vis.add(pv.Label)
		    .data(rows)
		    .top(function(){ return this.index * h + h / 2; })
			.text(function(d){ return d.name; })
		    .textAlign("right")
		    .textBaseline("middle")
			.font(this.defaultLabelFont)
			.textStyle(this.defaultLabelColor);
			
		this.vis.width(colNames.length * this.w + 5) //add 5 so draws far right stroke
		    	.height(rows.length * this.h)
		    	.top(this.t)
		    	.left(this.l);
				
		this.vis.add(pv.Panel)
		    .data(data.grid)
		    .top(function(){ return this.index * h; })
		    .height(this.h)
			.event("mouseout",function(d, f){
				instance._setLabelStyle(hLabels, vLabels, -1, -1);
			})
		    .add(pv.Panel)
		    .data(function(){
				return data.grid[this.parent.index];
			})
		    .left(function(){ return this.index * w; })
	        .width(this.w)
			.strokeStyle("black")
		    .lineWidth(2)
			.add(pv.Panel)
		    .data(function(){
				if(data.grid[this.parent.parent.index][this.parent.index] == null){
					return [ null ];
				}
				return data.grid[this.parent.parent.index][this.parent.index];
			})
			.height(function(d){
				if(d == null){
					return h;
				}
				return h/checkNames.length;
			})
			.top(function(){
				return this.index *  h/checkNames.length;
			})
		    .fillStyle( this._getFill )
		    .strokeStyle("black")
		    .lineWidth(1)
		    .antialias(false)
		    .title(function(d, f){
				if (d != null) {
					return checkNames[this.index] + ": " + d.message;
				}else{
					return "No checks configured";
				}
				
			})
			.cursor(this._setCellCursor)
			.event("click", this._setCellLink )
			.event("mouseover", function(d, f){ instance._setLabelStyle(hLabels, vLabels, this.parent.index, this.parent.parent.index); } );

		this.vis.canvas(this.parent).render()
	}
}

/**
 * Class: HistoryBar
 * Description: NOT YET IMPLEMENTED, PLEASE IGNORE
 */
var HistoryBar = function(src, dst, timeRange, barParts){
	var instance = this;
	this.vis = new pv.Panel();
	
	this.url = "data/redGreenTable.json";
	/* Table margins from top and left of div  */
	this.t = 5;
	this.l = 5;
	/* The cell dimensions. */
	this.w = 48
	this.h = 26;
	
	//specific vars
	this.src = src;
	this.dst = dst;
	this.timeRange = timeRange;
	this.barParts = barParts;
	
	this.setDataUrl = function (url) {
		this.url = url;
	}
	
	this.setTopMargin = function (t) {
		this.t = t;
	}
	
	this.setLeftMargin = function (l) {
		this.l = l;
	}
	
	this.setCellWidth = function (w) {
		this.w = w;
	}
	
	this.setCellHeight = function (h) {
		this.h = h;
	}
	
	this.setSrc = function (src) {
		this.src = src;
	}
	
	this.setDst = function (dst) {
		this.dst = dst;
	}
	
	this.setTimeRange = function (timeRange) {
		this.timeRange = timeRange;
	}
	
	this.setBarParts = function (barParts) {
		this.barParts = barParts;
	}
	
	this._getFill = function( d, f ){
		if(f == 1){
			return "red";
		}else if(f == 0){
			return "#00FF00";
		}else{
			return "#CCCCCC";
		}
	}
	
	this.render = function( data ) {
		//localize a few vars
		var h = this.h;
		var w = this.w;
		
		this.vis.width(data.length * this.w + 5) //add 5 so draws far right stroke
		    	.height(this.h + 10)
		    	.top(this.t)
		    	.left(this.l);
		this.vis.add(pv.Panel)
		    .data(data)
		    .left(function(){ return this.index * w; })
		    .width(this.w)
		    .add(pv.Panel)
		    .height(this.h)
		    .fillStyle( this._getFill )
		    .strokeStyle("black")
		    .lineWidth(1)
		    .antialias(false)
			
		this.vis.render()
	}
}
