#!/usr/bin/env ruby
# vim: ft=ruby : fileencoding=utf-8 : foldmethod=syntax : foldlevel=2 : foldnestmax=3
require 'colored'
require 'json'
require 'awesome_print'

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
      @@chordcmd = %i[create join insert delete retrieve].map do |k| 
        [k, "java -cp /home/vicente/OpenChord/:/home/vicente/OpenChord/build/classes:/home/vicente/OpenChord/config:/home/vicente/OpenChord/lib/log4j.jar eclipse.#{k.to_s.capitalize}"]
      end.to_h
  
      # Load configuration JSON file
      File.open(filepath, 'rb') { |f| @nodelist = JSON.parse(f.read) }
  
      # Run the commands
      fail unless argv.any? 
      send argv[0]
  
    rescue => e
      ( { "Errno::ENOENT" => "File not found, change filepath",
          "NoMethodError" => "Wrong options passed to the program",
          "RuntimeError"  => "Not given option"} [e.class.name] or "#{e.backtrace}").red.warnout
      abort HELP
    end

    def info; ap @nodelist; end
  
    def order (command)
      @nodelist['nodes'].each do |node|
         @pidlist[node] = `ssh #{node} #{command} &> /dev/null & echo $$`
      end
    end
  
    def close
      `ssh #{@nodelist['master_address']} #{@@chordcmd[:close]} &`  # Run master
      order(@@chordcmd[:close])
      puts "--------------Network Close-------------------"
    end
  
    def create
      @pidlist = `#{@@chordcmd[:create]} #{@nodelist['master_address']} &> /dev/null & echo $$`  # Run master
      order @@chordcmd[:join] + " " + @nodelist['master_address']

      File.open('.pidlist', 'w') { |f| f.write JSON.generate(@pidlist) }
      puts "--------------Network Created-----------------"
    end
  end
end 
