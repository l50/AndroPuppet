#!/usr/bin/env bash
###########################################
# Install all dependencies for Malwarrior.
# Jayson Grace < jayson.e.grace@gmail.com >
###########################################

# Error out if any issues occur
set -e

# Declare folder/filenames
TARGET_DIR="/opt/apet"
ZIP_FILE="apet.zip"
APET="./install_files/apet.rb"
LIB="./install_files/lib/"
DB="./install_files/db/"
LOG="./install_files/logs/"
DATA="./install_files/data"

# Make sure we're running as Super User
function checkRoot {
if [ $(id -u) -ne 0 ]; then
	echo "You must run this as root, Exiting.";
	exit 1;
fi
}

# Install Cuckoo
function installCuckoo {
	installCuckooPackages
	installCuckcooPipPackages
	# Configure tcpdump
	setcap cap_net_raw,cap_net_admin=eip /usr/sbin/tcpdump
	# Configure vm settings
	adduser cuckoo
	usermod -G vboxusers cuckoo
	installSSDeep
	installMongo
}

# Install all required packages from apt
function installCuckooPackages {
	apt-get install python-sqlalchemy python-bson python-dpkt python-jinja2 python-magic python-pymongo python-gridfs python-libvirt python-bottle\
	python-pefile python-chardet python-pip virtualbox tcpdump -y
}

# Install all required packages from pip
function installCuckcooPipPackages {
	install jinja2 pymongo bottle pefile maec==4.0.1.0 django chardet cybox
}

# Install SSDeep
function installSSDeep {
	apt-get install ssdeep python-pyrex subversion libfuzzy-dev
	svn checkout http://pyssdeep.googlecode.com/svn/trunk/ pyssdeep
	pushd pyssdeep
	python setup.py build
	python setup.py install
	popd
}

# Install Mongo



checkRoot
installCuckoo
echo "Now installing dependencies, please wait..."