******************
The Basics
******************

This page describes the Monitoring and Debugging Dashboard (MaDDash) API. The API uses a JSON data format and attempts to adhere closely to principles of REST. The interface provides access to the following types of resources:

* *Dashboard:* A collection of one or more *grid* resources. The grouping of *grids* into a dashboard as defined by the creator of the *dashboard* resource.
* *Grid:* A two-dimensional representation of one or more *rows* and *columns* of *checks* that measure some value between entities represented by the column value (x) and row value (y).
* *Check:* The output of a task performed using the column value (x) and the row value (y) as input.
* *Column:* A collection of checks that share a common x value. 
* *Row:* A collection of checks that share a common y value
* *Cell:* A collection of checks that share a common x and y value

A visual representation of these resources is provided in the figure below:

.. image:: images/maddash-resources.png
