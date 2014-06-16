***********
Quick Start
***********

This document is intended to get you started as quickly as possible with a basic setup. See other sections of the documentation for more details on the steps performed here. 

#. Login to a CentOS 6 or later host and setup your yum environment (move to step 2 if you are on a host running the perfSONAR Toolkit 3.3 or later, your yum is already setup!)::

    wget http://software.internet2.edu/rpms/el6/x86_64/RPMS.main/Internet2-repo-0.4-2.noarch.rpm
    yum install Internet2-repo-0.4-2.noarch.rpm
    yum clean all

#. Run the following command as a privileged user to install the software::

    yum install maddash

#. Open the file */etc/maddash/maddash-server/maddash.yaml* and change the following properties (*Note: Use spaces and not tabs in this file. YAML does not allow tabs.*)

#. Under the *groups* section, change the *myOwampHosts* list and the *myBwctlHosts* list to the list of OWAMP and BWCTL hosts you wish to check, respectively. *NOTE: If you comment out one of the groups because you don't want any BWCTL or OWAMP checks, then also remove the corresponding entry under the "grids" section of the file*
 
#. Do a search and replace for *example.mydomain.local* and change it to the hostname of the toolkit host on which the software is installed. This information will be used to generate the graphs.

#. Restart the server::

    /etc/init.d/maddash-server restart

#. Open the maddash web page in your browser at the following URL (replace MYHOST with the name of your host): http://MYHOST/maddash-webui

#. You should now be able to view the results of the checks being run. See the remainder of this document for more detailed customization features.
