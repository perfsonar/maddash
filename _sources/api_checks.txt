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
          "graphUrl":"https://stats.es.net/perfsonar/serviceTest/cgi-bin/bandwidthGraph.cgi?url=%maUrl&key=%maKeyF&keyR=%maKeyR&dstIP=%dstIP&srcIP=%srcIP&dst=%dstName&src=%srcName&type=TCP&length=2592000",
          "command":"/opt/perfsonar_ps/nagios/bin/check_throughput.pl -u %maUrl -w .5: -c 0: -r 86400 -s %row -d %col",
          "metaDataKeyLookup":"https://stats.es.net/perfsonar/serviceTest/cgi-bin/metaKeyReq.cgi?ma_url=%maUrl&eventType=%event.iperf&srcRaw=%row&dstRaw=%col&protocol=TCP&timeDuration=20",
          "maUrl":{
             "default":"http://%row:8085/perfSONAR_PS/services/pSB"
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
                "graphUrl":"https://stats.es.net/perfsonar/serviceTest/cgi-bin/bandwidthGraph.cgi?url=http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB&key=4ff548de812ae554da5b954e97753749&keyR=b96477f01d57138e1cf4334914d222c0&dstIP=198.124.252.141&srcIP=198.124.252.121&dst=chic-pt1.es.net&src=star-pt1.es.net&type=TCP&length=2592000",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB",
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
                "graphUrl":"https://stats.es.net/perfsonar/serviceTest/cgi-bin/bandwidthGraph.cgi?url=http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB&key=4ff548de812ae554da5b954e97753749&keyR=b96477f01d57138e1cf4334914d222c0&dstIP=198.124.252.141&srcIP=198.124.252.121&dst=chic-pt1.es.net&src=star-pt1.es.net&type=TCP&length=2592000",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB",
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
                "graphUrl":"https://stats.es.net/perfsonar/serviceTest/cgi-bin/bandwidthGraph.cgi?url=http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB&key=4ff548de812ae554da5b954e97753749&keyR=b96477f01d57138e1cf4334914d222c0&dstIP=198.124.252.141&srcIP=198.124.252.121&dst=chic-pt1.es.net&src=star-pt1.es.net&type=TCP&length=2592000",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB",
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
                "graphUrl":"https://stats.es.net/perfsonar/serviceTest/cgi-bin/bandwidthGraph.cgi?url=http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB&key=4ff548de812ae554da5b954e97753749&keyR=b96477f01d57138e1cf4334914d222c0&dstIP=198.124.252.141&srcIP=198.124.252.121&dst=chic-pt1.es.net&src=star-pt1.es.net&type=TCP&length=2592000",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB",
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
                "graphUrl":"https://stats.es.net/perfsonar/serviceTest/cgi-bin/bandwidthGraph.cgi?url=http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB&key=4ff548de812ae554da5b954e97753749&keyR=b96477f01d57138e1cf4334914d222c0&dstIP=198.124.252.141&srcIP=198.124.252.121&dst=chic-pt1.es.net&src=star-pt1.es.net&type=TCP&length=2592000",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB",
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
                "graphUrl":"https://stats.es.net/perfsonar/serviceTest/cgi-bin/bandwidthGraph.cgi?url=http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB&key=4ff548de812ae554da5b954e97753749&keyR=b96477f01d57138e1cf4334914d222c0&dstIP=198.124.252.141&srcIP=198.124.252.121&dst=chic-pt1.es.net&src=star-pt1.es.net&type=TCP&length=2592000",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB",
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
                "graphUrl":"https://stats.es.net/perfsonar/serviceTest/cgi-bin/bandwidthGraph.cgi?url=http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB&key=4ff548de812ae554da5b954e97753749&keyR=b96477f01d57138e1cf4334914d222c0&dstIP=198.124.252.141&srcIP=198.124.252.121&dst=chic-pt1.es.net&src=star-pt1.es.net&type=TCP&length=2592000",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB",
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
                "graphUrl":"https://stats.es.net/perfsonar/serviceTest/cgi-bin/bandwidthGraph.cgi?url=http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB&key=4ff548de812ae554da5b954e97753749&keyR=b96477f01d57138e1cf4334914d222c0&dstIP=198.124.252.141&srcIP=198.124.252.121&dst=chic-pt1.es.net&src=star-pt1.es.net&type=TCP&length=2592000",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"1.1697349988637Gbps",
                "Average":"7.871965Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB",
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
                "graphUrl":"https://stats.es.net/perfsonar/serviceTest/cgi-bin/bandwidthGraph.cgi?url=http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB&key=4ff548de812ae554da5b954e97753749&keyR=b96477f01d57138e1cf4334914d222c0&dstIP=198.124.252.141&srcIP=198.124.252.121&dst=chic-pt1.es.net&src=star-pt1.es.net&type=TCP&length=2592000",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"0.181510755305758Gbps",
                "Average":"8.4896975Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB",
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
                "graphUrl":"https://stats.es.net/perfsonar/serviceTest/cgi-bin/bandwidthGraph.cgi?url=http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB&key=4ff548de812ae554da5b954e97753749&keyR=b96477f01d57138e1cf4334914d222c0&dstIP=198.124.252.141&srcIP=198.124.252.121&dst=chic-pt1.es.net&src=star-pt1.es.net&type=TCP&length=2592000",
                "Max":"8.67244Gbps",
                "Standard_Deviation":"0.181510755305758Gbps",
                "Average":"8.4896975Gbps",
                "maUrl":"http://star-pt1.es.net:8085/perfSONAR_PS/services/pSB",
                "Min":"8.2796Gbps"
             },
             "returnCodeCount":0,
             "status":0
          }
       ]
    }
