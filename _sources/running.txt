****************************
Running and Managing MaDDash
****************************

Starting the service
====================
::

    /etc/init.d/maddash-server start

Stopping the service
====================
::

    /etc/init.d/maddash-server stop

Restarting the service
======================
::

    /etc/init.d/maddash-server restart

Logs
====
Log detail and file locations can be edited by modifying the file */etc/maddash/maddash-server/log4j.properties*.

The following logs are available by default:

+-------------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------+
| Log                                             | Description                                                                                                                        |
+=================================================+====================================================================================================================================+
| */var/log/maddash/maddash-server.log*           | Log for general errors and output                                                                                                  |
+-------------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------+
| */var/log/maddash/maddash-server.netlogger.log* | Logs formatted in the NetLogger format                                                                                             |
+-------------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------+
| */var/log/maddash/maddash-server.out*           | If anything gets written to STDOUT, it will appear here. Often useful as a last resort if software is behaving in unexpected ways. |
+-------------------------------------------------+------------------------------------------------------------------------------------------------------------------------------------+
