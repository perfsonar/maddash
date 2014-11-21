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


Example
+++++++

::

    {
       "name":"ESnet BWCTL Tests - Other Sites",
       "statusLabels":[
          "Throughput > 500Mbps",
          "Throughput 100-500Mbps",
          "Throughput < 100Mbps",
          "Unable to retrieve data",
          "Check has not yet run"
       ]
       "rows":[
          {
             "name":"albu-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/albu-pt1.es.net",
             "props":{ "pstoolkiturl": "http://albu-pt1.es.net"}
          },
          {
             "name":"ameslab-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/ameslab-pt1.es.net",
             "props":{}
          },
          {
             "name":"aofa-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/aofa-pt1.es.net",
             "props":{}
          },
          {
             "name":"atla-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/atla-pt1.es.net",
             "props":{}
          },
          {
             "name":"bois-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bois-pt1.es.net",
             "props":{}
          },
          {
             "name":"bost-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bost-pt1.es.net",
             "props":{}
          },
          {
             "name":"clev-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/clev-pt1.es.net",
             "props":{}
          },
          {
             "name":"denv-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/denv-pt1.es.net",
             "props":{}
          },
          {
             "name":"doe-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/doe-pt1.es.net",
             "props":{}
          },
          {
             "name":"elpa-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/elpa-pt1.es.net",
             "props":{}
          },
          {
             "name":"eqx-ash-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-ash-pt1.es.net",
             "props":{}
          },
          {
             "name":"eqx-chi-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-chi-pt1.es.net",
             "props":{}
          },
          {
             "name":"eqx-sj-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-sj-pt1.es.net",
             "props":{}
          },
          {
             "name":"forr-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/forr-pt1.es.net",
             "props":{}
          },
          {
             "name":"inl-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/inl-pt1.es.net",
             "props":{}
          },
          {
             "name":"kans-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/kans-pt1.es.net",
             "props":{}
          },
          {
             "name":"lasv-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lasv-pt1.es.net",
             "props":{}
          },
          {
             "name":"lvk-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lvk-pt1.es.net",
             "props":{}
          },
          {
             "name":"newy-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/newy-pt1.es.net",
             "props":{}
          },
          {
             "name":"nrel-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/nrel-pt1.es.net",
             "props":{}
          },
          {
             "name":"orau-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/orau-pt1.es.net",
             "props":{}
          },
          {
             "name":"osti-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/osti-pt1.es.net",
             "props":{}
          },
          {
             "name":"paix-pa-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/paix-pa-pt1.es.net",
             "props":{}
          },
          {
             "name":"pantex-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/pantex-pt1.es.net",
             "props":{}
          },
          {
             "name":"snla-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/snla-pt1.es.net",
             "props":{}
          },
          {
             "name":"srs-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/srs-pt1.es.net",
             "props":{}
          },
          {
             "name":"star-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net",
             "props":{}
          }
       ],
       "columnNames":[
          "chic-pt1.es.net",
          "hous-pt1.es.net",
          "sunn-pt1.es.net",
          "wash-pt1.es.net"
       ],
       "columnProps":[
          { "pstoolkiturl": "http://chic-pt1.es.net" },
          {},
          {},
          {}
       ],
       "checkNames":[
          "BWCTL 500Mbps",
          "BWCTL 500Mbps Reverse"
       ],
       "grid":[
          [
             [
                {
                   "message":" Average throughput is 4.17823Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/albu-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 2.0626646Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/albu-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=albu-pt1.es.net dst=hous-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/albu-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.009612776Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/albu-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 4.020105Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/albu-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00592367Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/albu-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=albu-pt1.es.net dst=wash-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/albu-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.003148915Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/albu-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 0.634649666666667Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/ameslab-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.938077666666667Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/ameslab-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=ameslab-pt1.es.net dst=hous-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/ameslab-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00716801333333333Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/ameslab-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.597662Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/ameslab-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.004773255Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/ameslab-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=ameslab-pt1.es.net dst=wash-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/ameslab-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.3389416Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/ameslab-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 0.00494619666666667Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/aofa-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 2.58350125Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/aofa-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.00409102Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/aofa-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.004451315Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/aofa-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.00241175666666667Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/aofa-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00267422Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/aofa-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.0099413725Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/aofa-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 8.73781Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/aofa-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 3.5751425Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/atla-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00781735666666667Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/atla-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 4.38536Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/atla-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00812078Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/atla-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 4.23377Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/atla-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0028477425Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/atla-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 4.3594175Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/atla-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0149236666666667Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/atla-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 3.73704333333333Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bois-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 2.09773Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bois-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 4.27852Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bois-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.998152Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bois-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 4.67863Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bois-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 3.06777Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bois-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 4.3927725Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bois-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0027046275Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bois-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 3.791905Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bost-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 2.83113966666667Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bost-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 3.76754Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bost-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00379118Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bost-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 3.55229Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bost-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0025877Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bost-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 3.41688Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bost-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 5.04567Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/bost-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 4.54274625Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/clev-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 7.603265Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/clev-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=clev-pt1.es.net dst=hous-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/clev-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.005621675Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/clev-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=clev-pt1.es.net dst=sunn-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/clev-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00333158Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/clev-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 8.15189Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/clev-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 7.75722333333333Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/clev-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 0.520729Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/denv-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" No throughput data returned for direction where src=chic-pt1.es.net dst=denv-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/denv-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=denv-pt1.es.net dst=hous-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/denv-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" No throughput data returned for direction where src=hous-pt1.es.net dst=denv-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/denv-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=denv-pt1.es.net dst=sunn-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/denv-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" No throughput data returned for direction where src=sunn-pt1.es.net dst=denv-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/denv-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 3.94622Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/denv-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00395194Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/denv-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" No throughput data returned for direction where src=doe-pt1.es.net dst=chic-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/doe-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.842569Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/doe-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=doe-pt1.es.net dst=hous-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/doe-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00560588Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/doe-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=doe-pt1.es.net dst=sunn-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/doe-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0032604Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/doe-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=doe-pt1.es.net dst=wash-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/doe-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" No throughput data returned for direction where src=wash-pt1.es.net dst=doe-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/doe-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 0.184879Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/elpa-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.878407Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/elpa-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 8.15883Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/elpa-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 4.107858Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/elpa-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 7.425585Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/elpa-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 6.86251333333333Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/elpa-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 7.25624Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/elpa-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00370766Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/elpa-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" No throughput data returned for direction where src=eqx-ash-pt1.es.net dst=chic-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-ash-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.901612Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-ash-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.00784455Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-ash-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.005918082Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-ash-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=eqx-ash-pt1.es.net dst=sunn-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-ash-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0031396175Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-ash-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=eqx-ash-pt1.es.net dst=wash-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-ash-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.9680565Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-ash-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 0.964757Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-chi-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.971136666666667Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-chi-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=eqx-chi-pt1.es.net dst=hous-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-chi-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00899921Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-chi-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=eqx-chi-pt1.es.net dst=sunn-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-chi-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.004578594Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-chi-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.780606Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-chi-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0113187666666667Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-chi-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 0.133310066666667Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-sj-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.07402125Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-sj-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=eqx-sj-pt1.es.net dst=hous-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-sj-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0932506Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-sj-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.93886Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-sj-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.880244333333333Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-sj-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.0946440666666667Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-sj-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0131815Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/eqx-sj-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" No throughput data returned for direction where src=forr-pt1.es.net dst=chic-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/forr-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.847902Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/forr-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=forr-pt1.es.net dst=hous-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/forr-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00554567Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/forr-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=forr-pt1.es.net dst=sunn-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/forr-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00283377285714286Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/forr-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.950016Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/forr-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.9216755Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/forr-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 0.039643692Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/inl-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0022998975Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/inl-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.0137376Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/inl-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.01198498Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/inl-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.0807381666666667Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/inl-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.033507625Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/inl-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.0370316975Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/inl-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0021112875Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/inl-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 1.77843166666667Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/kans-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 3.7190424Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/kans-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.017272Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/kans-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 5.182695Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/kans-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 7.58797Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/kans-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 5.7492875Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/kans-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 7.65898Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/kans-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00660182333333333Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/kans-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 0.0314442666666667Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lasv-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0945527Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lasv-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.0407865Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lasv-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.199800325Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lasv-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.253574Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lasv-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.38944925Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lasv-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.0220093Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lasv-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0137269333333333Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lasv-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" No throughput data returned for direction where src=lvk-pt1.es.net dst=chic-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lvk-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00471271Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lvk-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=lvk-pt1.es.net dst=hous-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lvk-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0012273435Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lvk-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=lvk-pt1.es.net dst=sunn-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lvk-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00478651Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lvk-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=lvk-pt1.es.net dst=wash-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lvk-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00335475333333333Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/lvk-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" No throughput data returned for direction where src=newy-pt1.es.net dst=chic-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/newy-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 3.75108Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/newy-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=newy-pt1.es.net dst=hous-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/newy-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0044793Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/newy-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=newy-pt1.es.net dst=sunn-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/newy-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00227202Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/newy-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 9.1444Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/newy-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" No throughput data returned for direction where src=wash-pt1.es.net dst=newy-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/newy-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 0.75031775Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/nrel-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.363561857142857Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/nrel-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=nrel-pt1.es.net dst=hous-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/nrel-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.441041Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/nrel-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.639120666666667Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/nrel-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.338216Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/nrel-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.827476Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/nrel-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.004740694Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/nrel-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" No throughput data returned for direction where src=orau-pt1.es.net dst=chic-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/orau-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0085492Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/orau-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.00523397Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/orau-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00703606Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/orau-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.688418Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/orau-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00338299333333333Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/orau-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.787215Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/orau-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.01410902Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/orau-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 0.789644333333333Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/osti-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0086786075Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/osti-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=osti-pt1.es.net dst=hous-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/osti-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00670083Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/osti-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.862314Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/osti-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00296268666666667Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/osti-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.8913125Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/osti-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.009695305Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/osti-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 0.0379188Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/paix-pa-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0431594666666667Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/paix-pa-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.0610763Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/paix-pa-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.2634005Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/paix-pa-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.8634445Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/paix-pa-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.8333906Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/paix-pa-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.0963184333333333Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/paix-pa-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0179318533333333Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/paix-pa-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 0.247959333333333Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/pantex-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.484714Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/pantex-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.2672575Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/pantex-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.4345552Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/pantex-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.271495333333333Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/pantex-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.452931833333333Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/pantex-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.139357Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/pantex-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00370091666666667Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/pantex-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 0.184086333333333Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/snla-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.522807Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/snla-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.2401876Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/snla-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0109487333333333Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/snla-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.1454495Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/snla-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00877121333333333Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/snla-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.1745234Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/snla-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0038363725Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/snla-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 0.0120399466666667Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/srs-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.0039317Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/srs-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.0196306Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/srs-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.00486644Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/srs-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.00413815Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/srs-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" No throughput data returned for direction where src=sunn-pt1.es.net dst=srs-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/srs-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.0128307475Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/srs-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.006279425Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/srs-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ],
          [
             [
                {
                   "message":" Average throughput is 7.871965Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 7.02195Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 0.608953Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.007868612Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/hous-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" No throughput data returned for direction where src=star-pt1.es.net dst=sunn-pt1.es.net",
                   "status":"3",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 0.003939425Gbps ",
                   "status":"1",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/sunn-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ],
             [
                {
                   "message":" Average throughput is 5.01062333333333Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps"
                },
                {
                   "message":" Average throughput is 5.066662Gbps ",
                   "status":"0",
                   "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/wash-pt1.es.net/BWCTL+500Mbps+Reverse"
                }
             ]
          ]
       ]
    }