#!/usr/bin/env ruby
# vim: ft=ruby : fileencoding=utf-8 : foldmethod=syntax : foldlevel=2 : foldnestmax=3
require 'colored'
require 'json'
require 'awesome_print'
require 'pry'

module OpenChord
  class ::String
    def warnout; warn self; end; 
  end
  
  class Launcher
    HELP = <<-EOF
use `ochord [option] [arg1] [arg2]`
Here are the available options
==============================
#{[ ["create".red    , "Create OpenChord network"]                            ,
    ["join".red      , "Join OpenChord network"]                              ,
    ["insert".red    , "Insert the given data (arg1, arg2 required)"]         ,
    ["delete".red    , "Delete the entry of the given key (arg1 required)"]   ,
    ["retrieve".red  , "Retrieve the entry of the given key (arg1 required)"] ,
    ["info".green    , "Print out useful variables"]                          ,
    ["quiet".green   , "do not printout anything"]                            ,
    ["help".green    , "this"] ]
  .map { |item| "%20.20s  %-60.60s\n" % [item[0], item[1]] }.join }
EOF
  
    def initialize argv:, filepath:
      Signal.trap("INT") { |signo| puts "Signal <#{Signal.signame(signo)}> caught, finishing..." }
  
      # Create the dictionary to call the OpenChord routines
      @@chordcmd = %i[create join].map do |k| 
        [k, "java -jar /home/vicente/OpenChord/dist/#{k.to_s.capitalize}.jar"]
      end.to_h
      @pidlist = {}

      # Load configuration JSON file
      File.open(filepath, 'rb') { |f| @nodelist = JSON.parse(f.read) }
  
      # Run the commands
      fail unless argv.any? 
      if argv.length > 1
        send argv.first, [argv[1], argv[2]]             # Case for inserting
      else              
        send argv.first                                 # Case for creating or join
      end
  
    rescue => e
      ( { "Errno::ENOENT" => "File not found, change filepath",
          "NoMethodError" => "Wrong options passed to the program",
          "RuntimeError"  => "Not given option"} [e.class.name] or "#{e.backtrace}").red.warnout
      binding.pry
      abort HELP
    end

    def info; ap @nodelist; end

    def info_pid
      ap File.open('.pidlist', 'r') { |f| JSON.parse(f.read) }
    end
  
    def insert (inp)
      key   = inp[0]
      value = inp[1]
      fail "No instance of openchord runinng" unless File.exist? '.pidlist'

      @pidlist = File.open('.pidlist', 'r') { |f| JSON.parse(f.read) }
      `echo '#{key} #{value}' > /proc/#{@pidlist['master']}/fd/0`
      warn "Problem inserting" unless $?.exited?
    end

    def order (command)
      @nodelist['nodes'].each do |node|
        @pidlist[node] = `ssh #{node} bash -c \' #{command} & echo $! \'`.chomp
      end
    end
  
    def close
      #`ssh #{@nodelist['master_address']} #{@@chordcmd[:close]} &`  # Run master
      @pidlist= File.open('.pidlist', 'r') { |f| JSON.parse(f.read) }
      `kill #{@pidlist['master']}`
      @nodelist['nodes'].each do |node|
        `ssh #{node} kill #{@pidlist[node]}` 
      end
      puts "--------------Network Close-------------------"
    end
  
    def create
      @pidlist['master'] = `#{@@chordcmd[:create]} #{@nodelist['master_address']} &> /dev/null & echo $!`.chomp  # Run master
      order @@chordcmd[:join] + " " + @nodelist['master_address']

      File.open('.pidlist', 'w') { |f| f.write JSON.generate(@pidlist) }
      puts "--------------Network Created-----------------"
      ap @pidlist
    end
  end
end 
