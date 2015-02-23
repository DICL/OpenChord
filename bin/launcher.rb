#!/usr/bin/env ruby
load 'openchord.rb'
include OpenChord

raise 'ECLIPSE_PATH env variable is not defined' unless ENV["ECLIPSE_PATH"]
Launcher.new  argv: ARGV, filepath: ENV["ECLIPSE_PATH"] + "/etc/eclipse.json"
