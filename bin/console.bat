@echo off
cls
java -cp .;..\build\classes;..\config;..\lib\log4j.jar de.uniba.wiai.lspi.chord.console.Main %*
