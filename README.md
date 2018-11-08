# MaDDash

The Monitoring and Debugging Dashboard (MaDDash) is a tool for collecting large amounts of inherently two-dimensional data and presenting it in visually useful ways. The project consists of the server side that schedules jobs to collect data and a web front-end to display that data. A summary of the collected data is also made available as a JSON REST API. This initial use case was collecting point-to-point network measurements published by perfSONAR and displaying them as an organized collection grids on the web front-end. These were presented with one endpoint as the row and the second endpoint as the column. In principle though, MaDDash can be extended to support any two-dimensional data. 

## Deploying

MaDDash is available as an RPM and this is the recommended method of deploying MaDDash on a production host. See our [Install Guide](http://docs.perfsonar.net/maddash_install.html) for more details.

Contributing
There are multiple ways one may want to contribute including:
 * Updating the data collector
 * Updating the existing web interface
 * Writing new clients to the JSON REST API

The subsection are for developers wishing to get involved in one or more of the ways above. 

## Development Environment

This repository allows you to use [Vagrant](https://www.vagrantup.com) to create a VM on [VirtualBox](https://www.virtualbox.org) with the necessary components installed. The default VM is based on CentOS 7 and creates a shared folder in the VM that points at the top-level of your checked-out copy. This allows you to edit files on your base system and have the changes automatically appear in the VM.

### Installation
1. Install [VirtualBox](https://www.virtualbox.org) according to the instructions on their site for your system. 
1. Install [Vagrant](https://www.vagrantup.com) according to the instructions on their site for your system. 
1. Install the vagrant-vbguest and vagrant-reload plugins with the following commands:
    ```
    vagrant plugin install vagrant-vbguest
    vagrant plugin install vagrant-reload
    ```
1. Clone the MaDDash github repo:
    ```
    git clone https://github.com/perfsonar/maddash
    ```
1. Start the VM with ``vagrant up``. The first time you do this it will take awhile to create the initial VM.

### Using the VM

You can login to the VM with the following command:
  ```
  vagrant ssh
  ```
You can start MaDDash with the following:
  ```
  systemctl start maddash-server
  ```
A build was done during VM creation and the version running will be based off the Git copy you have cloned locally. If you want to rebuild the packages and test new changes to the maddash-server run:
  ```
  cd /vagrant/maddash-server
  mvn clean install
  systemctl restart maddash-server
  ```
  
For changes to the web UI simply make you changes to the files under `maddash-webui`.


### Other Notes
* Any changes you make to the checked-out code on your host system get reflected in the host VM under the `/vagrant` directory
* The following symlinks are setup to files in the git copy of the code:
    
    * /etc/maddash -> /vagrant/vagrant-data/maddash-el7/etc/maddash
    * /usr/lib/maddash -> /vagrant/vagrant-data/maddash-el7/usr/lib/maddash
    * /usr/lib/maddash/maddash-webui -> /vagrant/maddash-webui/web
    * /usr/lib/maddash/maddash-server/target -> /vagrant/maddash-server/target
    * /usr/lib/maddash/maddash-server/bin/\*.sh -> /vagrant/maddash-server/bin/\*.sh
    
* Run ``vagrant reload`` to restart the VM
* Run ``vagrant suspend`` to freeze the VM. Running ``vagrant up`` again will restore the state it was in when you suspended it.
* Run ``vagrant halt`` to shutdown the VM. Running ``vagrant up`` again will run through the normal boot process.
* Run ``vagrant destroy`` to completely delete the VM. Running again ``vagrant up`` will build a brand new VM.

## Using the REST API

See our [REST API Guide](http://docs.perfsonar.net)

## Support
MaDDash is maintained as part of the perfSONAR project and all support questions can be sent to perfsonar-user@perfsonar.net.
