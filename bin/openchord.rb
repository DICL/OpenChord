#!/usr/bin/env ruby
# vim: ft=ruby : fileencoding=utf-8 : foldmethod=marker
require 'colored'
require 'json'
require 'awesome_print'
require 'pry'

module OpenChord
  class ::String #{{{
    def warnout; warn self; end; 
  end #}}}
  
  class Launcher
    #{{{
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
  
    # }}} 
    # initialize {{{
    #
    def initialize argv:, filepath: 
      # Create the dictionary to call the OpenChord routines
      # Overkill right? but it was supposed to have many elements at the beginning
      @@chordcmd = %i[create join].map do |k| 
        [k, "java -jar /home/vicente/OpenChord/dist/#{k.to_s.capitalize}.jar"]
      end.to_h
      @pidlist = {}

      # Load configuration JSON file
      File.open(filepath, 'rb') { |f| @nodelist = JSON.parse(f.read) }
  
      # Run the commands
      fail "No commands specified" unless argv.any? 
      if argv.first == "insert"
        insert key: argv[1], value: argv[2]             # Case for inserting
      else              
        send argv.first                                 # Case for creating or join
      end
  
    rescue => e
      ( { "Errno::ENOENT" => "File not found, change filepath",
          "NoMethodError" => "Wrong options passed to the program" 
      } [e.class.name] or e.message).red.warnout
      abort HELP
    end

    # }}} 
    # info {{{
    def info 
      ap File.open('ochord.pid', 'r') { |f| JSON.parse(f.read) } if File.exist? 'ochord.pid'
      ap @nodelist
    end

    ## }}}
    #  insert {{{
    #  Many harcoded things :TODO:
    #
    def insert key:, value:
      fail "No instance of openchord runinng" unless File.exist? 'ochord.pid'

      @pidlist = File.open('ochord.pid', 'r') { |f| JSON.parse(f.read) }
      `echo '#{key} #{value}' > /proc/#{@pidlist['master']}/fd/0`
      warn "Problem inserting" unless $?.exited?
    end

    # }}}
    # close {{{
    #
    def close
      @pidlist= File.open('ochord.pid', 'r') { |f| JSON.parse(f.read) }  # Assert that we have a pidfile

      `kill #{@pidlist['master']}`                                       # Kill master
      @nodelist['nodes'].each do |node|                                  # Kill for each of the nodes
        `ssh #{node} kill #{@pidlist[node]}` 
      end

      File.delete 'ochord.pid'
    end
  
    # }}}
    # hard_close {{{
    #
    def hardclose
      `pkill -u vicente java`                                              # Kill master
      @nodelist['nodes'].each do |node|                                    # Kill for each of the nodes
        `ssh #{node} pkill -u vicente java` 
      end
    end

    # }}}
    # create {{{
    #
    def create
      @pidlist['master'] = `#{@@chordcmd[:create]} #{@nodelist['master_address']} &> /dev/null & echo $!`.chomp  # Run master

      @nodelist['nodes'].each do |node|
        @pidlist[node] = `ssh #{node} '#{@@chordcmd[:join]} #{@nodelist['master_address']} &> /dev/null & echo $!' `.chomp
      end

      File.open('ochord.pid', 'w') { |f| f.write JSON.generate(@pidlist) }
      ap @pidlist
    end
  end
end 
