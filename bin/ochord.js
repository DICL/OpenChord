sys = require('util')
exec = require('child_process').exec;

function print_help () {
  process.stdout.write ("                                                     \n \
      use `$ ochord [option] [arg1] [arg2]`                                   \n \
      Here are the availible options                                          \n \
      ==============================                                          \n \
        - create         Create OpenChord network                             \n \
        - join           Join OpenChord network                               \n \
        - insert         Insert the given data (arg1, arg2 required)          \n \
        - delete         Delete the entry of the given key (arg1 required)    \n \
        - retrieve       Retrieve the entry of the given key (arg1 required)  \n \
      ");
 process.exit(0);
}

var child;
var javaargv;
var javacmd = "java -cp .:../build/classes:../config:../lib/log4j.jar"
var hash_parser = {
  'create'   : "eclipse.Create",
  'join'     : "eclipse.Join",
  'insert'   : "eclipse.Insert #{ARGV[1]} #{ARGV[2]}",
  'delete'   : "eclipse.Delete #{ARGV[1]}",
  'retrieve' : "eclipse.Retrieve #{ARGV[1]}"
}

console.log (process.argv);
if ((javaargv = hash_parser.hasOwnProperty(process.argv[2]))) {
  print_help();
}

child = exec(javacmd + " " + javaargv);
