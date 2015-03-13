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
    #
    def initialize filepath: 
      # Create the dictionary to call the OpenChord routines
      # Overkill right? but it was supposed to have many elements at the beginning
      @@chordcmd = %i[create join].map do |k| 
        [k, "java -jar /home/vicente/OpenChord/dist/#{k.to_s.capitalize}.jar"]
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
      @pidlist['localhost'] = `#{@@chordcmd[:create]} #{@nodelist['master_address']} &> /dev/null & echo $!`.chomp  # Run master
      if @debug then
        pb = ProgressBar.create(:format => '%e %b>%i %p%% %t', 
                                :total => @nodelist['nodes'].length, 
                                :progress_mark => "+".red)
      end

      @nodelist['nodes'].each do |node|
        @pidlist[node] = `ssh #{node} '#{@@chordcmd[:join]} #{@nodelist['master_address']} &> /dev/null & echo $!' `.chomp
        pb.increment if @debug
      end

      File.open('ochord.pid', 'w') { |f| f.write JSON.generate(@pidlist) }
    end
    # close {{{
    #
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
    #  insert {{{
    #  Many harcoded things :TODO:
    #
    def insert key:, value:
      fail "No instance of openchord runinng" unless File.exist? 'ochord.pid'

      @pidlist = File.open('ochord.pid', 'r') { |f| JSON.parse(f.read) }
      `echo '#{key} #{value}' > /proc/#{@pidlist['master']}/fd/0`
      warn "Problem inserting" unless $?.exited?
    end
  end
    # }}}

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
        opts.on("-i key,value", "--insert key,value", Array, "insert new field") { |i| }
        opts.on("-r", "--retrieve key", "Retrieve existing field") { |i| }
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
__END__
=begin rdoc
Rationale
  + 
  +
=end
