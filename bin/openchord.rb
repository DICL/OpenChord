#!/usr/bin/env ruby
# vim: ft=ruby : fileencoding=utf-8 : foldmethod=marker
%w[optparse colored json awesome_print pry].each { |m| require "#{m}" }

module OpenChord
  class ::String #{{{
    def warnout; warn self; end; 
  end #}}}
  class CLIcontroler < Launcher #{{{
    options = {}
    def initialize (input)
      OptionParser.new do |opts|
        opts.banner = "Usage: openchord.rb [options]"

        opts.on("-s", "--stat", "Run verbosely") do |i| 
          ochord.info 
        end 

        opts.on("-cAddress", "--create=Address", "Run verbosely") do |i| 
          options[:address] = i 
        end 

        opts.on("-jAddress", "--join=Address", "Run verbosely") do |i| 
          options[:address] = i 
        end 

        opts.on("-i", "--insert", "Run verbosely") do |i| 
        #  options[:verbose] = i 
        end 

        opts.on("-r", "--retrieve", "Run verbosely") do |i| 
         # options[:verbose] = i 
        end 

        opts.on("-d", "--delete", "Run verbosely") do |i| 
         # options[:verbose] = i 
        end 

        opts.on("-k", "--close", "Run verbosely") do |i| 
          options[:verbose] = i 
        end 

        opts.on("-K", "--hardclose", "Run verbosely") do |i| 
          options[:verbose] = i 
        end 
      end.parse! input
      p options
    end 
  end #}}}

  class Launcher
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
