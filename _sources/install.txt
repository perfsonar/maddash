************
Installation
************

The recommend method for installing MaDDash is via `Yum <https://fedoraproject.org/wiki/Yum?rd=Tools/yum>`_. This document described the process for identifying a target host, setting up your yum environment and installing the maddash package.

System Requirements
===================
Yum will handle all the dependencies so the only real requirement is that the operating system be the following:

* CentOS 6 or later (32-bit or 64-bit)

Note that the `perfSONAR Toolkit <http://www.perfsonar.net>` version 3.3 and later meets this requirement so feel free to install in there. Toolkit hosts have the added benefit of having the yum setup already configured so you can skip the next section. A toolkit host may not be ideal if you have a very large mesh to display as it would be better served by having dedicated resources from it's own host.

Yum Repository Setup
====================
Installation is currently supported via an RPM distribution through yum. We need to point at MaDash's home yum repository. As noted earlier perfSONAR Toolkits will already contain this configuration meaning this section can be skipped if installing on a toolkit host. Otherwise we start by downloading an RPM to add the new yum repository::

    wget http://software.internet2.edu/rpms/el6/x86_64/RPMS.main/Internet2-repo-0.4-2.noarch.rpm

You will also need to point at the `EPEL <http://fedoraproject.org/wiki/EPEL>`_ repository to satisfy some package dependencies. Download it as follows::

    wget http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm

Next we install that new RPM::

    yum localinstall Internet2-repo-0.4-2.noarch.rpm epel-release-6-8.noarch.rpm

Finally we refresh our yum cache so it uses the new repository immediately::

    yum clean all

Installing MaDDash
==================
To install MaDDash we run the following command::

    yum install maddash

The following packages are installed by the current default installation:

* *maddash* - Container package that has dependencies on the *maddash-server*, *maddash-webui*, and perl-perfSONAR_PS-Nagios packages. The package itself does not install any additional software, it simply pulls in the aforementioned packages.
* *maddash-server* - The backend server that schedules checks and makes results available via a REST/JSON interface running on an embedded web server. This package has a dependency on java which will also be installed during the yum installation process.
* *maddash-webui* - The web pages that display the dashboard. It consists of a set of CGI scripts that run under Apache. The server contacts the REST server run by the maddash-server package and then presents the data on the web page.
* *perl-perfSONAR_PS-Nagios* - Installs the perfSONAR Nagios checks that can alarm based on throughput, loss and other data returned by perfSONAR services.
* *perl-perfSONAR_PS-serviceTest* - Provides the performance graphs used by the maddash-webui package for perfSONAR checks.

Updating 
========
As new versions of MaDDash are released you may update by running ``yum update``. Below are version specific update notes that highlight additional tasks that may need to be performed by the administrator.

Version 1.2
-----------
For upgraders coming from a previous version of the software no additional action is *required* for your installation to operate as it did prior to update, BUT you may need to make some additional changes if you wish to use some of the new features in version 1.2. Specifically:

* You will need to add an administrative user to access the Administrator Interface. See :ref:`adminui-adduser` for details.
* If you have previously edited */etc/httpd/conf.d/apache-maddash.conf* then you will need to add directives for the administrator interface. If you have never touched this file, then no further work will be required. Specifically the following directives need to be added to */etc/httpd/conf.d/apache-maddash.conf*::
 
    RewriteEngine On
    RewriteCond %{HTTPS} !=on
    RewriteRule ^/maddash/admin/.* https://%{SERVER_NAME}%{REQUEST_URI} [R,L]
    RewriteCond %{HTTPS} !=on
    RewriteRule ^/maddash-webui/admin/.* https://%{SERVER_NAME}%{REQUEST_URI} [R,L]

    <Location /maddash/admin>
        AuthType Basic
        AuthName "MaDDash Administrative API"
        AuthBasicProvider file
        AuthUserFile /etc/maddash/maddash-webui/admin-users
        Require valid-user
    </Location>

    <Location /maddash-webui/admin>
        AuthType Basic
        AuthName "MaDDash Administrative API"
        AuthBasicProvider file
        AuthUserFile /etc/maddash/maddash-webui/admin-users
        Require valid-user
    </Location>
* If you have not previously configured Apache to use HTTPS then a self-signed certificate will be generated for you during the update by Apache's mod_ssl. If you wish to replace this certificate with your own please see the `Apache SSL <http://httpd.apache.org/docs/2.4/ssl/ssl_howto.html>`_ page for more details.
* If *Server Settings...* does not apear in the *Settings* menu of the MaDDash web page, you may need to open */opt/maddash/maddash-webui/web/etc/config.json* and add the option *enableAdminUI: true*

