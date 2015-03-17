#AndroPuppet

![AndroPuppet Logo](https://github.com/l50/AndroPuppet/blob/master/images/AndroPuppet.png "AndroPuppet")

##Functionality
AndroPuppet allows you to quickly and efficiently deploy a miniature cloud using minimal hardware. AndroPuppet interfaces with a laptop or desktop running a Debian-based derivative, along with Puppet and Vagrant. The server should be able to support up to four virtual machines (depending on hardware restrictions on both the server and the vm). These virtual machines can run any variety of system templates which are selected using AndroPuppet. This can be incredibly useful for groups that need a consistent computing experience that is mobile and easily repeatable.

The following templates are currently available:

Malware Sandbox: https://github.com/l50/cuckooVagrantBox

Penetration Testing Box: Coming soon!

Puppet Development Box: Coming soon!

Rails Development Box: Coming soon!

##Hardware Requirements
AndroPuppet will need a laptop or desktop with the following hardware requirements:

Debian-based operating system

\>= 8 GB RAM

\>= 2 CPU cores (4 is more ideal)

Android device running >= Android 4.0

##Server Requirements
A server must have the latest version of Vagrant and VirtualBox installed. Additionally, it must also be listening on port 22 and have credentials that the individual using AndroPuppet can provide.

###Server Instructions
1. Download vagrant from here: http://www.vagrantup.com/downloads.html
2. To install on a debian-based platform, use dpkg -i
3. Download and install VirtualBox from here: https://www.virtualbox.org/wiki/Downloads
4. Ensure the ssh server is enabled and the user account that will be used to connect has permissions to ssh into the box.

##Core Functionality
AndroPuppet will provide an interface that will allow for a user to connect to a server on which they can deploy the infrastructure. If there is an established connection between AndroPuppet and the server, the user will be able to view existing virtual machines detected on the server as well as create new ones.

If there are four virtual machines already created, the user will be required to destroy at least one of them to create a new virtual machine. Assuming the user was able to successfully connect and there is room for a new machine, the user will be given the option to create a new machine by specifying a desired system template out of the list of available templates. A user can also specify a template by selecting the custom template button and then entering in the URL associated with a git repository. Once they have specified the template they want to use, they will be able to deploy the machine to the server. 

##Future Work

I would like to eventually build in functionality that would allow a user to specify services as well as an operating for a box, so that they can build a completely customized box. 

###Miscellaneous footnotes
Clone into repo without account: git clone git://github.com/SomeUser/SomeRepo.git

Resources
---

Puppet - https://github.com/puppetlabs/puppet

Vagrant - https://github.com/mitchellh/vagrant

License
----

Apache


