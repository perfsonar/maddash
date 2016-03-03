**************************************
Advanced: perfSONAR Auto-Configuration
**************************************

Overview
========
The perfSONAR project includes centralized configuration management software that allows tests to be defined and published in a JSON format. It also provides a tool that can read this JSON file and generate a maddash.yaml file. This has the advantage that you dashboards will automatically update as new tests are added and removed and reduce errors due to mismatches between MaDDash configurations and the configurations on your testers. This section details how to configure your MaDDash instance to consume a published JSON file(s) and generate the corresponding maddash.yaml file.

Quick Start
===========
#. Login to the command-line interface of your host running MaDDash via SSH or a local terminal
#. Install the MeshConfig software::

    yum install perfsonar-meshconfig-guiagent

#. Update */etc/perfsonar/meshconfig-guiagent.conf* with the URL of your JSON file::

    <mesh>
        configuration_url             https://host.domain.edu/example.json   
    </mesh>
    ...

#. Run the following command to generate your maddash configuration for the first time::

     /usr/lib/perfsonar/bin/generate_gui_configuration

#. Go to your MaDDash web interface to verify the results. You should now have a MaDDash configuration that will update nightly based on the published mesh
 
Installing the package
======================

The configuration agent is made available in the same yum repository as maddash. To install it, run the following command::

    yum install perfsonar-meshconfig-guiagent

Configuration Guide
===================

This section provides detailed instructions on the configuration options available and details more advanced topics for those wishing to go beyond the default settings.

.. _config-cron:

Cron
----

The perl-perfSONAR_PS-MeshConfig-GUIAgent package by default installs a cron job that generates a new config nightly. The cron file can be found at the following location:

* /etc/cron.d/cron-mesh_config_gui_agent

You may edit this file if you wish to change the frequency. 

Non-perfSONAR Toolkit Nodes
+++++++++++++++++++++++++++

If you are not running MaDDash on the perfSONAR Toolkit, you MUST do the following:

#. Add a cron job to restart the services. You can do this by manually installing a provided cron script::

    cp /usr/share/doc/perfsonar-meshconfig-agent/cron-restart_gui_services /etc/cron.d/cron-restart_gui_services

#. It is required that MaDDash get restarted after the configuration is regenerated. You should adjust the time such that it runs at least 10 minutes after the configuration script runs to give adequate time for the new config to be built. The provided script will work for the default cron schedule, but you may need to adjust the time if you change the schedule. perfSONAR Toolkit users do not need to install this cron job as it can interact with a special daemon on the toolkit to restart it as soon as the script finishes.  

#. Make sure *generate_gui_configuration* can write to your maddash.yaml file. The easiest way to do this is to change the line in */etc/cron.d/cron-mesh_config_gui_agent* to run as *root* instead of the *perfsonar* user.

.. config-gui-agent:

GUI Agent
----------------------------------------

The primary file where you can configure the GUI Agent is found at the following location:

* /etc/perfsonar/meshconfig-guiagent.conf

This file allows you to define the meshes to use. It also allows you to set parameters for MaDDash such as how often checks run. 

General Properties
++++++++++++++++++

