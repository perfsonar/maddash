*****
Cells
*****

Retrieving a cell
=================

Request
-------

The request is a HTTP GET to the cell URL. There are currently no parameters to this request. See below for full summary.

+---------------+---------------------------------------------------+
| *URL*         | /maddash/grids/<grid-name>/<row-name>/<cell-name> |
+---------------+---------------------------------------------------+
| *HTTP Method* | GET                                               |
+---------------+---------------------------------------------------+

Response
--------

*Type:* application/json

+------------------+------------------+----------+--------------------------------------------------+
| Field            | Type             | Required | Description                                      |
+==================+==================+==========+==================================================+
| *checks*         | array of objects | Yes      | A list of check objects                          |
+------------------+------------------+----------+--------------------------------------------------+
| *checks[n].name* | string           | Yes      | The name of the check                            |
+------------------+------------------+----------+--------------------------------------------------+
| *checks[n].uri*  | string           | Yes      | The URI where you can retrieve the check details |
+------------------+------------------+----------+--------------------------------------------------+

Example
+++++++

::

    {
       "checks":[
          {
             "name":"BWCTL 500Mbps",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps"
          },
          {
             "name":"BWCTL 500Mbps Reverse",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/chic-pt1.es.net/BWCTL+500Mbps+Reverse"
          }
       ]
    }