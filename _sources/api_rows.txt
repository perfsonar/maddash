****
Rows
****

Retrieving a row
================

Request
-------

The request is a HTTP GET to the row URL. There are currently no parameters to this request. See below for full summary.

+---------------+---------------------------------------+
| *URL*         | /maddash/grids/<grid-name>/<row-name> |
+---------------+---------------------------------------+
| *HTTP Method* | GET                                   |
+---------------+---------------------------------------+

Response
--------

**Type:** application/json

+-----------------+------------------+----------+-------------------------------------------------+
| Field           | Type             | Required | Description                                     |
+=================+==================+==========+=================================================+
| *cells*         | array of objects | Yes      | A list of cell objects                          |
+-----------------+------------------+----------+-------------------------------------------------+
| *cells[n].name* | string           | Yes      | The name of the column                          |
+-----------------+------------------+----------+-------------------------------------------------+
| *cells[n].uri*  | string           | Yes      | The URI where you can retrieve the cell details |
+-----------------+------------------+----------+-------------------------------------------------+

Example
+++++++

::

    {
       "cells":[
          {
             "name":"chic-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/chic-pt1.es.net"
          },
          {
             "name":"chic-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/chic-pt1.es.net"
          },
          {
             "name":"hous-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/hous-pt1.es.net"
          },
          {
             "name":"hous-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/hous-pt1.es.net"
          },
          {
             "name":"sunn-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/sunn-pt1.es.net"
          },
          {
             "name":"sunn-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/sunn-pt1.es.net"
          },
          {
             "name":"wash-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/wash-pt1.es.net"
          },
          {
             "name":"wash-pt1.es.net",
             "uri":"/maddash/ESnet+BWCTL+Tests+-+Other+Sites/star-pt1.es.net/wash-pt1.es.net"
          }
       ]
    }