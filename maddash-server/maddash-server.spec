%define package_name maddash-server
%define mvn_project_list common-libs,jsnow,%{package_name}
%define install_base /usr/lib/maddash/%{package_name}
%define config_base /etc/maddash/%{package_name}
%define log_dir /var/log/maddash
%define run_dir /var/run/maddash
%define data_dir /var/lib/maddash/
%define perfsonar_auto_version 4.4.5
%define perfsonar_auto_relnum 0.a1.0

Name:           %{package_name}
Version:        %{perfsonar_auto_version}
Release:        %{perfsonar_auto_relnum}%{?dist}
Summary:        MaDDash Scheduler and REST Server
License:        distributable, see LICENSE
Group:          Development/Libraries
URL:            http://www.perfsonar.net
Source0:        maddash-%{version}-%{perfsonar_auto_relnum}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:  java-1.8.0-openjdk
BuildRequires:  java-1.8.0-openjdk-devel
BuildRequires:  sed
BuildArch:      noarch
Requires:       java-1.8.0-openjdk
%if 0%{?el7}
BuildRequires: systemd
BuildRequires:  maven
%{?systemd_requires: %systemd_requires}
%else
#apache-maven is not in standard centos repos
#can be acquired from https://repos.fedorapeople.org/repos/dchen/apache-maven/
#install in mock with mock -r $target --install <RPM_URL>
BuildRequires:  apache-maven
Requires:       chkconfig
%endif

%description
MaDDash is a framework for scheduling service checks and displaying results in a grid.
This package provides a server that schedules the checks and publishes the results
via REST interface.

%pre
/usr/sbin/groupadd -r maddash 2> /dev/null || :
/usr/sbin/useradd -g maddash -r -s /sbin/nologin -c "MaDDash User" -d /tmp maddash 2> /dev/null || :

%prep
%setup -q -n maddash-%{version}-%{perfsonar_auto_relnum}

%clean
rm -rf %{buildroot}

%build
mvn -DskipTests --projects %{mvn_project_list} clean package

%install
#Clean out previous build
rm -rf %{buildroot}

#Run install target
mvn -DskipTests --projects %{mvn_project_list} install

#Create directory structure for build root
mkdir -p %{buildroot}/%{install_base}/target
mkdir -p %{buildroot}/%{install_base}/bin
mkdir -p %{buildroot}/%{install_base}/sql
mkdir -p %{buildroot}/%{config_base}
%if 0%{?el7}
mkdir -p %{buildroot}%{_unitdir}
%else
mkdir -p %{buildroot}/etc/init.d
%endif

#Copy jar files and scripts
cp %{package_name}/target/*.jar %{buildroot}/%{install_base}/target/
install -m 755 %{package_name}/bin/* %{buildroot}/%{install_base}/bin/
install -m 744 %{package_name}/sql/* %{buildroot}/%{install_base}/sql/
%if 0%{?el7}
install -m 644 %{package_name}/scripts/%{package_name}.service %{buildroot}%{_unitdir}/%{package_name}.service
%else
install -m 755 %{package_name}/scripts/%{package_name} %{buildroot}/etc/init.d/%{package_name}
%endif

# Copy default config file
cp %{package_name}/etc/maddash.yaml %{buildroot}/%{config_base}/maddash.yaml

#Update log locations
sed -e s,%{package_name}.log,%{log_dir}/%{package_name}.log, -e s,%{package_name}.netlogger.log,%{log_dir}/%{package_name}.netlogger.log, < %{package_name}/etc/log4j.properties > %{buildroot}/%{config_base}/log4j.properties

%post
#Create directory for PID files
mkdir -p %{run_dir}
chown maddash:maddash %{run_dir}

#Create directory for logs
mkdir -p %{log_dir}
chown maddash:maddash %{log_dir}

#Create database directory
mkdir -p %{data_dir}
chown maddash:maddash %{data_dir}

#Create symbolic links to latest version of jar files
##if update then delete old links
if [ "$1" = "2" ]; then
  unlink %{install_base}/target/%{package_name}.one-jar.jar
  unlink %{install_base}/target/%{package_name}.jar
fi
ln -s %{install_base}/target/%{package_name}-%{version}.one-jar.jar %{install_base}/target/%{package_name}.one-jar.jar
chown maddash:maddash %{install_base}/target/%{package_name}.one-jar.jar
ln -s %{install_base}/target/%{package_name}-%{version}.jar %{install_base}/target/%{package_name}.jar
chown maddash:maddash %{install_base}/target/%{package_name}.jar

#Correct paths on x86_64 hosts
if [ -d "/usr/lib64" ]; then
    sed -i "s:/usr/lib/nagios/plugins:/usr/lib64/nagios/plugins:g" %{config_base}/maddash.yaml
fi

#Configure service to start when machine boots
%if 0%{?el7}
%systemd_post %{package_name}.service
if [ "$1" = "1" ]; then
    #if new install, then enable
    systemctl enable %{package_name}.service
    systemctl start %{package_name}.service
fi
%else
/sbin/chkconfig --add %{package_name}
/sbin/chkconfig %{package_name} on
%endif

if [ "$1" = "2" ]; then
    ##Upgrade database
    # %{install_base}/bin/update_db.sh -d %{data_dir} -f %{install_base}/sql/upgrade-1.0rc1-tables.sql

    #Update old nagios check paths
    sed -i "s:/opt/perfsonar_ps/nagios/bin:/usr/lib/nagios/plugins:g" %{config_base}/maddash.yaml

    #fix graph URL
    sed -i "s:/serviceTest:/perfsonar-graphs:g" %{config_base}/maddash.yaml
    sed -i "s:graphWidget.cgi::g" %{config_base}/maddash.yaml

    #restart service
    %if 0%{?el7}
    %else
        #restart service on update
        /sbin/service %{package_name} restart
    %endif
fi




%files
%defattr(-,maddash,maddash,-)
%config(noreplace) %{config_base}/*
%config(noreplace) %{install_base}/bin/*.env
%{install_base}/target/*
%{install_base}/bin/*.sh
%{install_base}/sql/*
%if 0%{?el7}
%{_unitdir}/%{package_name}.service
%else
/etc/init.d/%{package_name}
%endif

%preun
if [ $1 -eq 0 ]; then
%if 0%{?el7}
%systemd_preun %{package_name}.service
%else
    /sbin/chkconfig --del %{package_name}
    /sbin/service %{package_name} stop
%endif
    unlink %{install_base}/target/%{package_name}.one-jar.jar
    unlink %{install_base}/target/%{package_name}.jar
fi

%postun
%if 0%{?el7}
%systemd_postun_with_restart %{package_name}.service
%endif
