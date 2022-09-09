#!/bin/bash

##
# Script to insert default alternate colors for people upgrading to 2.X
# Note that prior to 2.X the default json file was invalid json according to RFC so can't
# read in JSON file, thus doing this ugly shell script

#only upgrades 1.x releases
PREV_VERSION=$1
if [[ ! "$PREV_VERSION" =~ ^1\. ]]; then
    exit 0
fi

#skip if alternateColors already set
grep -q "alternateColors" /etc/maddash/maddash-webui/config.json 
if [ $? -eq 0 ]; then
    exit 0
fi

TMPFILE=`mktemp`
cat > $TMPFILE <<- EOF
{
    "alternateColors": [
        {
            "name": "Classic",
            "colors": {
                "0": "green", 
                "1": "yellow", 
                "2": "red", 
                "3": "orange", 
                "4": "gray",
                "5": "black"
            }
        },
        {
            "name": "Gray Unknown",
            "colors": {
                "0": "#009E73", 
                "1": "#F0E442", 
                "2": "#CC79A7", 
                "3": "#A2A2A2", 
                "4": "#56B4E9",
                "5": "#000000"
            }
        },
        {
            "name": "Forest Rain",
            "colors": {
                "0": "#33a02c", 
                "1": "#b2df8a", 
                "2": "#1f78b4", 
                "3": "#a6cee3", 
                "4": "#eeeeee",
                "5": "black"
            }
        },
        {
            "name": "Heatwave",
            "colors": {
                "0": "#fecc5c", 
                "1": "#fd8d3c", 
                "2": "#e31a1c", 
                "3": "#ffffb2", 
                "4": "#eeeeee",
                "5": "black"
            }
        },
        {
            "name": "Old Movie",
            "colors": {
                "0": "#cccccc", 
                "1": "#969696", 
                "2": "#525252", 
                "3": "#f7f7f7", 
                "4": "#eeeeee",
                "5": "black"
            }
        },
        {
            "name": "Pastel",
            "colors": {
                "0": "#8dd3c7", 
                "1": "#ffffb3", 
                "2": "#fb8072", 
                "3": "#bebada", 
                "4": "#eeeeee",
                "5": "black"
            }
        },
        {
            "name": "Sea Breeze",
            "colors": {
                "0": "#bae4bc", 
                "1": "#7bccc4", 
                "2": "#2b8cbe", 
                "3": "#f0f9e8", 
                "4": "#eeeeee",
                "5": "black"
            }
        }
     ],
EOF
cp -f /etc/maddash/maddash-webui/config.json /etc/maddash/maddash-webui/config.v1.json
sed 0,/{/s/// /etc/maddash/maddash-webui/config.json >> $TMPFILE
mv -f $TMPFILE /etc/maddash/maddash-webui/config.json
chown maddash:maddash /etc/maddash/maddash-webui/config.json
chmod 644 /etc/maddash/maddash-webui/config.json