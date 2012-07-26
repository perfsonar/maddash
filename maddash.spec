%define install_base /opt/maddash
%define relnum 1 

Name:           maddash
Version:        1.0
Release:        %{relnum}
Summary:        MaDDash  
License:        distributable, see LICENSE
Group:          Development/Libraries
URL:            http://code.google.com/p/esnet-perfsonar
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      noarch
Requires:       maddash-server
Requires:       maddash-webui
Requires:       perl-perfSONAR_PS-Nagios

%description
MaDDash is a framework for scheduling service checks and displaying results in a grid. This package installs a default set of modules that can be used to perform basic maddash functions. 

%pre

%prep

%clean
rm -rf %{buildroot}

%build
mkdir -p %{buildroot}/%{install_base}/
echo "%{version}-%{release}" >  %{buildroot}/%{install_base}/VERSION

%install

%post

%files
%{install_base}/VERSION

