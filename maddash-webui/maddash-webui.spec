%define package_name maddash-webui 
%define install_base /opt/maddash/%{package_name}
%define relnum 1

Name:           %{package_name}
Version:        1.0
Release:        %{relnum}
Summary:        MaDDash Web Interface 
License:        distributable, see LICENSE
Group:          Development/Libraries
URL:            http://code.google.com/p/esnet-perfsonar
Source0:        maddash-%{version}-%{relnum}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      noarch
Requires:       perl
Requires:       httpd 

%description
MaDDash is a framework for scheduling service checks and displaying results in a grid. 
This package provides a web interface to display check results.

%pre
/usr/sbin/groupadd maddash 2> /dev/null || :
/usr/sbin/useradd -g maddash -r -s /sbin/nologin -c "MaDDash User" -d /tmp maddash 2> /dev/null || :

%prep
%setup -q -n maddash-%{version}-%{relnum}

%clean
rm -rf %{buildroot}

%build
%{package_name}/scripts/build_dojo.sh %{package_name}/dojo-build

%install
#Clean out previous build
rm -rf %{buildroot}

#Create directory structure for build root
mkdir -p %{buildroot}/%{install_base}
mkdir -p %{buildroot}/etc/httpd/conf.d

#Copy jar files and scripts
install -m 755 %{package_name}/web/*.cgi %{buildroot}/%{install_base}/
install -m 644 %{package_name}/etc/apache-maddash.conf  %{buildroot}/etc/httpd/conf.d/
cp -r %{package_name}/web/lib %{buildroot}/%{install_base}/lib
cp -r %{package_name}/web/style %{buildroot}/%{install_base}/style
cp -r %{package_name}/web/etc %{buildroot}/%{install_base}/etc

%post

%files
%defattr(-,maddash,maddash,-)
%config /etc/httpd/conf.d/apache-maddash.conf
%config %{install_base}/etc/config.json
%{install_base}/*

%preun

