*****
Grids
*****

Retrieving the list of grids
============================

You can get a list of grids, not organized into dashboard by sending an HTTP GET to the /maddash/grids URL. See below for full information.

Request
-------

The request is a HTTP GET to the grids URL. There are currently no parameters to this request. See below for full summary.

+---------------+----------------+
| *URL*         | /maddash/grids |
+---------------+----------------+
| *HTTP Method* | GET            |
+---------------+----------------+


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

**Type:** application/json

+-----------------------+-----------------+----------+----------------------------------------+
| Field                 | Type            | Required | Description                            |
+=======================+=================+==========+========================================+ 
| *grids*               | Array of object | Yes      | Array of grid objects                  |
+-----------------------+-----------------+----------+----------------------------------------+ 
| *grids[n].name*       | string          | Yes      | The name of the grid                   | 
+-----------------------+-----------------+----------+----------------------------------------+
| *grids[n].uri*        | string          | Yes      | The URI where the grid can be accessed | 
+-----------------------+-----------------+----------+----------------------------------------+

Example
+++++++

::

    {
       "grids":[
          {
             "name":"ESnet OWAMP Tests",
             "uri":"/maddash/ESnet+OWAMP+Tests"
          },
          {
             "name":"ESnet BWCTL Tests - Other Sites",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites"
          },
          {
             "name":"ESnet to GEANT OWAMP Tests",
             "uri":"/maddash/ESnet+to+GEANT+OWAMP+Tests"
          },
          {
             "name":"ESnet to GEANT BWCTL Tests",
             "uri":"/maddash/ESnet+to+GEANT+BWCTL+Tests"
          },
          {
             "name":"ESnet BWCTL Tests - Large Sites",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Large+Sites"
          }
       ]
    }


Retrieving a single grid
========================

Request
-------

The request is a HTTP GET to the grid URL. There are currently no parameters to this request. See below for full summary:

+---------------+----------------------------+
| *URL*         | /maddash/grids/<grid-name> |
+---------------+----------------------------+
| *HTTP Method* | GET                        |
+---------------+----------------------------+

Response
--------

*Type:* application/json

+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| Field                           | Type                              | Required | Description                                                                                                                                                                                                                                       | 
+=================================+===================================+==========+===================================================================================================================================================================================================================================================+
|  *name*                         |  string                           |  Yes     | The name of the dashboard.                                                                                                                                                                                                                        |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *statusLabels*                 |  array of strings                 |  Yes     | Descriptions of what each status means in the context of this grid. The index of the array corresponds to the :ref:`status-codes` described. If an item in the array is *null* then it is impossible to reach that state.                         |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *lastUpdateTime*               |  long                             |  Yes     | A Unix timestamp (in seconds) indicating when the most recent check was run                                                                                                                                                                       |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *report*                       |  :ref:`api_reports-report_object` |  No      | A report object listing details of any report patterns that match the grid                                                                                                                                                                        |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *rows*                         |  array of objects                 |  Yes     | The ordered list of rows in the dashboard                                                                                                                                                                                                         |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *rows[n].name*                 |  string                           |  Yes     | The name of the row                                                                                                                                                                                                                               |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *rows[n].props*                |  object                           |  Yes     | A list of key/value pairs that further describe the row. The keys and values are taken from :ref:`group-member-props`.                                                                                                                            |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *rows[n].uri*                  |  string                           |  Yes     | The uri of the row                                                                                                                                                                                                                                |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *columnNames*                  |  array of strings                 |  Yes     | A ordered list of column names                                                                                                                                                                                                                    |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *columnProps*                  |  array of objects                 |  Yes     | A ordered list of objects containing key value pairs that further describe columns. The keys and values are taken from :ref:`group-member-props`.                                                                                                 |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *checkNames*                   |  array of strings                 |  Yes     | The ordered list of check names                                                                                                                                                                                                                   |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *grid*                         |  3-dimenisional array of objects  |  Yes     | The full grid. The first dimension of the array is the rows, the second dimension is the array of cells in row, and the third is the array of checks. The order of each MUST match the order in the *rows*, *columnNames* and *checkNames* field. |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *grid[n]*                      |  2-dimenional array               |  Yes     | A row in the grid                                                                                                                                                                                                                                 |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *grid[n][m]*                   |  2-dimenional array               |  Yes     | A cell in the grid. If null then no checks are configured for this cell.                                                                                                                                                                          |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *grid[n][m][l]*                |  array or objects                 |  Yes     | A check in the grid                                                                                                                                                                                                                               |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *grid[n][m][l]*                |  array or objects                 |  Yes     | A check in the grid                                                                                                                                                                                                                               |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *grid[n][m][l].prevCheckTime*  |  long                             |  Yes     | The Unix timestamp (in seconds) when this check was last run                                                                                                                                                                                      |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *grid[n][m][l].message*        |  string                           |  Yes     | A human-readable message describing the last result of the check                                                                                                                                                                                  |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *grid[n][m][l].status*         |  int                              |  Yes     | The status of the last result of a check. See the :ref:`status-codes` table.                                                                                                                                                                      |
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
|  *grid[n][m][l].uri*            |  string                           |  Yes     | The URI where details on the check can be retrieved                                                                                                                                                                                               | 
+---------------------------------+-----------------------------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