+---------------------------+---------------------+----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| Name                      | Type                | Required | Description                                                                                                                                                                                                                                                                                                                                    | 
+===========================+=====================+==========+================================================================================================================================================================================================================================================================================================================================================+
| maddash_yaml              | String              | Yes      | The path on the local filesystem to maddash.yaml. In the general case it will be */etc/maddash/maddash-server/maddash.yaml*                                                                                                                                                                                                                    | 
+---------------------------+---------------------+----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| use_toolkit               | 0 or 1              | Yes      | The 'use_toolkit' option can be used to specify whether the agent should use the pS-Performance Toolkit's configuration daemon to save the configuration, and restart the services. If the agent is not installed on a toolkit instance, you will make sure that the configuration files listed above are all writable by user 'perfsonar'.    |
+---------------------------+---------------------+----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
| restart_services          | 0 or 1              | Yes      | The 'restart_services' option can be used to specify whether the services should be restarted after their configuration is updated. If the 'use_toolkit' variable is set to 0, use the 'cron-restart_gui_services' cron script included in /usr/share/doc/perfsonar-meshconfig-agent to restart the services. See :ref:`config-cron`.          |
+---------------------------+---------------------+----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
| send_error_emails         | 0 or 1              | No       | The 'send_error_emails' option can be used to tell the agent to send an email via sendmail) when an error occurs. These emails will be sent to the applicable administrators (e.g. the local administrator(s), the mesh administrator(s), the site administrator(s), and/or the host administrator(s).                                         |
+---------------------------+---------------------+----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
| send_error_emails_to_mesh | 0 or 1              | No       | The 'send_error_emails_to_mesh' option can be used to tell the agent to send an email to the mesh configuration administrators. If this is left unset, the only emails that will be sent out are to those listed in this configuration file.                                                                                                   |
+---------------------------+---------------------+----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
| email_from_address        | String              | No       | The port on which the web server should listen for HTTPS connections                                                                                                                                                                                                                                                                           |
+---------------------------+---------------------+----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
| admin_email               | String              | No       | By default, the from address for the error emails will be *mesh_agent@hostname*. However, if you'd like to specify a different email address, you can do so here. You may specify more than one.                                                                                                                                               |
+---------------------------+---------------------+----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
| mesh                      | Configuration Block | Yes      | You must define one or more blocks that give information about the location of the mesh file. See :ref:`mesh-props`                                                                                                                                                                                                                            |
+---------------------------+---------------------+----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
| maddash_options           | Configuration Block | Yes      | A block defining maddash specific options. See :ref:`maddash-props`.                                                                                                                                                                                                                                                                           |
+---------------------------+---------------------+----------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
 

.. _mesh-props:

Mesh Properties
+++++++++++++++

+----------------------+--------+---------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| Name                 | Type   | Required                              | Description                                                                                                                                                                                                    |
+======================+========+=======================================+================================================================================================================================================================================================================+ 
| configuration_url    | String | Yes                                   | Specifies the URL where the mesh config JSON file is published                                                                                                                                                 |
+----------------------+--------+---------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+ 
| validate_certificate | 0 or 1 | No                                    | If using https in the *configuration_url*  verifies that the certificate is trusted and matches the hostname. If the 'validate_certificate' option is set to 1, the 'ca_certificate_file' option must be set.  | 
+----------------------+--------+---------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ca_certificate_file  | String | No (unless validate_certificate is 1) | Specifies a .crt file to use as to validate https certificates                                                                                                                                                 | 
+----------------------+--------+---------------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

.. _maddash-props:

MaDDash Properties
++++++++++++++++++

+---------------------+---------------------+----------+--------------------------------------------------------------------------------+
| Name                | Type                | Required | Description                                                                    |
+=====================+=====================+==========+================================================================================+ 
| external_address    | String              | No       | The address to use in fields like graphUrl. Should be a public hostname or IP. |
+---------------------+---------------------+----------+--------------------------------------------------------------------------------+ 
| perfsonarbuoy/owamp | Configuration block | Yes      | Contains options for one-way delay and loss checks. See :ref:`owdelay-props`   |
+---------------------+---------------------+----------+--------------------------------------------------------------------------------+ 
| perfsonarbuoy/bwctl | Configuration block | Yes      | Contains options for throughput checks. See :ref:`throughput-props`            | 
+---------------------+---------------------+----------+--------------------------------------------------------------------------------+

.. _owdelay-props:

MaDDash One-Way Delay Check Properties
++++++++++++++++++++++++++++++++++++++

+----------------------+---------+----------+----------------------------------------------------------------------------------------------------------------------------------------+
| Name                 | Type    | Required | Description                                                                                                                            | 
+======================+=========+==========+========================================================================================================================================+
| check_command        | String  | Yes      | The path to the command to run when doing the check                                                                                    | 
+----------------------+---------+----------+----------------------------------------------------------------------------------------------------------------------------------------+
| check_interval       | Integer | Yes      | The time in between checks in seconds                                                                                                  | 
+----------------------+---------+----------+----------------------------------------------------------------------------------------------------------------------------------------+
| check_time_range     | Integer | Yes      | The amount of time range of data to analyse in seconds                                                                                 |
+----------------------+---------+----------+----------------------------------------------------------------------------------------------------------------------------------------+ 
| acceptable_loss_rate | Float   | Yes      | The amount of acceptable loss as a decimal indicating the percentage (e.g. 0.01 for 1%)                                                | 
+----------------------+---------+----------+----------------------------------------------------------------------------------------------------------------------------------------+
| critical_loss_rate   | Float   | Yes      | The loss threshold at which the dashboard will go critical/red for the check as a decimal indicating the percentage (e.g. 0.01 for 1%) | 
+----------------------+---------+----------+----------------------------------------------------------------------------------------------------------------------------------------+
| grid_name            | String  | No       | You may specify this option more than once. The name of the grid(s) for which these check parameters apply.                            | 
+----------------------+---------+----------+----------------------------------------------------------------------------------------------------------------------------------------+

.. _throughput-props:

MaDDash Throughput Check Properties
+++++++++++++++++++++++++++++++++++

+-----------------------+---------+----------+-------------------------------------------------------------------------------------------------------------+
| Name                  | Type    | Required | Description                                                                                                 |
+=======================+=========+==========+=============================================================================================================+ 
| check_command         | String  | Yes      | The path to the command to run when doing the check                                                         |
+-----------------------+---------+----------+-------------------------------------------------------------------------------------------------------------+ 
| check_interval        | Integer | Yes      | The time in between checks in seconds                                                                       |
+-----------------------+---------+----------+-------------------------------------------------------------------------------------------------------------+ 
| check_time_range      | Integer | Yes      | The amount of time range of data to analyse in seconds                                                      |
+-----------------------+---------+----------+-------------------------------------------------------------------------------------------------------------+ 
| acceptable_throughput | Float   | Yes      | The acceptable throughput in Gbps                                                                           |
+-----------------------+---------+----------+-------------------------------------------------------------------------------------------------------------+ 
| critical_throughput   | Float   | Yes      | The threshold at which throughput should alarm critical/red in Mbps                                         | 
+-----------------------+---------+----------+-------------------------------------------------------------------------------------------------------------+
| grid_name             | String  | No       | You may specify this option more than once. The name of the grid(s) for which these check parameters apply. | 
+-----------------------+---------+----------+-------------------------------------------------------------------------------------------------------------+

.. manual-configs:

Manual MaDDash Configurations
-----------------------------

It should be noted all existing groups, grids, checks, dashboards and general properties that are defined outside the mesh will be preserved. this allows you to combine manual configurations with those automatically generated. This can also be useful for adjusting how automatically generated values are displayed. For example, the automatically generated file only creates one dashboard with all the grids. You can manually create more dashboards and reference both manual or automatically generated grids if you wish to have a richer display.

Log Files
=========

Each run of the *generate_gui_configuration* script logs to the following file:

* /var/log/perfsonar/mesh_configuration_gui_agent.log