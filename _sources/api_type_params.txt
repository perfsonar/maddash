****************************************
Appendix: Check Type Specific Parameters
****************************************

Nagios Check
============

This is a type of check that represents a Nagios command.

Type
----

::

    net.es.maddash.checks.NagiosCheck

params
------

+-----------+--------+-----------------------------------------------------------+
| *Name*    | *Type* | *Description*                                             |
+===========+========+===========================================================+
| *command* | string | The nagios command templates used when running this check |
+-----------+--------+-----------------------------------------------------------+

returnParams
------------

+-------------------+--------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| *Name*            | *Type* | *Description*                                                                                                                                                                                                          |
+===================+========+========================================================================================================================================================================================================================+
| *<nagios-stat>*   | string |A nagios command can return an arbitrary set of key value pairs that report statistics about a check (e.g. mean, meadian, standard deviation, etc). These statics are mapped into this field as string key value pairs. |
+-------------------+--------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

perfSONAR Nagios Check
======================
This is a type of check that represents a Nagios command written as part of the perfSONAR-PS toolkit. *It inherits all the fields of *net.es.maddash.checks.NagiosCheck* in addition to a few related to retrieving graphs.* The fields specific to this check are listed below.

Type
----

::

    net.es.maddash.checks.PSNagiosCheck

params
------

+---------------------+--------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| *Name*              | *Type* | *Description*                                                                                                                                                                                  |
+=====================+========+================================================================================================================================================================================================+
| *metaDataKeyLookup* | string | Template for the URL to a script the metadata key can be retrieved. This is used by some perfSONAR instances to retrieve graph results                                                         |
+---------------------+--------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| *maUrl*             | object | Contains templates for accessing a host's Measurement Archive (MA)                                                                                                                             |
+---------------------+--------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| *maUrl.default*     | object | The default Measurement Archive (MA) URL template if no host specific URL provided                                                                                                             |
+---------------------+--------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| *maUrl.<hostname>*  | object | A Measurement Archive(MA) URL template where you can get results for a specific host. Available if a specific MA has a different URL structure then other hosts (e.g. runs on different port). |
+---------------------+--------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+

returnParams
------------

+----------+--------+----------------------------------------------------------------------------------------------------------+
| *Name*   | *Type* | *Description*                                                                                            |
+==========+========+==========================================================================================================+
| graphUrl | string | The full URL where a graph of data relevant to the check can be retrieved.                               |
+----------+--------+----------------------------------------------------------------------------------------------------------+
| maUrl    | string | The full URL of the perfSONAR Measurement Archive (MA) you can contact to get data related to this check |
+----------+--------+----------------------------------------------------------------------------------------------------------+