.. _status-codes:

Status-Codes
++++++++++++

+-------------+--------------------------------------------------------------------------------------------------------------------------+
| Status Code | Description                                                                                                              |
+=============+==========================================================================================================================+
| *0*         | The check passed.                                                                                                        |
+-------------+--------------------------------------------------------------------------------------------------------------------------+
| *1*         | The check is in a warning state                                                                                          |
+-------------+--------------------------------------------------------------------------------------------------------------------------+
| *2*         | The check is in critical state                                                                                           |
+-------------+--------------------------------------------------------------------------------------------------------------------------+
| *3*         | The check is in an unknown state. This should be returned if data cannot be retrieved or the check has an internal error |
+-------------+--------------------------------------------------------------------------------------------------------------------------+
| *4*         | The check has not yet run                                                                                                |
+-------------+--------------------------------------------------------------------------------------------------------------------------+
| *5*         | The check is down due to a scheduled event such as a maintenance window                                                  |
+-------------+--------------------------------------------------------------------------------------------------------------------------+
| *>5*        | Custom states that may be defined by an individual check                                                                 |
+-------------+--------------------------------------------------------------------------------------------------------------------------+


Example
+++++++

::

    {
       "name":"ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing",
       "statusLabels":[
          "Throughput >= 5000Mbps",
          "Throughput < 5000Mbps",
          "Throughput <= 1000Mbps",
          "Unable to retrieve data",
          "Check has not yet run"
       ],
       "lastUpdateTime":1468948423,
       "columnNames":[
          "chic-pt1.es.net",
          "hous-pt1.es.net",
          "sunn-pt1.es.net",
          "wash-pt1.es.net"
       ],
       "columnProps":[
          {
             "added_by_mesh_agent":"yes",
             "label":"chic-pt1.es.net"
          },
          {
             "added_by_mesh_agent":"yes",
             "label":"hous-pt1.es.net"
          },
          {
             "added_by_mesh_agent":"yes",
             "label":"sunn-pt1.es.net"
          },
          {
             "added_by_mesh_agent":"yes",
             "label":"wash-pt1.es.net"
          }
       ],
       "checkNames":[
          "Throughput",
          "Throughput Reverse"
       ],
       "grid":[
          [
             [
                {
                   "message":" Average throughput is 9.929Gbps ",
                   "status":0,
                   "prevCheckTime":1468945208,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/albq-pt1.es.net/chic-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 8.731Gbps ",
                   "status":0,
                   "prevCheckTime":1468945101,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/albq-pt1.es.net/chic-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 7.584Gbps ",
                   "status":0,
                   "prevCheckTime":1468945102,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/albq-pt1.es.net/hous-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.932Gbps ",
                   "status":0,
                   "prevCheckTime":1468945219,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/albq-pt1.es.net/hous-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 8.596Gbps ",
                   "status":0,
                   "prevCheckTime":1468945157,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/albq-pt1.es.net/sunn-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 8.732Gbps ",
                   "status":0,
                   "prevCheckTime":1468945906,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/albq-pt1.es.net/sunn-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.898Gbps ",
                   "status":0,
                   "prevCheckTime":1468945168,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/albq-pt1.es.net/wash-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 7.300Gbps ",
                   "status":0,
                   "prevCheckTime":1468945324,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/albq-pt1.es.net/wash-pt1.es.net/Throughput+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 9.597Gbps ",
                   "status":0,
                   "prevCheckTime":1468942249,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/aofa-pt1.es.net/chic-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.435Gbps ",
                   "status":0,
                   "prevCheckTime":1468945344,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/aofa-pt1.es.net/chic-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.269Gbps ",
                   "status":0,
                   "prevCheckTime":1468942186,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/aofa-pt1.es.net/hous-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.829Gbps ",
                   "status":0,
                   "prevCheckTime":1468945350,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/aofa-pt1.es.net/hous-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 8.931Gbps ",
                   "status":0,
                   "prevCheckTime":1468945351,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/aofa-pt1.es.net/sunn-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.207Gbps ",
                   "status":0,
                   "prevCheckTime":1468946160,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/aofa-pt1.es.net/sunn-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.781Gbps ",
                   "status":0,
                   "prevCheckTime":1468942233,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/aofa-pt1.es.net/wash-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 8.686Gbps ",
                   "status":0,
                   "prevCheckTime":1468945379,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/aofa-pt1.es.net/wash-pt1.es.net/Throughput+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 8.691Gbps ",
                   "status":0,
                   "prevCheckTime":1468945344,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/atla-pt1.es.net/chic-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 8.758Gbps ",
                   "status":0,
                   "prevCheckTime":1468945278,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/atla-pt1.es.net/chic-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.065Gbps ",
                   "status":0,
                   "prevCheckTime":1468945342,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/atla-pt1.es.net/hous-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 8.644Gbps ",
                   "status":0,
                   "prevCheckTime":1468945288,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/atla-pt1.es.net/hous-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 8.818Gbps ",
                   "status":0,
                   "prevCheckTime":1468945948,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/atla-pt1.es.net/sunn-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 7.420Gbps ",
                   "status":0,
                   "prevCheckTime":1468948423,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/atla-pt1.es.net/sunn-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.662Gbps ",
                   "status":0,
                   "prevCheckTime":1468945345,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/atla-pt1.es.net/wash-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 8.768Gbps ",
                   "status":0,
                   "prevCheckTime":1468945324,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/atla-pt1.es.net/wash-pt1.es.net/Throughput+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 9.780Gbps ",
                   "status":0,
                   "prevCheckTime":1468944616,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bois-pt1.es.net/chic-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 8.555Gbps ",
                   "status":0,
                   "prevCheckTime":1468945225,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bois-pt1.es.net/chic-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 8.461Gbps ",
                   "status":0,
                   "prevCheckTime":1468942357,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bois-pt1.es.net/hous-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 8.916Gbps ",
                   "status":0,
                   "prevCheckTime":1468945268,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bois-pt1.es.net/hous-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 8.906Gbps ",
                   "status":0,
                   "prevCheckTime":1468944840,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bois-pt1.es.net/sunn-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.253Gbps ",
                   "status":0,
                   "prevCheckTime":1468946139,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bois-pt1.es.net/sunn-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 7.702Gbps ",
                   "status":0,
                   "prevCheckTime":1468944563,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bois-pt1.es.net/wash-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 6.525Gbps ",
                   "status":0,
                   "prevCheckTime":1468945154,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bois-pt1.es.net/wash-pt1.es.net/Throughput+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 9.923Gbps ",
                   "status":0,
                   "prevCheckTime":1468945056,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bost-pt1.es.net/chic-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.086Gbps ",
                   "status":0,
                   "prevCheckTime":1468945112,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bost-pt1.es.net/chic-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 8.548Gbps ",
                   "status":0,
                   "prevCheckTime":1468945151,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bost-pt1.es.net/hous-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.818Gbps ",
                   "status":0,
                   "prevCheckTime":1468945215,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bost-pt1.es.net/hous-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 8.520Gbps ",
                   "status":0,
                   "prevCheckTime":1468945161,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bost-pt1.es.net/sunn-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.006Gbps ",
                   "status":0,
                   "prevCheckTime":1468945845,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bost-pt1.es.net/sunn-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 8.073Gbps ",
                   "status":0,
                   "prevCheckTime":1468944956,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bost-pt1.es.net/wash-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.921Gbps ",
                   "status":0,
                   "prevCheckTime":1468944902,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bost-pt1.es.net/wash-pt1.es.net/Throughput+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 8.182Gbps ",
                   "status":0,
                   "prevCheckTime":1468945326,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/denv-pt1.es.net/chic-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.066Gbps ",
                   "status":0,
                   "prevCheckTime":1468945283,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/denv-pt1.es.net/chic-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.845Gbps ",
                   "status":0,
                   "prevCheckTime":1468945279,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/denv-pt1.es.net/hous-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 8.695Gbps ",
                   "status":0,
                   "prevCheckTime":1468945282,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/denv-pt1.es.net/hous-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.484Gbps ",
                   "status":0,
                   "prevCheckTime":1468945285,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/denv-pt1.es.net/sunn-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.584Gbps ",
                   "status":0,
                   "prevCheckTime":1468945765,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/denv-pt1.es.net/sunn-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 7.984Gbps ",
                   "status":0,
                   "prevCheckTime":1468945226,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/denv-pt1.es.net/wash-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 6.703Gbps ",
                   "status":0,
                   "prevCheckTime":1468942233,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/denv-pt1.es.net/wash-pt1.es.net/Throughput+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 8.470Gbps ",
                   "status":0,
                   "prevCheckTime":1468945326,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/elpa-pt1.es.net/chic-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 7.880Gbps ",
                   "status":0,
                   "prevCheckTime":1468945334,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/elpa-pt1.es.net/chic-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.883Gbps ",
                   "status":0,
                   "prevCheckTime":1468945290,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/elpa-pt1.es.net/hous-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.929Gbps ",
                   "status":0,
                   "prevCheckTime":1468945324,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/elpa-pt1.es.net/hous-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.909Gbps ",
                   "status":0,
                   "prevCheckTime":1468945282,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/elpa-pt1.es.net/sunn-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.924Gbps ",
                   "status":0,
                   "prevCheckTime":1468946097,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/elpa-pt1.es.net/sunn-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 8.359Gbps ",
                   "status":0,
                   "prevCheckTime":1468945283,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/elpa-pt1.es.net/wash-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 6.837Gbps ",
                   "status":0,
                   "prevCheckTime":1468945327,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/elpa-pt1.es.net/wash-pt1.es.net/Throughput+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 9.848Gbps ",
                   "status":0,
                   "prevCheckTime":1468945354,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/kans-pt1.es.net/chic-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.890Gbps ",
                   "status":0,
                   "prevCheckTime":1468945344,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/kans-pt1.es.net/chic-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.857Gbps ",
                   "status":0,
                   "prevCheckTime":1468945269,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/kans-pt1.es.net/hous-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.932Gbps ",
                   "status":0,
                   "prevCheckTime":1468945353,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/kans-pt1.es.net/hous-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 8.633Gbps ",
                   "status":0,
                   "prevCheckTime":1468945227,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/kans-pt1.es.net/sunn-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.088Gbps ",
                   "status":0,
                   "prevCheckTime":1468945750,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/kans-pt1.es.net/sunn-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.781Gbps ",
                   "status":0,
                   "prevCheckTime":1468945323,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/kans-pt1.es.net/wash-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 8.195Gbps ",
                   "status":0,
                   "prevCheckTime":1468945278,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/kans-pt1.es.net/wash-pt1.es.net/Throughput+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 9.823Gbps ",
                   "status":0,
                   "prevCheckTime":1468945207,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/nash-pt1.es.net/chic-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.379Gbps ",
                   "status":0,
                   "prevCheckTime":1468945103,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/nash-pt1.es.net/chic-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 8.525Gbps ",
                   "status":0,
                   "prevCheckTime":1468945287,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/nash-pt1.es.net/hous-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.936Gbps ",
                   "status":0,
                   "prevCheckTime":1468945222,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/nash-pt1.es.net/hous-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 7.453Gbps ",
                   "status":0,
                   "prevCheckTime":1468945213,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/nash-pt1.es.net/sunn-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.360Gbps ",
                   "status":0,
                   "prevCheckTime":1468946139,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/nash-pt1.es.net/sunn-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.443Gbps ",
                   "status":0,
                   "prevCheckTime":1468945347,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/nash-pt1.es.net/wash-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.740Gbps ",
                   "status":0,
                   "prevCheckTime":1468945285,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/nash-pt1.es.net/wash-pt1.es.net/Throughput+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 9.832Gbps ",
                   "status":0,
                   "prevCheckTime":1468944463,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/newy-pt1.es.net/chic-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 7.788Gbps ",
                   "status":0,
                   "prevCheckTime":1468944899,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/newy-pt1.es.net/chic-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 8.009Gbps ",
                   "status":0,
                   "prevCheckTime":1468944565,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/newy-pt1.es.net/hous-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.233Gbps ",
                   "status":0,
                   "prevCheckTime":1468944216,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/newy-pt1.es.net/hous-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.939Gbps ",
                   "status":0,
                   "prevCheckTime":1468944491,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/newy-pt1.es.net/sunn-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 8.970Gbps ",
                   "status":0,
                   "prevCheckTime":1468945770,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/newy-pt1.es.net/sunn-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.202Gbps ",
                   "status":0,
                   "prevCheckTime":1468944903,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/newy-pt1.es.net/wash-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 8.925Gbps ",
                   "status":0,
                   "prevCheckTime":1468944492,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/newy-pt1.es.net/wash-pt1.es.net/Throughput+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 7.875Gbps ",
                   "status":0,
                   "prevCheckTime":1468945051,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/pnwg-pt1.es.net/chic-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 7.316Gbps ",
                   "status":0,
                   "prevCheckTime":1468945092,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/pnwg-pt1.es.net/chic-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 6.421Gbps ",
                   "status":0,
                   "prevCheckTime":1468945096,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/pnwg-pt1.es.net/hous-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.232Gbps ",
                   "status":0,
                   "prevCheckTime":1468945169,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/pnwg-pt1.es.net/hous-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.285Gbps ",
                   "status":0,
                   "prevCheckTime":1468945093,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/pnwg-pt1.es.net/sunn-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.925Gbps ",
                   "status":0,
                   "prevCheckTime":1468946010,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/pnwg-pt1.es.net/sunn-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 6.699Gbps ",
                   "status":0,
                   "prevCheckTime":1468945804,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/pnwg-pt1.es.net/wash-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 6.746Gbps ",
                   "status":0,
                   "prevCheckTime":1468945265,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/pnwg-pt1.es.net/wash-pt1.es.net/Throughput+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 9.509Gbps ",
                   "status":0,
                   "prevCheckTime":1468944886,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/star-pt1.es.net/chic-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 9.644Gbps ",
                   "status":0,
                   "prevCheckTime":1468944484,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/star-pt1.es.net/chic-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 7.648Gbps ",
                   "status":0,
                   "prevCheckTime":1468944373,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/star-pt1.es.net/hous-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 8.551Gbps ",
                   "status":0,
                   "prevCheckTime":1468944348,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/star-pt1.es.net/hous-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 6.960Gbps ",
                   "status":0,
                   "prevCheckTime":1468944346,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/star-pt1.es.net/sunn-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 7.628Gbps ",
                   "status":0,
                   "prevCheckTime":1468945753,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/star-pt1.es.net/sunn-pt1.es.net/Throughput+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 8.174Gbps ",
                   "status":0,
                   "prevCheckTime":1468944478,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/star-pt1.es.net/wash-pt1.es.net/Throughput"
                },
                {
                   "message":" Average throughput is 8.064Gbps ",
                   "status":0,
                   "prevCheckTime":1468944981,
                   "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/star-pt1.es.net/wash-pt1.es.net/Throughput+Reverse"
                }
             ]
          ]
       ],
       "rows":[
          {
             "name":"albq-pt1.es.net",
             "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/albq-pt1.es.net",
             "props":{
                "added_by_mesh_agent":"yes",
                "label":"albq-pt1.es.net"
             }
          },
          {
             "name":"aofa-pt1.es.net",
             "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/aofa-pt1.es.net",
             "props":{
                "added_by_mesh_agent":"yes",
                "label":"aofa-pt1.es.net"
             }
          },
          {
             "name":"atla-pt1.es.net",
             "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/atla-pt1.es.net",
             "props":{
                "added_by_mesh_agent":"yes",
                "label":"atla-pt1.es.net"
             }
          },
          {
             "name":"bois-pt1.es.net",
             "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bois-pt1.es.net",
             "props":{
                "added_by_mesh_agent":"yes",
                "label":"bois-pt1.es.net"
             }
          },
          {
             "name":"bost-pt1.es.net",
             "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/bost-pt1.es.net",
             "props":{
                "added_by_mesh_agent":"yes",
                "label":"bost-pt1.es.net"
             }
          },
          {
             "name":"denv-pt1.es.net",
             "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/denv-pt1.es.net",
             "props":{
                "added_by_mesh_agent":"yes",
                "label":"denv-pt1.es.net"
             }
          },
          {
             "name":"elpa-pt1.es.net",
             "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/elpa-pt1.es.net",
             "props":{
                "added_by_mesh_agent":"yes",
                "label":"elpa-pt1.es.net"
             }
          },
          {
             "name":"kans-pt1.es.net",
             "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/kans-pt1.es.net",
             "props":{
                "added_by_mesh_agent":"yes",
                "label":"kans-pt1.es.net"
             }
          },
          {
             "name":"nash-pt1.es.net",
             "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/nash-pt1.es.net",
             "props":{
                "added_by_mesh_agent":"yes",
                "label":"nash-pt1.es.net"
             }
          },
          {
             "name":"newy-pt1.es.net",
             "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/newy-pt1.es.net",
             "props":{
                "added_by_mesh_agent":"yes",
                "label":"newy-pt1.es.net"
             }
          },
          {
             "name":"pnwg-pt1.es.net",
             "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/pnwg-pt1.es.net",
             "props":{
                "added_by_mesh_agent":"yes",
                "label":"pnwg-pt1.es.net"
             }
          },
          {
             "name":"star-pt1.es.net",
             "uri":"/maddash/grids/ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing/star-pt1.es.net",
             "props":{
                "added_by_mesh_agent":"yes",
                "label":"star-pt1.es.net"
             }
          }
       ],
       "report":{
          "mesh":{
             "name":"ESnet - 100G ESnet Hub to 100G ESnet Hub Throughput Testing"
          },
          "global":{
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
                   "solutions":[
    
               ]
                }
             ]
          },
          "sites":{
             "star-pt1.es.net":{
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
             "hous-pt1.es.net":{
                "stats":[
                   24,
                   0,
                   0,
                   0,
                   0,
                   0
                ],
                "severity":0
             },
             "bois-pt1.es.net":{
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
             "sunn-pt1.es.net":{
                "stats":[
                   24,
                   0,
                   0,
                   0,
                   0,
                   0
                ],
                "severity":0
             },
             "wash-pt1.es.net":{
                "stats":[
                   24,
                   0,
                   0,
                   0,
                   0,
                   0
                ],
                "severity":0
             },
             "bost-pt1.es.net":{
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
             "kans-pt1.es.net":{
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
             "atla-pt1.es.net":{
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
             "pnwg-pt1.es.net":{
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
             "nash-pt1.es.net":{
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
             "aofa-pt1.es.net":{
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
             "elpa-pt1.es.net":{
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
             "chic-pt1.es.net":{
                "stats":[
                   24,
                   0,
                   0,
                   0,
                   0,
                   0
                ],
                "severity":0
             },
             "newy-pt1.es.net":{
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
             "albq-pt1.es.net":{
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
             "denv-pt1.es.net":{
                "stats":[
                   8,
                   0,
                   0,
                   0,
                   0,
                   0
                ],
                "severity":0
             }
          }
       }
    }