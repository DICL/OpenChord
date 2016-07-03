#!/usr/bin/env ruby
require_relative 'openchord.rb'
include OpenChord

raise 'ECLIPSE_PATH env variable is not defined' unless ENV["ECLIPSE_PATH"]
CLIcontroler.new input: ARGV, filepath: ENV['HOME'] + '/.eclipse.json'
