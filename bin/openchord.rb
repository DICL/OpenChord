#!/usr/bin/env ruby
# vim: ft=ruby : fileencoding=utf-8 : foldmethod=marker
%w[optparse colored json awesome_print pry].each { |m| require "#{m}" }

module OpenChord
  class ::String #{{{
    def warnout; warn self; end; 
  end #}}}

  class Launcher
    # initialize {{{
    #
    def initialize filepath: 
      # Create the dictionary to call the OpenChord routines
      # Overkill right? but it was supposed to have many elements at the beginning
      @@chordcmd = %i[create join].map do |k| 
        [k, "java -jar /home/vicente/OpenChord/dist/#{k.to_s.capitalize}.jar"]
      end.to_h
      @pidlist = {}

      # Load configuration JSON file
      File.open(filepath, 'rb') { |f| @nodelist = JSON.parse(f.read) }

    rescue => e
      ( { "Errno::ENOENT" => "File not found, change filepath",
          "NoMethodError" => "Wrong options passed to the program" 
      } [e.class.name] or e.message).red.warnout
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
  end #}}}
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

  class CLIcontroler < Launcher
    def initialize input:, filepath:  #{{{
      @options = {}
      super(filepath: filepath)

      OptionParser.new do |opts|
        opts.banner = "Usage: openchord.rb [options]"

        opts.on("-s", "--stat"        , "Reveal current setting") { |i| info }
        opts.on("-c", "--create [Address]", "Create new openchord network") { |i| @options[:address] = i; create }
        opts.on("-i key,value", "--insert key,value", Array, "insert new field") { |i| }
        opts.on("-r", "--retrieve key", "Retrieve existing field") { |i| }
        opts.on("-d", "--delete key"  , "delete a field") { |i| }
        opts.on("-k", "--close"       , "close network")  { |i| @options[:verbose] = i; close }
        opts.on("-K", "--hardclose"   , "no mercy close") { |i| hardclose }
        opts.on("-h", "--help"        , "recursive this") { |i| puts opts }
      end.parse! input
    end 
  end #}}}
end
