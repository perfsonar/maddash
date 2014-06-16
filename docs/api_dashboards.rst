**********
Dashboards
**********

Retrieving the list of dashboards
=================================

Request
-------

The request is a HTTP GET to the dashboards URL. There are currently no parameters to this request. See below for full summary.

+---------------+---------------------+
| *URL*         | /maddash/dashboards |
+---------------+---------------------+
| *HTTP Method* | GET                 |
+---------------+---------------------+

Response
--------

**Type:** application/json

+--------------------------------+------------------+----------+--------------------------------------------------------------------+
| Field                          | Type             | Required | Description                                                        |
+================================+==================+==========+====================================================================+
| *dashboards*                   | array of objects | Yes      | The list of dashboards available                                   |
+--------------------------------+------------------+----------+--------------------------------------------------------------------+
| *dashboards[n].name*           | string           | Yes      | The name of the dashboard                                          |
+--------------------------------+------------------+----------+--------------------------------------------------------------------+
| *dashboards[n].grids*          | array of strings | Yes      | A list of URIs that indicate which grids belong to this dashboard  |
+--------------------------------+------------------+----------+--------------------------------------------------------------------+
| *dashboards[n].grids[n].name*  | string           | Yes      | The name of the grid                                               |
+--------------------------------+------------------+----------+--------------------------------------------------------------------+
| *dashboards[n].grids[n].uri*   | string           | Yes      | The URI of the grid                                                |
+--------------------------------+------------------+----------+--------------------------------------------------------------------+

Example
+++++++

::

    {
       "dashboards":[
          {
             "name":"DICE",
             "grids":[
                {
                   "name":"ESnet to GEANT BWCTL Tests",
                   "uri":"/maddash/grids/ESnet+to+GEANT+BWCTL+Tests"
                },
                {
                   "name":"ESnet to GEANT OWAMP Tests",
                   "uri":"/maddash/grids/ESnet+to+GEANT+OWAMP+Tests"
                }
             ]
          },
          {
             "name":"ESnet BWCTL",
             "grids":[
                {
                   "name":"ESnet BWCTL Tests - Large Sites",
                   "uri":"/maddash/grids/ESnet+BWCTL+Tests+-+Large+Sites"
                },
                {
                   "name":"ESnet BWCTL Tests - Other Sites",
                   "uri":"/maddash/grids/ESnet+BWCTL+Tests+-+Other+Sites"
                }
             ]
          },
          {
             "name":"ESnet OWAMP",
             "grids":[
                {
                   "name":"ESnet OWAMP Tests",
                   "uri":"/maddash/grids/ESnet+OWAMP+Tests"
                }
             ]
          }
       ]
    }
