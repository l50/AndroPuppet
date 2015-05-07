#!/usr/bin/env bash

# Error out if any issues occur
set -e

# Make sure we're running as Super User
function checkPriv {
	if [ $(id -u) -ne 0 ]; then
		echo "You must run this with elevated privileges (sudo), Exiting."
		exit 1;
	fi
}

# Ensure this script is running on a supported platform
function checkOS {
	if python -mplatform | grep Ubuntu 2>/dev/null; then
		echo "This is a supported machine, continuing..."
	else
		echo "This is not a supported platform, hop on an Ubuntu machine and try again."
		exit 1;
	fi
}
# Check if the target directory exists, if not, create it
function prepareTargetDir {
	if [ ! -d "$TARGET_DIR" ]; then
		pushd /opt
		git clone git://github.com/l50/AndroPuppet.git
		popd
	else
		echo "AndroPuppet is already installed. You should be good to start building machines."
		exit
	fi
}

function manageVagrant {
	if hash vagrant 2>/dev/null; then
		echo "Vagrant is installed, and we are assuming it's running the lastest version. Continuing..."
    else
    	echo "Vagrant is not installed, installing..."
    	mkdir /opt/AndroPuppet/dependencies
    	pushd /opt/AndroPuppet/dependencies
    	wget https://dl.bintray.com/mitchellh/vagrant/vagrant_1.7.2_x86_64.deb
    	dpkg -i 
    	popd
    fi
}

function manageVirtualBox {
	if hash VBoxManage 2>/dev/null; then
		echo "VirtualBox is installed, and we are assuming it's running the lastest version. Continuing..."
	else
		echo "Vagrant is not installed, installing..."
    	sudo apt-get update
		sudo apt-get install virtualbox-4.3
    fi
}

# Setup the permissions for /opt/AndroPuppet for the current user
function setupPermissions {
	if [[ $SUDO_UID -ne 0 ]]; then
		chown -R "$SUDO_USER" "$TARGET_DIR"
		chgrp -R "$SUDO_GID" "$TARGET_DIR"
	fi
}


# Automatically switch to the develop branch
function switchBranch {
	pushd /opt/AndroPuppet
	# devlopment branch
	git pull origin develop
	git checkout develop
   	popd
}


TARGET_DIR="/opt/AndroPuppet"

checkPriv
#checkOS
prepareTargetDir

manageVagrant
manageVirtualBox
setupPermissions

# DEBUG for developer
switchBranch

echo "You should be good to go!"

