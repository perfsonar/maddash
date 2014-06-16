MaDDash
========

The Monitoring and Debugging Dashboard (MaDDash) is a tool for collecting large amounts of inherently two-dimensional data and presenting it in visually useful ways. The project consists of the server side that schedules jobs to collect data and a web front-end to display that data. A summary of the collected data is also made available as a JSON REST API. This initial use case was collecting point-to-point network measurements published by perfSONAR and displaying them as an organized collection grids on the web front-end. These were presented with one endpoint as the row and the second endpoint as the column. In principle though, MaDDash can be extended to support any two-dimensional data. 

Deploying
---------
MaDDash is available as an RPM and this is the recommended method of deploying MaDDash on a production host. See our [Install Guide](http://software.es.net/maddash) for more details.

Contributing
------------
There are multiple ways one may want to contribute including:
 * Updating the data collector
 * Updating the existing web interface
 * Writing new clients to the JSON REST API

The subsection are for developers wishing to get involved in one or more of the ways above. 

###Development System Requirements

 * Unix-like OS (e.g. Mac OS X, Linux)
 * Java 1.6 or higher
 * Maven 3.0.3 or higher 
 * Apache 2.2 or higher

###Building

```
mvn clean install
```

###Configuring your development environment

The collector configuration file can be found in *maddash-server/etc/maddash-server.yaml*. See the [Install Guide](http://software.es.net/maddash) for a full list of options. 

The web interface has a sample Apache configuration under *maddash-webui/etc/apache-maddash.conf*. You will need to modify this with file paths appropriate for your system and install it under your Apache configuration directory (e.g. */etc/httpd/conf.d*). There are also some UI customizations that can be done by editing *maddash-webui/web/etc/config.json*. See [Install Guide](http://software.es.net/maddash) for a full list of options.

###Running a development data collector

```
cd maddash-server
./bin/run.sh
```

###Running a development web interface

The web interface runs under apache. You will need to modify *maddash-webui/etc/apache-maddash.conf* and install it under apache for the server to run. 

###Using the REST API

See our [REST API Guide](http://software.es.net/maddash/#api)

Support
-------
MaDDash is maintained as part of the perfSONAR project and all support questions can be sent to perfsonar-user@internet2.edu.
