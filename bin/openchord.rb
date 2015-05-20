#!/usr/bin/env ruby
# vim: ft=ruby : fileencoding=utf-8 : foldmethod=marker
%w[ruby-progressbar optparse etc colored json awesome_print pry].each { |m| require m }

module OpenChord
  class ::String #{{{
    def warnout; warn self; end; 
  end #}}}

  class Launcher
    attr_accessor :debug

    # initialize {{{
    def initialize filepath: 
      # Create the dictionary to call the OpenChord routines
      # Overkill right? but it was supposed to have many elements at the beginning
      @@chordcmd = %i[Create Join].map do |k| 
      [k, "java -cp /home/vicente/OpenChord/dist/Create.jar:/home/vicente/OpenChord/lib/json.jar eclipse.#{k.to_s.capitalize}"]
      end.to_h
      @pidlist = {}
      @debug = false

      # Load configuration JSON file
      @nodelist = File.open(filepath) { |f| JSON.parse(f.read) }
      @universe = ["localhost"] | @nodelist['nodes']

    rescue => e
      (e.class.name == "Errno::ENOENT" ? "File not found, change filepath" : e.message).red.warnout
    end

    # }}} 
    # create {{{
    #
    def create
      make_pipe
      system "#{@@chordcmd[:Create]} #{@nodelist['master_address']} < ochord.pipe.in > ochord.pipe.out 2>output &"
      @pidlist['localhost'] = $?.pid + 1 # :WATCHOUT: Super buggy

      if @debug then
        pb = ProgressBar.create(:format => '%e %b>%i %p%% %t', 
                                :total => @nodelist['nodes'].length, 
                                :progress_mark => "+".red)
      end

      @nodelist['nodes'].each do |node|
        @pidlist[node] = `ssh #{node} '#{@@chordcmd[:Join]} #{@nodelist['master_address']} &> /dev/null & echo $!' `.chomp
        pb.increment if @debug
      end

      File.open('ochord.pid', 'w') { |f| f.write JSON.generate(@pidlist) }
    end

    # }}}
    # close {{{
    def close
      @pidlist = File.open('ochord.pid') { |f| JSON.parse(f.read) }  # Assert that we have a pidfile

      if @debug then
        pb = ProgressBar.create(:format => '%e %b>%i %p%% %t', 
                                :total => (@nodelist['nodes'].length + 1), 
                                :progress_mark => "+".red)
      end

      @universe.each do |node|                                  # Kill for each of the nodes
        `ssh #{node} kill #{@pidlist[node]}` 
        pb.increment if @debug
      end

      File.delete 'ochord.pid'
      File.delete 'ochord.pipe.in'
      File.delete 'ochord.pipe.out'
    rescue => e 
      warn "No previous instance of openchord found"
      abort
    end

    # }}}
    # hard_close {{{
    #
    def hardclose
      @universe.each do |node|                                    # Kill for each of the nodes
        `ssh #{node} pkill -u vicente java` 
      end

      File.delete 'ochord.pid'
      File.delete 'ochord.pipe.in'
      File.delete 'ochord.pipe.out'
    end

    # }}}
    # show {{{
    #
    def show 
      @universe.each do |node|                                    # Kill for each of the nodes
        `ssh #{node} pgrep -u vicente java`
        status = $?.exitstatus == 0 ? "Running" : "Stopped"
        puts "#{`ssh #{node} hostname`.chomp.green} : #{status.red}"
      end
    end

    # }}}
    # info {{{
    def info 
      ap File.open('ochord.pid') { |f| JSON.parse(f.read) } if File.exist? 'ochord.pid'
      ap @nodelist
    end

    ## }}}
    #  Insert {{{
    #  Many harcoded things :TODO:
    #
    def insert key:, value:
      fail "No instance of openchord runinng" unless File.exist? 'ochord.pid'

      package = {:command => 'insert', :key => key, :value => value}
      File.open('ochord.pipe.in', 'w') { |f| f.write JSON.generate(package) }
    end
    # }}}
    #  Retrieve {{{
    #  Many harcoded things :TODO:
    #
    def retrieve key: 
      fail "No instance of openchord runinng" unless File.exist? 'ochord.pid'
      package = {:command => 'retrieve', :key => key, :value => "dummy"}
      f = File.open('ochord.pipe.out', 'r+');
      File.open('ochord.pipe.in', 'w') { |f| f.write JSON.generate(package) }

      #Read the reply
      output = JSON.parse(f.gets)
      puts output['data']
    end
  end
    #}}}
    # Make pipe {{{ 
    def make_pipe
      `mkfifo ochord.pipe.in; mkfifo ochord.pipe.out`
    end
    #}}}

  class CLIcontroler < Launcher
    def initialize input:, filepath:  #{{{
      @options = {}
      super(filepath: filepath)

      OptionParser.new do |opts|
        opts.banner = <<EOF
openchord.rb is a script to create a OpenChord network
Usage: openchord.rb [-v/--verbose] [options]
EOF
        opts.version = 1.0
        opts.program_name = "\'Ruby openchord\' launcher"
        opts.separator "\nCore options"
        opts.on("-c", "--create [Address]", "Create new openchord network") { |i| @options[:address] = i; create }
        opts.on("-i key,value", "--insert key,value", Array, "insert new field") { |i,j| insert(key: i, value: j) }
        opts.on("-r", "--retrieve key", "Retrieve existing field") { |i| retrieve(key: i) }
        opts.on("-d", "--delete key"  , "delete a field") { |i| }
        opts.on("-k", "--close"       , "close network")  { close }
        opts.on("-K", "--hardclose"   , "no mercy close") { hardclose }
        opts.separator "\nDebug options"
        opts.on("-v", "--verbose"     , "Print out debug messages") { @debug = true } 
        opts.on(      "--config"      , "Reveal current setting") { info }
        opts.on(      "--show"        , "Check the status of the network") { show }
        opts.separator ""
        opts.on_tail("-h", "--help"   , "recursive this") { puts opts }
      end.parse! input
    end 
  end #}}}
end
