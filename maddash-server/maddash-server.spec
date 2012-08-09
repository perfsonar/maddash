%define package_name maddash-server 
%define mvn_project_list common-libs,%{package_name}
%define install_base /opt/maddash/%{package_name}
%define config_base /etc/maddash/%{package_name}
%define log_dir /var/log/maddash
%define run_dir /var/run/maddash
%define data_dir /var/lib/maddash/
%define relnum 4 

Name:           %{package_name}
Version:        1.0
Release:        %{relnum}
Summary:        MaDDash Scheduler and REST Server
License:        distributable, see LICENSE
Group:          Development/Libraries
URL:            http://code.google.com/p/esnet-perfsonar
Source0:        maddash-%{version}-%{relnum}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:  java-1.6.0-openjdk
BuildRequires:  java-1.6.0-openjdk-devel
BuildRequires:  sed 
BuildArch:      noarch
Requires:       java-1.6.0-openjdk
Requires:       chkconfig

%description
MaDDash is a framework for scheduling service checks and displaying results in a grid. 
This package provides a server that schedules the checks and publishes the results
via REST interface.

%pre
/usr/sbin/groupadd maddash 2> /dev/null || :
/usr/sbin/useradd -g maddash -r -s /sbin/nologin -c "MaDDash User" -d /tmp maddash 2> /dev/null || :

%prep
%setup -q -n maddash-%{version}-%{relnum}

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
mkdir -p %{buildroot}/etc/init.d

#Copy jar files and scripts
cp %{package_name}/target/*.jar %{buildroot}/%{install_base}/target/
install -m 755 %{package_name}/bin/* %{buildroot}/%{install_base}/bin/
install -m 744 %{package_name}/sql/* %{buildroot}/%{install_base}/sql/
install -m 755 %{package_name}/scripts/%{package_name} %{buildroot}/etc/init.d/%{package_name}

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

#Configure service to start when machine boots
/sbin/chkconfig --add %{package_name}

##Upgrade database
if [ "$1" = "2" ]; then
  %{install_base}/bin/update_db.sh -d %{data_dir} -f %{install_base}/sql/upgrade-1.0rc1-tables.sql
fi

%files
%defattr(-,maddash,maddash,-)
%config(noreplace) %{config_base}/*
%{install_base}/target/*
%{install_base}/bin/*
%{install_base}/sql/*
/etc/init.d/%{package_name}

%preun
if [ $1 -eq 0 ]; then
    /sbin/chkconfig --del %{package_name}
    /sbin/service %{package_name} stop
    unlink %{install_base}/target/%{package_name}.one-jar.jar
    unlink %{install_base}/target/%{package_name}.jar
fi
