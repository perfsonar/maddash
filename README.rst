esnet-gh-pages-base
===================

Base templates for ESnet's GitHub pages. These pages are created using the
Sphinx_ documentation package using the sphinx-bootstrap-theme_ with some
pages.  This repo is meant to be included into a project using git subtree and
provides the overrides and customizations to the base theme.

.. _Sphinx: http://sphinx-doc.org
.. _sphinx-bootstrap-theme: https://github.com/ryan-roemer/sphinx-bootstrap-theme

Installation
------------

1. Install Sphinx and sphinx-bootstrap-theme. See the instructions below for
   installing these either using the Mac OS X base system python or MacPorts.
2. ``cd $PROJECT_ROOT``
3. ``mkdir docs``
4. ``git subtree add --prefix docs/_esnet https://github.com/esnet/esnet-gh-pages-base.git master --squash``
5. ``cd docs``
6. ``sphinx-quickstart``
7. ``ln -s ../_esnet/static _static/esnet``
8. edit ``conf.py`` as described in the next section
  
Editing conf.py
^^^^^^^^^^^^^^^

``sphinx-quickstart`` creates a basic conf.py file. In general the
defaults are OK and things can be fixed later by editing conf.py. 
For author I suggest putting either the names of the people most 
involved in writing the docs, the names of the developrs or if all
else fails, ESnet is fine. The project release and version should 
map to the current version of the project your are documenting and it 
is a good idea to try to keep that up to date going forward. It might 
be worth automating this if you have a way to do so in the project
workflow.

However to use the ESnet theme we need to make some changes.
Make the following changes to conf.py::

   # add this with the imports at the top of the file
   import sphinx_bootstrap_theme

   # change templates_path to this
   templates_path = ['_esnet/templates']

   # add _esnet to exclude_patterns
   exclude_patterns = ['_build', '_esnet']

   # change html_theme and html_theme_path:
   html_theme = 'bootstrap'
   html_theme_path = sphinx_bootstrap_theme.get_html_theme_path()

   # add html_theme options:
   html_theme_options = {
          "navbar_pagenav": False,
          "nosidebar": False,
          "navbar_class": "navbar",
          "navbar_site_name": "Section",
          "source_link_position": "footer",
       "navbar_links": [
           ("Index", "genindex"),
           ("ESnet", "https://www.es.net", True),
       ],
   }

   # add html_logo and html_sidebars
   html_logo = "_esnet/static/logo-esnet-ball-sm.png"
   html_sidebars = {'index': None, 'search': None, '*': ['localtoc.html']}
   html_favicon = "_esnet/static/favicon.ico"
   html_context = {
      "github_url": "https://github.com/esnet/PROJNAME",
   }

That's it!

Files to track in the project repo
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The ``sphinx-quickstart`` command will create a number of files and the 
sphinx build process will make files as well. I suggest not saving 
sphinx build products in the project repo.  This works out to tracking 
the following files in ``docs``::

   *.rst
   Makefile
   conf.py
   _static/esnet  # to track the symlink created above

You'll need to ``git add`` these files to your repo.  You may also want to
add the following rules to ``${PROJECT_ROOT}/.gitignore``::

   # ignore Sphinx build products
   /docs/_build

Sphinx Installation using Mac OS X Base Python
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

1. sudo /usr/bin/easy_install pip
2. sudo /usr/local/bin/pip install sphinx sphinx-bootstrap-theme

Sphinx Installation using MacPorts
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

1. sudo port install python27 py27-pip py27-sphinx
2. sudo port select pip py27-pip
3. sudo port select sphinx py27-sphinx
4. pip install sphinx sphinx-bootstrap-theme # make sure this is /opt/local/bin/pip

Creating Content, Previewing and Publishing
-------------------------------------------

The files are in the ``docs`` directory.  Take a look at the content of
``index.rst``.  Take a look at the docs from other projects and review the
documentation for Sphinx_.

Create the ``gh-pages`` branch
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Follow the instructions at `GitHub gh-pages creation`_ to create the initial
gh-pages branch.

_`GitHub gh-pages creation`: https://help.github.com/articles/creating-project-pages-manually

Building HTML
^^^^^^^^^^^^^

In the ``docs`` directory run ``make clean html``.

Previewing the site
^^^^^^^^^^^^^^^^^^^

``open _build/html/index.html``

or

``open -a /Application/Google\ Chrome.app _build/html/index.html``

Publishing the site
^^^^^^^^^^^^^^^^^^^

From the ``docs`` directory run ``_esnet/deploy.sh``.  It will be visible at:
``http://github.com/esnet/PROJECT``.
