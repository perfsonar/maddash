%define package_name maddash-webui
%define install_base /usr/lib/maddash/%{package_name}
%define config_base /etc/maddash/%{package_name}
%define upgrade_base /usr/lib/maddash/upgrades/%{package_name}
%define perfsonar_auto_version 4.4.6
%define perfsonar_auto_relnum 1

Name:           %{package_name}
Version:        %{perfsonar_auto_version}
Release:        %{perfsonar_auto_relnum}%{?dist}
Summary:        MaDDash Web Interface
License:        distributable, see LICENSE
Group:          Development/Libraries
URL:            http://www.perfsonar.net
Source0:        maddash-%{version}-%{perfsonar_auto_relnum}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      noarch
BuildRequires:  wget
BuildRequires:  java-1.8.0-openjdk
Requires:       perl
Requires:       perl-URI
Requires:       httpd
Requires:       mod_ssl

%description
MaDDash is a framework for scheduling service checks and displaying results in a grid.
This package provides a web interface to display check results.

%pre
/usr/sbin/groupadd -r maddash 2> /dev/null || :
/usr/sbin/useradd -g maddash -r -s /sbin/nologin -c "MaDDash User" -d /tmp maddash 2> /dev/null || :

#track previous version
rm -rf %{_localstatedir}/lib/rpm-state
mkdir -p %{_localstatedir}/lib/rpm-state
rpm -q --queryformat "%%{RPMTAG_VERSION} %%{RPMTAG_RELEASE} " %{name} > %{_localstatedir}/lib/rpm-state/previous_version || :


%prep
%setup -q -n maddash-%{version}-%{perfsonar_auto_relnum}

%clean
rm -rf %{buildroot}

%build
%{package_name}/scripts/build_dojo.sh %{package_name}/dojo-build

%install
#Clean out previous build
rm -rf %{buildroot}

#Create directory structure for build root
mkdir -p %{buildroot}/%{install_base}
mkdir -p %{buildroot}/%{config_base}
mkdir -p %{buildroot}/%{upgrade_base}
mkdir -p %{buildroot}/etc/httpd/conf.d

#Copy jar files and scripts
install -m 755 %{package_name}/web/*.cgi %{buildroot}/%{install_base}/
install -m 644 %{package_name}/etc/apache-maddash.conf  %{buildroot}/etc/httpd/conf.d/
install -m 644 %{package_name}/web/etc/* %{buildroot}/%{config_base}/
install -m 755 %{package_name}/scripts/upgrades/* %{buildroot}/%{upgrade_base}/
cp -r %{package_name}/web/admin %{buildroot}/%{install_base}/admin
cp -r %{package_name}/web/lib %{buildroot}/%{install_base}/lib
cp -r %{package_name}/web/style %{buildroot}/%{install_base}/style
cp -r %{package_name}/web/images %{buildroot}/%{install_base}/images

%post
if [ -f %{_localstatedir}/lib/rpm-state/previous_version ] ; then
    PREV_VERSION=`cat %{_localstatedir}/lib/rpm-state/previous_version`
    rm %{_localstatedir}/lib/rpm-state/previous_version
fi

mkdir -p %{install_base}/etc
#create empty directory for config files. apache user files can go here
touch %{config_base}/admin-users
chown apache:apache %{config_base}/admin-users
chmod 600 %{config_base}/admin-users

if [ "$1" = "2" ]; then

    #Replace pre-1.3 file
    if [ -e /opt/maddash/maddash-webui/etc/config.json ]; then
        mv %{config_base}/config.json %{config_base}/config.json.bak
        mv /opt/maddash/maddash-webui/etc/config.json %{config_base}/config.json
        mv -f /opt/maddash/maddash-webui/etc/config.json.rpmsave /opt/maddash/maddash-webui/etc/config.json.rpmsave.bak
    elif [ -e /opt/maddash/maddash-webui/etc/config.json.rpmsave ]; then
        mv %{config_base}/config.json %{config_base}/config.json.bak
        mv /opt/maddash/maddash-webui/etc/config.json.rpmsave %{config_base}/config.json
    fi

    #update apache config
    sed -i "s:/opt/maddash:/usr/lib/maddash:g" /etc/httpd/conf.d/apache-maddash.conf
    grep -q "FollowSymLinks" /etc/httpd/conf.d/apache-maddash.conf || sed -i "s:+ExecCGI:FollowSymLinks +ExecCGI:g" /etc/httpd/conf.d/apache-maddash.conf

    #run upgrade scripts
    for script in %{upgrade_base}/*; do
        $script ${PREV_VERSION}
    done
fi

#create symlink to config.json
if [ ! -e %{install_base}/etc/config.json ]; then
    ln -s %{config_base}/config.json %{install_base}/etc/config.json
fi

#httpd selinux settings
setsebool -P httpd_can_network_connect on

#enable httpd on fresh install
if [ "$1" = "1" ]; then
    systemctl enable httpd
fi
#restart apache so config changes are applied
systemctl restart httpd

%files
%defattr(-,maddash,maddash,-)
%config(noreplace) /etc/httpd/conf.d/apache-maddash.conf
%config(noreplace) %{config_base}/config.json
%{config_base}/config.example.json
%{install_base}/*
%{upgrade_base}/*

%preun
