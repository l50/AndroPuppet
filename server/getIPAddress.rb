#!/usr/bin/env ruby

#
# Used to get the ip address of a vagrant machine
#
# Jayson Grace < jayson.e.grace < at > gmail.com >
#
# Version 1.0
#
# Since 2015-04-24
#
machine = ''

ARGV.each do |a|
  machine = a
end

if (machine.empty?)
  puts 'No machine given'
else
  ipAddr = `vagrant ssh #{machine} -c 'ifconfig | grep -oP "inet addr:\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}" | grep -oP "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}" | tail -n 2 | head -n 1'`
  puts "IP ADDRESS: #{ipAddr}"
end