******
Checks
******

Retrieving a single check
=========================

Request
-------

The request is a HTTP GET to the check URL. See below for full summary:

+---------------+------------------------------------------------------------------+
| *URL*         | /maddash/grids/<grid-name>/<row-name>/<column-name>/<check-name> |
+---------------+------------------------------------------------------------------+
| *HTTP Method* | GET                                                              |
+---------------+------------------------------------------------------------------+

*URL Parameters*

+------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| Name             | Value                                                                                                                                                                 |
+==================+=======================================================================================================================================================================+
| *resultsPerPage* | The maximum number of results to return in the history object. Defaults to 10.                                                                                        |
+------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| *page*           | The page of results to display. It will start with the result at numResults `*` page (starting with 0 and ordered from newest to oldest check result). Defaults to 0. |
+------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------+

Response
--------

*Type:* application/json

+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| Field                        | Type                             | Required| Description                                                                                                   |
+==============================+==================================+=========+===============================================================================================================+
| *gridName*                   | string                           | Yes     | The name of the grid that contains this check                                                                 |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *rowName*                    | string                           | Yes     | The name of the row that contains this check                                                                  |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *colName*                    | string                           | Yes     | The name of the column that contains this check                                                               |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *checkName*                  | string                           | Yes     | The name of the check                                                                                         |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *description*                | string                           | Yes     | A description of the check                                                                                    |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *prevCheckTime*              | long                             | Yes     | A Unix timestamp (in seconds) when the check previously ran                                                   |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *nextCheckTime*              | long                             | Yes     | A Unix timestamp (in seconds) when the check will run next                                                    |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+  
| *status*                     | int                              | Yes     | The current status of the check. See the :ref:`status-codes` table for values.                                |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+ 
| *returnCode*                 | int                              | Yes     | The current status of the check. See the :ref:`status-codes` table for values.                                |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+ 
| *message*                    | string                           | Yes     | The last message returned by the check                                                                        |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *type*                       | string                           | Yes     | The type of check                                                                                             |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *params*                     | Object                           | Yes     | Type specific check configuration parameters                                                                  |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *returnCodeCount*            | int                              | Yes     | The number of times a the returnCode was seen if its different than the current status. 0 if same as status.  |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *checkInterval*              | int                              | Yes     | The time in between checks if the returnCode and status are the same                                          |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *retryInterval*              | int                              | Yes     | The time in between checks if the status and returnCode are different                                         |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *retryAttempts*              | int                              | Yes     | The number of times a returnCode different than the status must be seen before changing the status            |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *globalReport*               | :ref:`api_reports-report_object` | No      | A report of any problems affecting the parent grid of this check                                              |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *rowReport*                  | :ref:`api_reports-report_object` | No      | A report of any problems affecting the row of this check                                                      |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *collReport*                 | :ref:`api_reports-report_object` | No      | A report of any problems affecting the column of this check                                                   |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *historyPageCount*           | int                              | Yes     | The number of pages available for the history. Assumes the current result set size.                           |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *historyResultsPerPage*      | int                              | Yes     | The maximum number of results per page. Echos the resultsPerPage URL parameter.                               |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *history*                    | array of objects                 | Yes     | An array of previous results for this check. If page is 0 then teh first check is the most recent check run   |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *history[n].time*            | long                             | Yes     | A Unix timestamp (seconds) when the check was run.                                                            |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *history[n].returnCode*      | int                              | Yes     | The return code of this check. See the :ref:`status-codes` table for values.                                  |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *history[n].message*         | string                           | Yes     | The message returned by this check                                                                            |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *history[n].returnParams*    | object                           | Yes     | Type specific parameters returned by this check                                                               |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *history[n].returnCodeCount* | int                              | Yes     | The number of times this return code was seen, if different than the status. 0 if same as status.             |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *history[n].status*          | int                              | Yes     | The status of the check at the time this check was run                                                        |
+------------------------------+----------------------------------+---------+---------------------------------------------------------------------------------------------------------------+

Example
+++++++

