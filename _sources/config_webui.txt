*****************************
Customizing the Web Interface
*****************************

Web Server
==========
The maddash-webui is a web application that runs under Apache. The web scripts and resources are installed in */usr/lib/maddash/maddash-webui*. In addition, an Apache configuration file is installed at */etc/httpd/conf.d/apache-maddash.conf* that sets-up the */maddash-webui* URL. It also creates a reverse proxy for request to the URL */maddash* to localhost port 8881. If you modify the server port for maddash, you will also need to update this configuration.


.. _config-webui-vizcustom:

Visual Customizations
=====================
The interface provides some customization options in the JSON config file */etc/maddash/maddash-webui/config.json*. It provides the following options:

+------------------+-------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| Name             | Type        | Required | Description                                                                                                                                                                                                               |
+==================+=============+==========+===========================================================================================================================================================================================================================+ 
| title            | String      | Yes      | The title displayed at the very top of the web page                                                                                                                                                                       |
+------------------+-------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
| defaultDashboard | String      | Yes      | The name of the default dashboard to display when someone visits /maddash-webui. It MUST match the name of a dashboard defined in your maddash-server.yaml file.                                                          | 
+------------------+-------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| colors           | JSON object | No       | See :ref:`config-webui-vizcustom-colors`                                                                                                                                                                                  |
+------------------+-------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| enableAdminUI    | Boolean     | No       | A 'true' or 'false' value indicating you want *Server Settings...* to appear in the *Settings* menu of the web interface. Note this does NOT disable direct access to the administrator UI, just removes it from the menu.|
+------------------+-------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| addNodeURL       | String      | No       | A URL where users may find more information about adding a node to your dashboard(s). If populated an extra item will be added to the menu bar displayed for users.                                                       |
+------------------+-------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| externalLinksMenu| JSON object | No       | See :ref:`config-webui-vizcustom-extlinks`                                                                                                                                                                                |
+------------------+-------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| grids            | JSON object | No       | Custom layout features for individual grids. See :ref:`grids-props` section.                                                                                                                                              | 
+------------------+-------------+----------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

.. _config-webui-vizcustom-colors:

Customizing Dashboard Colors
----------------------------
.. note:: Color customization was added in version 1.2 of MaDDash and not supported in previous versions.

You may customize the colors used by the dashboards for each check state using the *colors* option. This option takes the form of an object consisting of key/value pairs. The key is the state value and the value is the color to be displayed for that state value.  See :ref:`status-codes` for a complete listing of status codes and their meanings.  The value is the color as a CSS color name (e.g. black) or hexadecimal (e.g. #000000). For example, the default value look like the following::

    "colors": {
        0: "green",
        1: "yellow",
        2: "red",
        3: "orange",
        4: "gray",
        5: "black"
     }

If you wanted to change everything in the OK state to be blue instead of green you could provide something like the following::

    "colors": {
        0: "blue",
        1: "yellow",
        2: "red",
        3: "orange",
        4: "gray",
        5: "black"
     }

.. note:: It should be noted you must define states 0-5 every time you provide the colors. It is not enough to just define the subset you want to change. In addition, some checks may have custom states greater than 5. You may optionally include any states greater than five  in the configuration, but 0-5 are always required. See documentation for a specific check if you are unsure if it has any custom states.

.. _config-webui-vizcustom-extlinks:

Adding a Custom List of External Links
---------------------------------------
.. note:: The External Links menu was added in version 1.2 of MaDDash and not supported in previous versions.

You may define a list of links that will appear in the top navigation menu of all MaDDash pages. You may add links to anything with a URL including items such as your organization's homepage, links to other MaDDash instances or other monitoring tools. The block contains a list of objects with a label and a URL. The label is the text displayed in the drop-down menu and the URL is the hyperlink to be opened when that text is clicked. An example of the configuration block is below::
    
    "externalLinksMenu": {
        "menuLinks": [
            { 
               "label": "ESnet",
                "url": "http://www.es.net"
            },
            {
               "label": "perfSONAR",
               "url": "http://www.perfsonar.net"
            }
        ]
    }
    
By default the dopdown appear as *External Links" in the top menu. If you would like to change this you can customize the label with the **menuLabel** property::

    "externalLinksMenu": {
        "menuLabel": "Other Resources",
        "menuLinks": [
            { 
               "label": "ESnet",
                "url": "http://www.es.net"
            },
            {
               "label": "perfSONAR",
               "url": "http://www.perfsonar.net"
            }
        ]
    }

A table with a full listing of the properties detailed above can be seen below:

+--------------------+---------+----------+----------------------------------------------------------------------------------------------+
| Name               | Type    | Required | Description                                                                                  | 
+====================+=========+==========+==============================================================================================+
| menuLabel          | String  | No       | Optional label that appears in menu bar for external links menu. Default is "External Links" | 
+--------------------+---------+----------+----------------------------------------------------------------------------------------------+
| menuLinks          | Array   | Yes      | Array of objects describing each link you want displayed in the menu.                        |
+--------------------+---------+----------+----------------------------------------------------------------------------------------------+ 
| menuLinks[n].label | String  | Yes      | The text to appear in the menu for an individual link                                        |
+--------------------+---------+----------+----------------------------------------------------------------------------------------------+
| menuLinks[n].url   | String  | Yes      | The URL to open when a link is clicked                                                       |
+--------------------+---------+----------+----------------------------------------------------------------------------------------------+

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
 