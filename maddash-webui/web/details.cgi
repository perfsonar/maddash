#!/usr/bin/perl -T

use strict;
use CGI;
use URI::Escape qw/uri_escape uri_unescape/;

my $cgi = new CGI();

print $cgi->header;

#this is detailOwamp.cgi

my $uri = $cgi->param("uri");

#handle refresh
my $refreshParam = $cgi->param("refresh");
my $refreshTime = "0";
if($refreshParam && $refreshParam =~ /\d+/){
    $refreshTime = $refreshParam;
}

if($uri){

#sanitize uri
my @uri_parts = split '/', $uri;
my $first_part = 1;
my $encoded_uri = "";
foreach my $uri_part(@uri_parts){
    $uri_part =~ s/\+/ /g;#get rid of any spaces encoded as + signs
    $encoded_uri .= '/' unless($first_part);
    $encoded_uri .= uri_escape(uri_unescape($uri_part));
    $first_part = 0;
}

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
                var privacyPolicy = new MadDashPrivacyPolicy("maddashPrivacyPolicy");
			    privacyPolicy.render(config.data);
			    
                
                var ds = new MaDDashDataSource("$encoded_uri");
                //ds.connect(new MaDDashCheckNav("maddashCheckNav", "/maddash-webui", ""));
                ds.connect(new MaDDashCheckTitle("maddashCheckTitle"));
                ds.connect(new MaDDashQuickStatus("maddashQuickStatus", config.data));
                ds.connect(new MaDDashCheckSummary("maddashCheckSummary", config.data));
                ds.connect(new MaDDashCheckStatistics("maddashCheckStats"));
                ds.connect(new MaDDashCheckDetails("maddashCheckDetails"));
                ds.connect(new MadDashHistory("maddashHistory", config.data));
                ds.connect(new MaDDashGraphPane("maddashGraph"));
                ds.connect(new MaDDashRefreshLabel("maddashRefreshStatus"));
                
                var mnugs = new MaDDashDataSource("/maddash/grids"); 
                var mnuds = new MaDDashDataSource("/maddash/dashboards"); 
                var navMenu = new MadDashNavMenu("maddashMenuBar", "index.cgi", config, {}, mnugs, ds);
                navMenu.setPageRefresh($refreshTime);
                mnuds.connect(navMenu);

                mnuds.render();
                ds.render();
            }
        </script>
    </head>
    <body class="claro" style="font-family:sans-serif" onload="loadDashWidgets()" marginheight="0" marginwidth="0">
        <div id="maddashTitle" class="maddashTitle"></div>
        <div id="maddashMenuBar"></div>
        <div id="maddashRefreshStatus"></div>
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
        <div class="maddashFooterMoreInfo">
            <br>
            <hr width="90%">
            More information on MaDDash available <a target="newwindow" href="https://docs.perfsonar.net">here</a>
            <div id="maddashPrivacyPolicy"></div>
        </div>
    </body>
</html>
EOF

}else{
    print "<h1>No URI provided</h1>";
}
