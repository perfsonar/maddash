*************
Release Notes
*************


Version 1.2
===================

Administrator Web Interface
---------------------------
A new administrator web interface can now be accessed to perform actions such as:

* Force a set of checks to run again at a certain time. This may be useful when you want to reduce the wait time for a check to run again, such as when an issue has been resolved. 
* Schedule maintenance events that may impact check results. Scheduling events gives you the option to stop checks from running during certain time windows. It also allows you to provide a description of events which should clarify why checks behave certain ways.

See :doc:`admin_ui` for more details on how to use this interface. 

Improved Web Interface
-----------------------
* Visual enhancements to the menu make it more prominent and add more options
* Auto-refreshes are now supported. Just click on the new *Settings* menu and select the desired interval from the *Auto Refresh* menu
* A label indicating the last time a page is refreshed is now on the top of every page so you know the freshness of the page you are viewing. 
* Details about events created through the new administrator interface are included on each check details page (i.e. the page accessed by clicking a box in the dashboard). This should help identify alarms generated due to maintenance events or similar.
* A special state was added for scheduled downtime. By default checks in this state will be marked as black.

Enhanced Customization Options
------------------------------
* You can now customize the colors displayed by maddash. See :ref:`config-webui-vizcustom-colors` for more details.
* You may now define custom links that get applied to row and column labels. See *pstoolkiturl* under :ref:`group-member-props` for more information.
* You may add a special link to the menu where users may get more information on how to get added to their dashboard. See *addNodeURL* under :ref:`config-webui-vizcustom` for more details.
* An option was added to include a link to the new administrator interface in the *Settings* menu. This option will be on by default for new users. See *enableAdminUI* under :ref:`config-webui-vizcustom` for more details.

New API Calls
-------------
* New administrator functions have been added to manage events and reschedule the time that checks run. These checks must go over HTTPS and require HTTP BASIC authentication (this is enforced by the provided Apache configuration). 
* New GET requests that list the row members, column members and check names have been added
* Custom status codes are now supported so checks can add new states not previously supported. 