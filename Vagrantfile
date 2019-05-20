# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|

  # Build a MaDDash el7 machine. The souce will live under /vagrant. You can access 
  # /etc/maddash in the shared directory /vagrant-data/vagrant/{hostname}/etc/maddash. 
  # Port forwarding is setup and hosts are on a private network with static IPv4 and IPv6 
  # addresses
  config.vm.define "maddash-el7", primary: true, autostart: true do |maddash|
    # set box to official CentOS 7 image
    maddash.vm.box = "centos/7"
    # explcitly set shared folder to virtualbox type. If not set will choose rsync 
    # which is just a one-way share that is less useful in this context
    maddash.vm.synced_folder ".", "/vagrant", type: "virtualbox"
    # Set hostname
    maddash.vm.hostname = "maddash-el7"
    
    # Enable IPv4. Cannot be directly before or after line that sets IPv6 address. Looks
    # to be a strange bug where IPv6 and IPv4 mixed-up by vagrant otherwise and one 
    #interface will appear not to have an address. If you look at network-scripts file
    # you will see a mangled result where IPv4 is set for IPv6 or vice versa
    maddash.vm.network "private_network", ip: "10.0.0.22"
    
    # Setup port forwarding to apache
    maddash.vm.network "forwarded_port", guest: 443, host: "22443", host_ip: "127.0.0.1"
    
    # Enable IPv6. Currently only supports setting via static IP. Address below in the
    # reserved local address range for IPv6
    maddash.vm.network "private_network", ip: "fdac:218a:75e5:69c8::122"
    
    #Disable selinux
    maddash.vm.provision "shell", inline: <<-SHELL
        sed -i s/SELINUX=enforcing/SELINUX=permissive/g /etc/selinux/config
    SHELL
    
    #reload VM since selinux requires reboot. Requires `vagrant plugin install vagrant-reload`
    maddash.vm.provision :reload
    
    #Install all requirements and perform initial setup
    maddash.vm.provision "shell", inline: <<-SHELL
        # Create users
        /usr/sbin/groupadd -r maddash 2> /dev/null || :
        /usr/sbin/useradd -g maddash -r -s /sbin/nologin -c "MaDDash User" -d /tmp maddash 2> /dev/null || :
        
        # Clean out any misconfigured shared directories
        # Create here in case RPMs touch (in theories they should not)
        if ! [ -d /vagrant/vagrant-data/maddash-el7/etc/maddash ]; then
            rm -rf /vagrant/vagrant-data/maddash-el7/etc/perfsonar
        fi
        if ! [ -L /etc/maddash ]; then
            rm -rf /etc/maddash
        fi
        if ! [ -d /vagrant/vagrant-data/maddash-el7/usr/lib/maddash ]; then
            rm -rf /vagrant/vagrant-data/maddash-el7/usr/lib/maddash
        fi
        if ! [ -L /usr/lib/maddash ]; then
            rm -rf /usr/lib/maddash
        fi
        mkdir -p /vagrant/vagrant-data/maddash-el7/etc/maddash
        ln -fs /vagrant/vagrant-data/maddash-el7/etc/maddash /etc/maddash
        mkdir -p /vagrant/vagrant-data/maddash-el7/usr/lib/maddash
        ln -fs /vagrant/vagrant-data/maddash-el7/usr/lib/maddash /usr/lib/maddash
        
        yum install -y epel-release
        yum install -y  http://software.internet2.edu/rpms/el7/x86_64/RPMS.main/perfSONAR-repo-0.8-1.noarch.rpm
        yum clean all
        yum install -y perfSONAR-repo-staging perfSONAR-repo-nightly
        yum clean all
        yum install -y gcc\
            kernel-devel\
            kernel-headers\
            dkms\
            make\
            bzip2\
            nagios-plugins-perfsonar\
            perl\
            perl-devel\
            java-1.8.0-openjdk\
            java-1.8.0-openjdk-devel\
            sed\
            maven\
            wget\
            git\
            perl-URI\
            httpd\
            mod_ssl\
            perl-CGI\
            perfsonar-graphs\
            net-tools
        
        # Create etc directory
        mkdir -p /etc/maddash/maddash-server
        if ! [ -e /etc/maddash/maddash-server/log4j.properties ]; then
            sed -e s,maddash-server.log,/var/log/maddash/maddash-server.log, -e s,maddash-server.netlogger.log,/var/log/maddash/maddash-server.netlogger.log, < /vagrant/maddash-server/etc/log4j.properties > /etc/maddash/maddash-server/log4j.properties
        fi
        if ! [ -e /etc/maddash/maddash-server/maddash.yaml ]; then
            cp /vagrant/maddash-server/etc/maddash.yaml /etc/maddash/maddash-server/maddash.yaml
        fi
        chown -R maddash:maddash /etc/maddash/maddash-server
        mkdir -p /etc/maddash/maddash-webui
        ln -fs /vagrant/maddash-webui/etc/config.json /etc/maddash/maddash-webui/config.json
        touch /etc/maddash/maddash-webui/admin-users
        chown apache:apache /etc/maddash/maddash-webui/admin-users
        # Not for production, apache change does not always stick so do 644
        chmod 644 /etc/maddash/maddash-webui/admin-users
        htpasswd -b /etc/maddash/maddash-webui/admin-users admin admin
        
        # Create /usr/lib directory
        mkdir -p /usr/lib/maddash/maddash-server/bin
        if ! [ -e /usr/lib/maddash/maddash-server/bin/maddash-server.env ]; then
            cp /vagrant/maddash-server/bin/maddash-server.env /usr/lib/maddash/maddash-server/bin/maddash-server.env
        fi
        ln -fs /vagrant/maddash-server/bin/run.sh /usr/lib/maddash/maddash-server/bin/run.sh
        ln -fs /vagrant/maddash-server/bin/startServer.sh /usr/lib/maddash/maddash-server/bin/startServer.sh
        ln -fs /vagrant/maddash-server/bin/update_db.sh /usr/lib/maddash/maddash-server/bin/update_db.sh
        ln -fs /vagrant/maddash-server/target /usr/lib/maddash/maddash-server/target
        ln -fs /vagrant/maddash-webui/web /usr/lib/maddash/maddash-webui
        
        # Create log directories
        mkdir -p /var/log/maddash/
        chown -R maddash:maddash /var/log/maddash/

        # Create data directory
        mkdir -p /var/lib/maddash
        chown -R maddash:maddash /var/lib/maddash/
        
        # Build dojo
        /vagrant/maddash-webui/scripts/build_dojo.sh /vagrant/maddash-webui/dojo-build
        
        # Setup apache
        cp /vagrant/maddash-webui/etc/apache-maddash.conf /etc/httpd/conf.d/apache-maddash.conf
        chown apache:apache /etc/httpd/conf.d/apache-maddash.conf
        systemctl enable httpd
        systemctl restart httpd
        
        # Setup unit files
        ln -fs /vagrant/maddash-server/scripts/maddash-server.service /usr/lib/systemd/system/maddash-server.service
        systemctl daemon-reload
        
        # Finally build maddash. Do this last so if build fails, then VM otherwise good to go
        # Note: Make sure you have checked out git submodules or build will fails
        cd /vagrant/maddash-server
        mvn clean install 
        if [ -L /vagrant/maddash-server/target/maddash-server.one-jar.jar ]; then
            rm -f /vagrant/maddash-server/target/maddash-server.one-jar.jar
        fi
        ln -fs /vagrant/maddash-server/target/maddash-server-*.one-jar.jar /vagrant/maddash-server/target/maddash-server.one-jar.jar        
    SHELL
  end
end
