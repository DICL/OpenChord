#!/usr/bin/env ruby
# vim: ft=ruby : fileencoding=utf-8 : foldmethod=marker
%w[ruby-progressbar optparse colored json awesome_print].each { |m| require m }

module OpenChord
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
      @config   = File.open(filepath) { |f| JSON.parse(f.read) }
      @nodes    = @config['nodes']
      @universe = ["localhost"] | @nodes
      @localdir = @config['localdir']
      @PipeIn   = @localdir + "/pipe.in"
      @PipeOut  = @localdir + "/pipe.out"
      @PIDFile  = @localdir + "/ochord.pid"

      #Assert that the required variables are correctly setted
      raise IOError unless File.stat(@localdir).writable? 

    rescue Errno::ENOENT
      abort "Configuration file at \"#{filepath}\" not found".red

    rescue IOError
      abort "The configuration file is not properly writen".red
    end

    # }}} 
    # create {{{
    #
    def create
      `mkfifo #{@PipeOut} #{@PipeIn}`

      system "nohup #{@@chordcmd[:Create]} #{@config['master_address']} <#{@PipeIn} 1>#{@PipeOut} 2>output &"
      @pidlist['localhost'] = $?.pid + 1 # :WATCHOUT: Super buggy

      if @debug then
        pb = ProgressBar.create(:format => '%e %b>%i %p%% %t', 
                                :total => @nodes.length, 
                                :progress_mark => "+".red)
      end

      @nodes.each do |node|
        @pidlist[node] = `ssh #{node} 'nohup #{@@chordcmd[:Join]} #{@config['master_address']} &> /dev/null & echo $!' `.chomp
        pb.increment if @debug
      end

      File.open(@PIDFile, 'w') { |f| f.write JSON.generate(@pidlist) }
    end

    # }}}
    # close {{{
    def close
      @pidlist = File.open(@PIDFile) { |f| JSON.parse(f.read) }  # Assert that we have a pidfile

      if @debug then
        pb = ProgressBar.create(:format => '%e %b>%i %p%% %t', 
                                :total => (@nodes.length + 1), 
                                :progress_mark => "+".red)
      end

      @universe.each do |node|                                  # Kill for each of the nodes
        `ssh #{node} kill #{@pidlist[node]}` 
        pb.increment if @debug
      end

      File.delete @PIDFile
      File.delete @PipeIn
      File.delete @PipeOut
    rescue
      warn "No previous instance of openchord found"
      abort
    end

    # }}}
    # hard_close {{{
    #
    def hardclose
      system "pkill -u vicente -f 'eclipse.Create'"
      @universe.each do |node|                                    # Kill for each of the nodes
        system "ssh #{node} 'pkill -u vicente -f \"eclipse.Join\"'"
      end

      File.delete @PIDFile if File.exist? @PIDFile
      File.delete @PipeIn if File.exist? @PipeIn
      File.delete @PipeOut if File.exist? @PipeOut
    end

    # }}}
    # Status {{{
    #
    def status
      @universe.each do |node|                                    # Kill for each of the nodes
        `ssh #{node} pgrep -u vicente java`
        status = $?.exitstatus == 0 ? "Running" : "Stopped"
        puts "#{`ssh #{node} hostname`.chomp.green} : #{status.red}"
      end
    end

    # }}}
    # info {{{
    def info 
      ap File.open(@PIDFile) { |f| JSON.parse(f.read) } if File.exist? @PIDFile
      ap @nodes
    end

    ## }}}
    #  Insert {{{
    #
    def insert key:, value:
      fail "No instance of openchord runinng" unless File.exist? @PIDFile

      package = {:command => 'insert', :key => key, :value => value}
      File.open(@PipeIn, 'w') { |f| f.write JSON.generate(package) }
    end
    # }}}
    #  Retrieve {{{
    #
    def retrieve key: 
      fail "No instance of openchord runinng" unless File.exist? @PIDFile
      package = {:command => 'retrieve', :key => key, :value => "dummy"}
      mutex = Mutex.new

      f = File.open(@PipeOut, File::RDONLY); #|File::NONBLOCK);

      sender = Thread.new {
        mutex.lock
        sleep 0.5
        File.open(@PipeIn, 'w') { |fa| fa.write JSON.generate(package) }
      }

      mutex.unlock if mutex.locked?
      attempts = 0
      begin
        attempts += 1
        output = JSON.parse(f.read_nonblock(1024))
        rescue Errno::EAGAIN 
          sleep 0.5
          exit if attempts > 10
          retry
      end
      sender.join
      puts output['data']
    end
    #}}}
    # Delete {{{
    #
    def delete key: 
      fail "No instance of openchord runinng" unless File.exist? @PIDFile
      package = {:command => 'delete', :key => key, :value => "dummy"}
      File.open(@PipeIn, 'w') { |f| f.write JSON.generate(package) }
    end
    #}}}
  end

  class CLIcontroler < Launcher
    def initialize input:, filepath:  #{{{
      @options = {}
      super(filepath: filepath)

      OptionParser.new do |opts|
        opts.banner = "openchord.rb is a script to create a OpenChord network\n" +
                      "Usage: openchord.rb [-v/--verbose] [Actions]"
        opts.version = 1.0
        opts.program_name = "\'Ruby openchord\' launcher"
        opts.separator ""
        opts.separator "Core Actions"
        opts.separator "    create [Address]    Create new openchord network"
        opts.separator "    insert KEY VALUE    Insert new field"
        opts.separator "    retrieve KEY        Retrieve existing field"
        opts.separator "    delete KEY          delete a field"
        opts.separator "    close               close network"
        opts.separator "    hardclose           no mercy close"
        opts.separator "    status              Check the status of the network"
        opts.separator "    config              Reveal current setting"
        opts.separator ""
        opts.separator "\nDebug options"
        opts.on_tail("-v", "--verbose", "Print out debug messages") { @debug = true } 
        opts.on_tail("-h", "--help"   , "recursive this") { puts opts; exit }
      end.parse! input

      case input.shift
      when 'create' then create
      when 'close' then  close
      when 'hardclose' then  hardclose
      when 'status' then status
      when 'insert' then    insert(key: input[0], value: input[1])
      when 'retrieve' then    retrieve(key: input[0])
      #when 'rm' then     delete input
      #when 'ls' then     list
      when 'config' then config
      else               raise "Not action given"
      end
    end 
  end #}}}
end