::

    {
       "gridName":"ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing",
       "rowName":"denv-pt1.es.net",
       "colName":"chic-pt1.es.net",
       "checkName":"Throughput Reverse",
       "description":"Throughput from chic-pt1.es.net to denv-pt1.es.net",
       "prevCheckTime":1468945283,
       "nextCheckTime":1468959683,
       "status":0,
       "returnCode":0,
       "message":" Average throughput is 9.066Gbps ",
       "returnCodeCount":0,
       "type":"net.es.maddash.checks.PSNagiosCheck",
       "params":{
          "graphUrl": "http://ps-dashboard.es.net/perfsonar-graphs/graphWidget.cgi?url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.252.45&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.252.45&",
          "command":"/usr/lib64/nagios/plugins/check_throughput.pl -u %maUrl -w 5: -c 1: -r 86400 -s %col -d %row -p tcp",
          "maUrl": "http://chic-pt1.es.net:8085/esmond/perfsonar/archive"
       }, 
       "checkInterval":14400,
       "retryInterval":600,
       "retryAttempts":3,
       "statusShortName":"OK",
       "historyPageCount":5,
       "historyResultPerPage":10,
       "history":[
          {
             "time":1468945283,
             "returnCode":0,
             "message":" Average throughput is 9.066Gbps ",
             "returnParams":{
                "Count":"3",
                "graphUrl":"http://ps-dashboard.es.net/perfsonar-graphs/graphWidget.cgi?url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.252.45&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.252.45&",
                "Max":"9.618572",
                "Standard_Deviation":"0.511447234967904",
                "Average":"9.06594433333333",
                "maUrl":"http://chic-pt1.es.net:8085/esmond/perfsonar/archive",
                "Min":"8.609269"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1468929262,
             "returnCode":0,
             "message":" Average throughput is 8.278Gbps ",
             "returnParams":{
                "Count":"4",
                "graphUrl":"http://ps-dashboard.es.net/perfsonar-graphs/graphWidget.cgi?url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.252.45&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.252.45&",
                "Max":"9.917373",
                "Standard_Deviation":"2.2768582559265",
                "Average":"8.278238",
                "maUrl":"http://chic-pt1.es.net:8085/esmond/perfsonar/archive",
                "Min":"4.967738"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1468914842,
             "returnCode":0,
             "message":" Average throughput is 8.278Gbps ",
             "returnParams":{
                "Count":"4",
                "graphUrl":"http://ps-dashboard.es.net/perfsonar-graphs/graphWidget.cgi?url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.252.45&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.252.45&",
                "Max":"9.917373",
                "Standard_Deviation":"2.2768582559265",
                "Average":"8.278238",
                "maUrl":"http://chic-pt1.es.net:8085/esmond/perfsonar/archive",
                "Min":"4.967738"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1468900420,
             "returnCode":0,
             "message":" Average throughput is 7.947Gbps ",
             "returnParams":{
                "Count":"4",
                "graphUrl":"http://ps-dashboard.es.net/perfsonar-graphs/graphWidget.cgi?url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.252.45&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.252.45&",
                "Max":"9.917373",
                "Standard_Deviation":"2.10691941712745",
                "Average":"7.947076",
                "maUrl":"http://chic-pt1.es.net:8085/esmond/perfsonar/archive",
                "Min":"4.967738"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1468886003,
             "returnCode":0,
             "message":" Average throughput is 8.198Gbps ",
             "returnParams":{
                "Count":"5",
                "graphUrl":"http://ps-dashboard.es.net/perfsonar-graphs/graphWidget.cgi?url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.252.45&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.252.45&",
                "Max":"9.917373",
                "Standard_Deviation":"1.90889560344947",
                "Average":"8.1978948",
                "maUrl":"http://chic-pt1.es.net:8085/esmond/perfsonar/archive",
                "Min":"4.967738"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1468871580,
             "returnCode":0,
             "message":" Average throughput is 8.443Gbps ",
             "returnParams":{
                "Count":"5",
                "graphUrl":"http://ps-dashboard.es.net/perfsonar-graphs/graphWidget.cgi?url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.252.45&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.252.45&",
                "Max":"9.917373",
                "Standard_Deviation":"2.04820449942671",
                "Average":"8.4426554",
                "maUrl":"http://chic-pt1.es.net:8085/esmond/perfsonar/archive",
                "Min":"4.967738"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1468857174,
             "returnCode":0,
             "message":" Average throughput is 9.008Gbps ",
             "returnParams":{
                "Count":"5",
                "graphUrl":"http://ps-dashboard.es.net/perfsonar-graphs/graphWidget.cgi?url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.252.45&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.252.45&",
                "Max":"9.917373",
                "Standard_Deviation":"0.939671374584643",
                "Average":"9.0076434",
                "maUrl":"http://chic-pt1.es.net:8085/esmond/perfsonar/archive",
                "Min":"7.792678"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1468842761,
             "returnCode":0,
             "message":" Average throughput is 8.780Gbps ",
             "returnParams":{
                "Count":"4",
                "graphUrl":"http://ps-dashboard.es.net/perfsonar-graphs/graphWidget.cgi?url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.252.45&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.252.45&",
                "Max":"9.833072",
                "Standard_Deviation":"0.912399626892372",
                "Average":"8.780211",
                "maUrl":"http://chic-pt1.es.net:8085/esmond/perfsonar/archive",
                "Min":"7.792678"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1468828338,
             "returnCode":0,
             "message":" Average throughput is 8.841Gbps ",
             "returnParams":{
                "Count":"5",
                "graphUrl":"http://ps-dashboard.es.net/perfsonar-graphs/graphWidget.cgi?url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.252.45&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.252.45&",
                "Max":"9.833072",
                "Standard_Deviation":"0.801737163872799",
                "Average":"8.8409196",
                "maUrl":"http://chic-pt1.es.net:8085/esmond/perfsonar/archive",
                "Min":"7.792678"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1468813920,
             "returnCode":0,
             "message":" Average throughput is 9.169Gbps ",
             "returnParams":{
                "Count":"5",
                "graphUrl":"http://ps-dashboard.es.net/perfsonar-graphs/graphWidget.cgi?url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.141&source=198.129.252.45&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.254.14&url=http://chic-pt1.es.net:8085/esmond/perfsonar/archive&dest=198.124.252.154&source=198.129.252.45&",
                "Max":"9.933079",
                "Standard_Deviation":"0.85547823840283",
                "Average":"9.1687506",
                "maUrl":"http://chic-pt1.es.net:8085/esmond/perfsonar/archive",
                "Min":"7.792678"
             },
             "returnCodeCount":0,
             "status":0
          }
       ],
       "globalReport":{
          "stats":[
             96,
             0,
             0,
             0,
             0,
             0
          ],
          "severity":0,
          "problems":[
             {
                "name":"Entire grid has OK status.",
                "severity":0,
                "category":"PERFORMANCE",
                "solutions":[]
             }
          ]
       },
       "rowReport":{
          "stats":[
             8,
             0,
             0,
             0,
             0,
             0
          ],
          "severity":0
       },
       "colReport":{
          "stats":[
             24,
             0,
             0,
             0,
             0,
             0
          ],
          "severity":0
       }
    }

Rescheduling checks
=========================

Request
-------

The request is a HTTP POST that must be authenticated using HTTP BASIC authentication:

+---------------+------------------------------------------------------------------+
| *URL*         | /maddash/admin/schedule                                          |
+---------------+------------------------------------------------------------------+
| *HTTP Method* | POST                                                             |
+---------------+------------------------------------------------------------------+

*JSON Parameters*

+------------------------------+----------------------------------+------------------------------------------------------------------------------------------------------------------------------------+
| Name                         | Type                 | Required  | Value                                                                                                                              |
+==============================+======================+===========+====================================================================================================================================+
| *checkFilters*               | JSON Object          | Yes       | A JSON object with filters that select which  checks will be rescheduled                                                           |
+------------------------------+----------------------+-----------+------------------------------------------------------------------------------------------------------------------------------------+
| *checkFilters.gridName*      | JSON Array or String | No        | A JSON array with the list of grids to select. Undefined or the string `*` means to match every thing.                             |
+------------------------------+----------------------+-----------+------------------------------------------------------------------------------------------------------------------------------------+
| *checkFilters.rowName*       | JSON Array or String | No        | A JSON array with the list of rows to select. Undefined or the string `*` means to match every thing.                              |
+------------------------------+----------------------+-----------+------------------------------------------------------------------------------------------------------------------------------------+
| *checkFilters.columnName*    | JSON Array or String | No        | A JSON array with the list of columns to select. Undefined or the string `*` means to match every thing.                           |
+------------------------------+----------------------+-----------+------------------------------------------------------------------------------------------------------------------------------------+
| *checkFilters.checkName*     | JSON Array or String | No        | A JSON array with the list of checks to select. Undefined or the string `*` means to match every thing.                            |
+------------------------------+----------------------+-----------+------------------------------------------------------------------------------------------------------------------------------------+
| *checkFilters.dimensionName* | JSON Array or String | No        | A JSON array with the list of columns or rows to select. Undefined or the string `*` means to match every thing.                   |
+------------------------------+----------------------+-----------+------------------------------------------------------------------------------------------------------------------------------------+
| *nextCheckTime*              | UNIX timestamp       | No        | A UNIX timestamp indicating when the check should next run                                                                         |
+------------------------------+----------------------+-----------+------------------------------------------------------------------------------------------------------------------------------------+

Example
+++++++

::

    {
        "checkFilters": {
            "gridName": ["BWCTL"],
            "rowName": ["chic-pt1.es.net"],
            "columnName": "*",
            "checkName": "*",
        },
        "nextCheckTime": 1421864236
    }
    
Response
--------

*Type:* application/json

+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| Field                        | Type             | Required| Description                                                                                                   |
+==============================+==================+=========+===============================================================================================================+
| *status*                     | integer          | Yes     | A value of 0 means the operation succeeded. Non-zero means an error occurred.                                 |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *checkUpdateCount*           | integer          | Yes     | The number of checks updated by this operation                                                                |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *message*                    | string           | Yes     | A message describing the result of the operation                                                              |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+

Example
+++++++

::

    {
        "status": 0,
        "checkUpdateCount": 12,
        "message": "Successfully updated 12 checks"
    }