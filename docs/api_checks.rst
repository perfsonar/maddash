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

+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| Field                        | Type             | Required| Description                                                                                                   |
+==============================+==================+=========+===============================================================================================================+
| *gridName*                   | string           | Yes     | The name of the grid that contains this check                                                                 |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *rowName*                    | string           | Yes     | The name of the row that contains this check                                                                  |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *colName*                    | string           | Yes     | The name of the column that contains this check                                                               |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *checkName*                  | string           | Yes     | The name of the check                                                                                         |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *description*                | string           | Yes     | A description of the check                                                                                    |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *prevCheckTime*              | long             | Yes     | A Unix timestamp (in seconds) when the check previously ran                                                   |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *nextCheckTime*              | long             | Yes     | A Unix timestamp (in seconds) when the check will run next                                                    |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+  
| *status*                     | int              | Yes     | The current status of the check. See the :ref:`status-codes` table for values.                                |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+ 
| *returnCode*                 | int              | Yes     | The current status of the check. See the :ref:`status-codes` table for values.                                |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+ 
| *message*                    | string           | Yes     | The last message returned by the check                                                                        |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *type*                       | string           | Yes     | The type of check                                                                                             |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *params*                     | Object           | Yes     | Type specific check configuration parameters                                                                  |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *returnCodeCount*            | int              | Yes     | The number of times a the returnCode was seen if its different than the current status. 0 if same as status.  |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *checkInterval*              | int              | Yes     | The time in between checks if the returnCode and status are the same                                          |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *retryInterval*              | int              | Yes     | The time in between checks if the status and returnCode are different                                         |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *retryAttempts*              | int              | Yes     | The number of times a returnCode different than the status must be seen before changing the status            |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *historyPageCount*           | int              | Yes     | The number of pages available for the history. Assumes the current result set size.                           |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *historyResultsPerPage*      | int              | Yes     | The maximum number of results per page. Echos the resultsPerPage URL parameter.                               |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *history*                    | array of objects | Yes     | An array of previous results for this check. If page is 0 then teh first check is the most recent check run   |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *history[n].time*            | long             | Yes     | A Unix timestamp (seconds) when the check was run.                                                            |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *history[n].returnCode*      | int              | Yes     | The return code of this check. See the :ref:`status-codes` table for values.                                  |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *history[n].message*         | string           | Yes     | The message returned by this check                                                                            |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *history[n].returnParams*    | object           | Yes     | Type specific parameters returned by this check                                                               |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *history[n].returnCodeCount* | int              | Yes     | The number of times this return code was seen, if different than the status. 0 if same as status.             |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+
| *history[n].status*          | int              | Yes     | The status of the check at the time this check was run                                                        |
+------------------------------+------------------+---------+---------------------------------------------------------------------------------------------------------------+

Example
+++++++

