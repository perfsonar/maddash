*******************************************
MaDDash: Monitoring and Debugging Dashboard
*******************************************

MaDDash is a project aimed at collecting and presenting two-dimensional monitoring data as a set of grids referred to as a dashboard. Many monitoring systems focus on one dimension, such as a single host or service. Unfortunately, users can quickly run into n-squared problems both in terms of configuration on the back-end and data presentation on the front-end if you try to present naturally two-dimensional data in this manner. 

The classic use case is point-to-point network measurements between two hosts. For every host in the set, you want to know some performance metric to every other host. You don't want to add a large configuration block for every host. You also don't want to do things like only look at the results in aggregate or be presented with a long list of every host pair. MaDDash addresses this by reading a configuration file where the two-dimensional grids are defined using specialized structures, running a set of jobs based on this configuration (usually a set of `Nagios <http://www.nagios.org>`_ checks), then storing the results. These results can then be accessed using a REST API which provides the building blocks for components such as the included web interface that presents the data as a set of grids. 

MaDDash is currently developed as part of the `perfSONAR <http://code.google.com/p/perfsonar-ps/w/list>`_ project. As such network monitoring is a primary focus of most deployments, but it is relatively agnostic to the data being collected and displayed so can be adapted to any case where a two-dimensional relationship is formed. 
 
Documentation
=============
Deploying
---------
.. toctree::
   :maxdepth: 2
   
   quick_start
   install
   config_server
   config_webui
   running
   admin_ui
   mesh_config
   release_notes
   
API
----------------
.. toctree::
   :maxdepth: 2
   
   api_intro
   api_dashboards
   api_grids
   api_rows
   api_cells
   api_checks
   api_events
   api_type_params
   api_misc
   
Links
=====
* `Project Homepage <http://software.es.net/maddash/>`_
* `Source Code Repository <https://github.com/esnet/maddash>`_
* `Issue Tracker <https://github.com/esnet/maddash/issues>`_
* `perfSONAR Project Page <http://code.google.com/p/perfsonar-ps/w/list>`_
* `Example ESnet MaDash Deployment <http://ps-dashboard.es.net>`_
 
Indices and tables
==================

* :ref:`genindex`
* :ref:`search`

