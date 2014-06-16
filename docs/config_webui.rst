*****************************
Customizing the Web Interface
*****************************

Web Server
==========
The maddash-webui is a web application that runs under Apache. The web scripts and resources are installed in */opt/maddash/maddash-webui*. In addition, an Apache configuration file is installed at */etc/httpd/conf.d/apache-maddash.conf* that sets-up the */maddash-webui* URL. It also creates a reverse proxy for request to the URL */maddash* to localhost port 8881. If you modify the server port for maddash, you will also need to update this configuration.


Visual Customizations
=====================
The interface provides some customization options in the JSON config file */opt/maddash/maddash-webui/etc/config.json*. It provides the following options:

+------------------+-------------+----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| Name             | Type        | Required | Description                                                                                                                                                      |
+==================+=============+==========+==================================================================================================================================================================+ 
| title            | String      | Yes      | The title displayed at the very top of the web page                                                                                                              |
+------------------+-------------+----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
| defaultDashboard | String      | Yes      | The name of the default dashboard to display when someone visits /maddash-webui. It MUST match the name of a dashboard defined in your maddash-server.yaml file. | 
+------------------+-------------+----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| grids            | JSON object | No       | Custom layout features for individual grids. See :ref:`grids-props` section.                                                                                     | 
+------------------+-------------+----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------+

.. _grids-props:

grids properties
----------------
grids are specified as follows (where *gridName* is the name of the grid you want to adjust)::

    "grids":{
            "gridName":{
                ...properties...
            },
        }

The following properties are available:

+---------------+---------+----------+--------------------------------------------------------+
| Name          | Type    | Required | Description                                            | 
+===============+=========+==========+========================================================+
| cellSize      | Integer | No       | The height and width in pixels of one cell in the grid | 
+---------------+---------+----------+--------------------------------------------------------+
| cellPadding   | Integer | No       | The space between cells of the grid                    |
+---------------+---------+----------+--------------------------------------------------------+ 
| textBlockSize | Integer | No       | The length of the text blocks at the top of the grid   |
+---------------+---------+----------+--------------------------------------------------------+
 
