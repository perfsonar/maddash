#!/usr/bin/perl -T

use strict;
use CGI;

my $cgi = new CGI();

print $cgi->header;

#this is detailOwamp.cgi

my $uri = $cgi->param("uri");
if($uri){

print <<EOF;
<!DOCTYPE HTML>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <title>MaDDash Dashboard - Check Results</title>
        <link rel="stylesheet" href="lib/dojo/dijit/themes/claro/claro.css" media="screen">
        <link rel="stylesheet" href="style/maddash-webui.css" media="screen">
        <script src="lib/dojo/dojo/dojo.js" data-dojo-config="parseOnLoad: true"></script>
        <script src="lib/dojo/dojo/dojo-maddash.js"></script>
        <script type="text/javascript" src="lib/protovis/protovis-r3.2.js"></script>
        <script type="text/javascript" src="lib/maddash.js"></script>
        <script type="text/javascript" src="lib/maddash-webui.js"></script>
        <script>
            require(["dijit/TitlePane", "dijit/layout/TabContainer", "dijit/layout/ContentPane", "dojo/parser"]);
            
            function loadDashWidgets(){
                var configDS = new MaDDashDataSource("etc/config.json", false);
			    var config = new MadDashConfig();
			    configDS.connect(config);
			    configDS.render();
			    var titlePane = new MadDashTitleSpan("maddashTitle", "index.cgi");
			    titlePane.render(config.data);
			    
			    var mnugs = new MaDDashDataSource("/maddash/grids"); 
                var mnuds = new MaDDashDataSource("/maddash/dashboards"); 
                mnuds.connect(new MadDashNavMenu("maddashMenuBar", "index.cgi", mnugs));
                mnuds.render();
                
                var ds = new MaDDashDataSource("$uri");
                //ds.connect(new MaDDashCheckNav("maddashCheckNav", "/maddash-webui", ""));
                ds.connect(new MaDDashCheckTitle("maddashCheckTitle"));
                ds.connect(new MaDDashQuickStatus("maddashQuickStatus"));
                ds.connect(new MaDDashCheckSummary("maddashCheckSummary"));
                ds.connect(new MaDDashCheckStatistics("maddashCheckStats"));
                ds.connect(new MaDDashCheckDetails("maddashCheckDetails"));
                ds.connect(new MadDashHistory("maddashHistory"));
                ds.connect(new MaDDashGraphPane("maddashGraph"));
                ds.render();
            }
        </script>
    </head>
    <body class="claro" style="font-family:sans-serif" onload="loadDashWidgets()" marginheight="0" marginwidth="0">
        <div id="maddashTitle" class="maddashTitle"></div>
        <div id="maddashMenuBar"></div>
        <div id="maddashCheckTitle" class="maddashCheckTitleDiv"></div>
        <div id="maddashQuickStatus" class="maddashQuickStatus"></div>
        <div style="width:95%;height:1200px;margin-left:auto;margin-right:auto;margin-bottom:20px">
            <div data-dojo-type="dijit.layout.TabContainer" style="width:100%;height:100%;font-size:14px">
                <div data-dojo-type="dijit.layout.ContentPane" title="Summary">
                    <div id="maddashCheckSummaryPane" data-dojo-type="dijit.TitlePane" data-dojo-props="title: 'Current Results'">
                        <div id="maddashCheckSummary"></div>
                    </div>    
                    <div id="maddashCheckStatsPane" data-dojo-type="dijit.TitlePane" data-dojo-props="title: 'Statistics'" open="false">
                         <div id="maddashCheckStats"></div>
                    </div>
                    <div id="maddashGraphPane" data-dojo-type="dijit.TitlePane" data-dojo-props="title: 'Graph'">
                        <div id="maddashGraph" style="height: 800px"></div>
                    </div>
                </div>
                
                <div data-dojo-type="dijit.layout.ContentPane" title="History">
                    <div id="maddashHistory"></div>
                </div>
                
                <div data-dojo-type="dijit.layout.ContentPane" title="Check Details">
                    <div id="maddashCheckDetails"></div>
                </div>
            </div>
        </div>
    </body>
</html>
EOF

}else{
    print "<h1>No URI provided</h1>";
}