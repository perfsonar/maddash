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
 *   aysnc: optional boolean indicating if the render call should be asynchronous. Defaults to true.
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
			for(var i=0; i < instance.callbacks.length; i++){
				instance.callbacks[i].render(this.data, instance.url);
			}
		}	
	}
	
}

/**
 * Class: MaDDashGrid
 * Description: Widget that displays grid of checks. Uses
 *   d3 and jQuery to draw the grid.
 * Parameters:
 *      parentId: id string of the container element
 *      legendId: id string of the legend element
 */

var MaDDashGrid = function(parentId, legendId, reportId){
    var instance = this;
    var colorscale = {
        0: "#009E73", 
        1: "#F0E442", 
        2: "#CC79A7", 
        3: "#E69F00", 
        4: "#56B4E9",
        5: "#000000"
        };

    this.parent = parentId;
    this.legend = legendId;
    this.report = reportId;
    this.cellSize = 13;
    this.cellPadding = 2;
    
    this.setColorScale = function(colors){
        colorscale = colors;
    }
    
    this.getColorScale = function(){
        return colorscale;
    }
    
    this.render = function (data){
        //TODO: Set title
        //d3.select("#dashboard_name").html(dashboard.name + " Dashboard");
        d3.select("#" + this.parent).html("");
        this.displaygrid(data, this.parent);
    }
      
      
    this.displaygrid = function (data, canvas){
      //Display legends
      var legendsdata = data.statusLabels
      .map(function(d,i){ return {label:d, color:colorscale[i]} })
      //.filter(function(d,i){return d.label === null ? false : true})
      d3.select("#"+this.legend).html("")
      var legends = d3.select("#"+this.legend)
        .selectAll(".legend")
          .data(legendsdata)
          .enter()
            .append("div").attr("class", "legend");
      legends.append("div")
        .attr("class", "lsymbol")
        .style("background", function(d,i){return d.color})
        .style("display", function(d,i){return d.label === null ? "none" : "block"})
      legends.append("div")
        .attr("class", "ltext")
        .text(function(d,i){return d.label})
        .style("display", function(d,i){return d.label === null ? "none" : "block"})
        
        
      //report
      var reportdata = data.report;
      var reportdiv = d3.select("#"+this.report).html("");
      var problemCount = 0;
      var hostCount = 0;
      var hostProblemCount = 0;
      var gridProblemCount = 0;
      var reportDetailUrl = "report.cgi?grid=" + encodeURIComponent(data.name);
      if(reportdata != null){
            if(reportdata.global.severity > 0){
                gridProblemCount += reportdata.global.problems.length;
                problemCount += gridProblemCount;
            }
            
            for(var site in reportdata.sites){
                if(reportdata.sites[site].severity > 0){
                    hostCount++;
                    hostProblemCount += reportdata.sites[site].problems.length;
                    problemCount += reportdata.sites[site].problems.length;
                }
            }
      }
      if(problemCount > 0){
        var reportLink = reportdiv.attr('class', 'gridReportSummaryWarn')
                            .append("a").attr("href", reportDetailUrl);
        reportLink.append("img").attr("src", "images/warning.png");
        reportLink.append("span")
            .attr("class", "gridReportSummaryWarnText")
            .append("a").attr("href", reportDetailUrl)
            .text("Found a total of " + problemCount +  
                    (problemCount == 1 ? " problem" : " problems") + 
                    (hostCount == 1 ? " involving " + hostCount + " host" : "") + 
                    (hostCount > 1 ? " involving " + hostCount +  " hosts" : "") + 
                    (gridProblemCount > 0 && hostCount > 0 ? " with " + gridProblemCount + " affecting the entire grid" :
                        (gridProblemCount > 0 && hostCount == 0 ? " and it is affecting the entire grid" : 
                            (gridProblemCount == 0 ? " in the grid" : "")))
                );
      }else{
        reportdiv.attr('class', 'gridReportSummaryOk')
            .append("img").attr("src", "images/success.png");
        reportdiv.append("span")
            .attr("class", "gridReportSummaryOkText")
            .text("No problems found in grid");
      }
      
      //GRID Container
      var nrows = data.grid.length;
      var ncols = data.grid[0].length;
      
      //Changed these from portal to allow customization
      var cellsize = this.cellSize;
      var padding = this.cellPadding;
      var text_multiplier = 7;
       //calc max row text size
      var maxRowSize = 0;
      for(var ri = 0; ri < data.rows.length; ri++){
        if(data.rows[ri].name.length > maxRowSize){
            maxRowSize = data.rows[ri].name.length;
        }
      }
      maxRowSize = maxRowSize * text_multiplier + 10;//10 is just arbitrary margin
      var maxColSize = 0;
      for(var ci = 0; ci < data.columnNames.length; ci++){
        if(data.columnNames[ci].length > maxColSize){
            maxColSize = data.columnNames[ci].length;
        }
      }
      maxColSize = (maxColSize+1) * text_multiplier; //plus one gives some good margin
      
      var viz = d3.select("#" + canvas)
        .style("width", ncols * (cellsize + 2*padding) + 110 + maxRowSize)
      var top = viz.append("div")
        .attr("class", "gtop")
        .style("margin-left", maxRowSize + "px")
        .style("float", "left")
        .append("svg:svg")
          .attr("height", maxColSize)
          .attr("width", ncols * (cellsize + 2*padding) + 90)
        .selectAll(".rname")
          .data(data.columnNames)
          .enter()
            .append("g")
            .attr("class", function(d,i){return "gcol" + i})
            .attr("transform", function(d,i){
                return "translate("+((i*(cellsize+2*padding)))+",0)"}
            );       
    
      top.append("svg:rect")
        .attr("class", function(d,i){return "gcol" + i})
        .attr("x",0).attr("y",0)
        .attr("transform", function(d,i){
                //return "rotate(45,0,"+ (d.length * text_multiplier) + ") translate (-0.5,3)";
                //return "rotate(45,0," + maxColSize +") translate(0,0)";
                return "translate (0, 0)";
            })
        .attr("height",function(d,i){ return maxColSize}).attr("width",(cellsize+padding))
      
      
      top.append("svg:a")
        .attr("xlink:href", function(d,i){
                if(data.columnProps[i]['pstoolkiturl']){
                    return data.columnProps[i]['pstoolkiturl'];
                }else if(data.report.sites[d].severity > 0){
                    return reportDetailUrl + "&host=" + encodeURIComponent(d);
                }
                return null;
            })
        .attr("target", "_blank")
        .attr("class", function(d,i){
            if(data.columnProps[i]['pstoolkiturl']){
                return "glink";
            }else{
                return "gnolink";
            }
        })
        .append("svg:text")
        .attr("class", function(d,i){
            if(data.report.sites[d].severity > 0){
                return "gtexterr";
            }else{
                return "gtext";
            }
        })
        .attr("text-anchor", "start")
        .attr("dy", "1.5em")
        .attr("dx", "1em")
        .attr("style", function(d,i){
            if(data.report.sites[d].severity > 0){
                return "fill: " + colorscale[parseInt(data.report.sites[d].severity)] + ";" ;
            }else{
                return "";
            }
        })
        .attr("transform", 
            function(d,i){
                return "rotate(-90) translate(-" + (maxColSize + 10) + ", -3)";
                //return "rotate(-45," + maxColSize + "," + (ncols * (cellsize + 2*padding)) + ") translate(0,0)";
            }
        )
        .text(function(d,i){return d})        
    
      var left = viz.append("div")
        .attr("class", "gleft")
        .append("svg:svg")
          .attr("width", maxRowSize)
          .attr("height", nrows * (cellsize + 2*padding))
        .selectAll(".rname")
          .data(data.rows)
          .enter()
            .append("g")
            .attr("class", function(d,i){return "grow" + i})
            .attr("transform", function(d,i){return "translate(0,"+(i*(cellsize+2*padding))+")"})

      left.append("svg:rect")
        .attr("class", function(d,i){return "grow" + i})
        .attr("x",0).attr("y",0)
        .attr("width",maxRowSize).attr("height",(cellsize+2*padding))
      
      left.append("svg:a")
        .attr("xlink:href", function(d,i){
                if(data.rows[i].props['pstoolkiturl']){
                    return data.rows[i].props['pstoolkiturl'];
                }else if(data.report.sites[data.rows[i].name].severity > 0){
                    return reportDetailUrl + "&host=" + encodeURIComponent(data.rows[i].name);
                }
                return null;
        })
        .attr("target", "_blank")
        .attr("class", function(d,i){
            if(data.rows[i].props['pstoolkiturl']){
                return "glink";
            }else{
                return "gnolink";
            }
        })
        .append("svg:text")
        .attr("class", function(d,i){
            if(data.report.sites[data.rows[i].name].severity > 0){
                return "gtexterr";
            }else{
                return "gtext";
            }
        })
        .attr("style", function(d,i){
            if(data.report.sites[data.rows[i].name].severity > 0){
                return "fill: " + colorscale[parseInt(data.report.sites[data.rows[i].name].severity)]  + ";" ;
            }else{
                return "";
            }
        })
        .attr("transform", "translate("+ (maxRowSize-5) +",0)")
        .text(function(d,i){return data.rows[i].name})
        .attr("text-anchor", "end")
        .attr("dy", "1.1em")


      var grid = viz.append("div")
        .attr("class", "ggrid")
    
      var cols = grid.selectAll(".gcol")
        .data(data.columnNames)
        .enter()
          .append("div")
          .attr("class", function(d,i){return "gcol gcol" + i})
          .style("width", (cellsize+2*padding) + "px")
          .style("height", "100%")
          .style("left", function(d,i){return (i*(cellsize+2*padding)) + "px"})
    
      var rows = grid.selectAll(".grow")
        .data(data.grid)
        .enter()
          .append("div")
          .attr("class", function(d,i){return "grow grow" + i})
          .style("width", "100%")
          .style("z-index", 1000)
    
      var color = "";
      var selected_row = 0;
      var selected_col = 0;
      var cells = rows.selectAll(".gcell")
        .data(function(d,r){ return d.map(function(d,i){ return {celldata:d, row:r}}) })
        .enter()
          .append("div")
          .attr("class", "gcell")
          .style("height", cellsize +"px")
          .style("width", cellsize +"px")
          .style("margin", (padding) +"px")
          
          .on("mouseover", function(d,i){
            selected_row = d.row;
            selected_col = i;
            if(d.celldata){
              d3.select(this).style("margin", (padding-1) +"px");
              d3.select(this).classed("shadow", true);
            }
            viz.selectAll(".gcol"+ i).classed("gactive", true);
            viz.selectAll(".grow"+ d.row).classed("gactive", true);
          })
          .on("mouseout", function(d,i){
            // d3.select(this).classed("shadow", false);
            // d3.select(this).style("background-color", color.brighter());
            d3.select(this).style("margin", (padding) +"px");
            d3.select(this).classed("shadow", false);
            viz.selectAll(".gcol"+ i).classed("gactive", false);
            viz.selectAll(".grow"+ d.row).classed("gactive", false);
          })
         /* 
           //DELETE FROM PORTAL
           .on("click", function(d,i){
            var that = this;
            if(d.celldata!=null){
              var href = "details.cgi?uri=" + d.celldata[0].uri;
              window.open( href, "Graph");
            }
          }) */
      
      var subcell_count = 1;
      $("#"+canvas).find(".gcell").each(function(i,d){
        var data = d3.select(this).data()[0];
        
        var html = "<div class='tooltip'>";
        if(data.celldata!=null){
          subcell_count = data.celldata.length;
          for(var msgi = 0; msgi < subcell_count; msgi++){
              var msg = (data.celldata[msgi]? data.celldata[msgi].message : "");
              if(msg && data.celldata[msgi].status == 5){
                msg = "Scheduled downtime. Click box for details.";
              }
              html += "<div class='" + (msgi == 0 ? "top-tip" : "bottom-tip") + "'>" + msg + "</div>";
          }
          html += "</div>";
          
          $(this).tipsy({
            html :true,
            opacity: 0.9,
            title : function(){
            return html
          }})
        }
      })
      
      var temp = cells.selectAll(".gsubcell")
        .data(function(d,i){return d.celldata===null? [] : d.celldata })
        .enter()
          .append("div");
      temp
        .style("height", cellsize/subcell_count +"px")              
        .style("background", function(d,i){
          return colorscale[parseInt(d.status)];
        })
        .on("click", function(d,i){ //CHANGE FROM PORTAL
            var that = this;
            if(d!=null && d.uri!=null && instance.handleClick != null){
              instance.handleClick(d);
            }
          })
  }
  
  /****************************
     Customization functions 
  ****************************/
    /* Default handler. Can be overridden with setClickHandler */
    this.handleClick = function(d){
        var uri = d.uri;
        $.getJSON(uri, function(data) {
                var href = data['history'][0].returnParams.graphUrl.replace("https", "http");
                 window.open( href, "Graph", "menubar=0,location=0,height=700,width=700" );
         })
        
    }
    this.setClickHandler = function(f){
        this.handleClick = f;
    }
    this.setCellSize = function(value){
        this.cellSize = value;
    }
    this.setCellPadding = function(value){
        this.cellPadding = value;
    }
    
}