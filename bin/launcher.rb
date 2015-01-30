#!/usr/bin/env ruby
#
#
#

javacmd = "java -cp .:../build/classes:../config:../lib/log4j.jar "

case ARGV[0]

when 'create'
  `#{javacmd} eclipse.Create`

when 'insert'
  `javacmd eclipse.Insert ARGV[1] ARGV[2]`

when 'delete'
  `javacmd eclipse.Delete ARGV[1]`

when 'retrieve'
  `javacmd eclipse.Retrieve ARGV[1]`

#when 'close'
#  `$java $classpath eclise.Close`

else
  puts 'You should give an argument'

end