::

    {
       "gridName":"ESnet BWCTL Tests - Other Sites",
       "rowName":"star-pt1.es.net",
       "colName":"chic-pt1.es.net",
       "checkName":"BWCTL 500Mbps",
       "description":"Throughput from star-pt1.es.net to chic-pt1.es.net (according to star-pt1.es.net MA)",
       "prevCheckTime":1334712021,
       "nextCheckTime":1334715621,
       "status":0,
       "returnCode":0,
       "message":" Average throughput is 7.871965Gbps ",
       "returnCodeCount":0,
       "type":"net.es.maddash.checks.PSNagiosCheck",
       "params":{
          "graphUrl":"/perfsonar-graphs/graphWidget.cgi?url=%maUrl&dest=%dest&source=%source&dst=%dstName&src=%srcName&protocol=tcp",
          "command":"/usr/lib/nagios/bin/check_throughput.pl -u %maUrl -w .5: -c 0: -r 86400 -s %row -d %col",
          "maUrl":{
             "default":"http://%row:8085/perfsonar/esmond/archive"
          }
       },
       "checkInterval":3600,
       "retryInterval":900,
       "retryAttempts":3,
       "historyPageCount":14,
       "historyResultsPerPage":10,
       "history":[
          {
             "time":1334712021,
             "returnCode":0,
             "message":" Average throughput is 7.871965Gbps ",
             "returnParams":{
                "Count":"4",
                "graphUrl":"/perfsonar-graphs/graphWidget.cgi?url=http://star-pt1.es.net:8085/perfsonar/esmond/archive&dest=198.124.252.141&source=198.124.252.121&protocol=tcp",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfsonar/esmond/archive",
                "Min":"6.13474Gbps"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1334708390,
             "returnCode":0,
             "message":" Average throughput is 7.871965Gbps ",
             "returnParams":{
                "Count":"4",
                "graphUrl":"/perfsonar-graphs/graphWidget.cgi?url=http://star-pt1.es.net:8085/perfsonar/esmond/archive&dest=198.124.252.141&source=198.124.252.121&protocol=tcp",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfsonar/esmond/archive",
                "Min":"6.13474Gbps"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1334704786,
             "returnCode":0,
             "message":" Average throughput is 7.871965Gbps ",
             "returnParams":{
                "Count":"4",
                "graphUrl":"/perfsonar-graphs/graphWidget.cgi?url=http://star-pt1.es.net:8085/perfsonar/esmond/archive&dest=198.124.252.141&source=198.124.252.121&protocol=tcp",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfsonar/esmond/archive",
                "Min":"6.13474Gbps"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1334701166,
             "returnCode":0,
             "message":" Average throughput is 7.871965Gbps ",
             "returnParams":{
                "Count":"4",
                "graphUrl":"/perfsonar-graphs/graphWidget.cgi?url=http://star-pt1.es.net:8085/perfsonar/esmond/archive&dest=198.124.252.141&source=198.124.252.121&protocol=tcp",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfsonar/esmond/archive",
                "Min":"6.13474Gbps"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1334697542,
             "returnCode":0,
             "message":" Average throughput is 7.871965Gbps ",
             "returnParams":{
                "Count":"4",
                "graphUrl":"/perfsonar-graphs/graphWidget.cgi?url=http://star-pt1.es.net:8085/perfsonar/esmond/archive&dest=198.124.252.141&source=198.124.252.121&protocol=tcp",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfsonar/esmond/archive",
                "Min":"6.13474Gbps"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1334693924,
             "returnCode":0,
             "message":" Average throughput is 7.871965Gbps ",
             "returnParams":{
                "Count":"4",
                "graphUrl":"/perfsonar-graphs/graphWidget.cgi?url=http://star-pt1.es.net:8085/perfsonar/esmond/archive&dest=198.124.252.141&source=198.124.252.121&protocol=tcp",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfsonar/esmond/archive",
                "Min":"6.13474Gbps"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1334690304,
             "returnCode":0,
             "message":" Average throughput is 7.871965Gbps ",
             "returnParams":{
                "Count":"4",
                "graphUrl":"/perfsonar-graphs/graphWidget.cgi?url=http://star-pt1.es.net:8085/perfsonar/esmond/archive&dest=198.124.252.141&source=198.124.252.121&protocol=tcp",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfsonar/esmond/archive",
                "Min":"6.13474Gbps"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1334686678,
             "returnCode":0,
             "message":" Average throughput is 7.871965Gbps ",
             "returnParams":{
                "Count":"4",
                "graphUrl":"/perfsonar-graphs/graphWidget.cgi?url=http://star-pt1.es.net:8085/perfsonar/esmond/archive&dest=198.124.252.141&source=198.124.252.121&protocol=tcp",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfsonar/esmond/archive",
                "Min":"6.13474Gbps"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1334683054,
             "returnCode":0,
             "message":" Average throughput is 8.4896975Gbps ",
             "returnParams":{
                "Count":"4",
                "graphUrl":"/perfsonar-graphs/graphWidget.cgi?url=http://star-pt1.es.net:8085/perfsonar/esmond/archive&dest=198.124.252.141&source=198.124.252.121&protocol=tcp",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"0.181510755305758Gbps",
                "Average":"8.4896975Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfsonar/esmond/archive",
                "Min":"8.2796Gbps"
             },
             "returnCodeCount":0,
             "status":0
          },
          {
             "time":1334679450,
             "returnCode":0,
             "message":" Average throughput is 8.4896975Gbps ",
             "returnParams":{
                "Count":"4",
                "graphUrl":"/perfsonar-graphs/graphWidget.cgi?url=http://star-pt1.es.net:8085/perfsonar/esmond/archive&dest=198.124.252.141&source=198.124.252.121&protocol=tcp",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"0.181510755305758Gbps",
                "Average":"8.4896975Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfsonar/esmond/archive",
                "Min":"8.2796Gbps"
             },
             "returnCodeCount":0,
             "status":0
          }
       ]
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
