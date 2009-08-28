/***************************************************************************
 *                                                                         *
 *                            CommandFactory.java                          *
 *                            -------------------                          *
 *   date                 : 26. Mai 2003, 18:32                            *
 *   copyright            : (C) 2004 Distributed and Mobile Systems Group  *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : sven.kaffille@uni-bamberg.de                   *
 *                                                                         *
 *                                                                         *
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   A copy of the license can be found in the license.txt file supplied   *
 *   with this software or at: http://www.gnu.org/copyleft/gpl.html        *
 *                                                                         *
 ***************************************************************************/

package de.uniba.wiai.lspi.util.console;

import java.util.*;
import java.lang.reflect.*;
import java.io.PrintStream;

import de.uniba.wiai.lspi.util.console.parser.*;

/**
 * Factory responsible to create instances of {@link Command} given the commands
 * name. The factory must be supplied with a mapping from the commands' names to
 * the commands' classes, which are subclasses of {@link Command}. The mapping
 * is provided through a Hashtable containing keys (Strings) representing
 * command names and values (Strings) representing class names. <br/> <div> For
 * example: <br/> <CODE> MyClass toCommand = new MyClass(...); <br/> Hashtable
 * myMapping = new Hashtable(); <br/> myMapping.put("exit",
 * MyExitCommand.class.getName()); <br/> ... <br/> CommandFactory factory = new
 * CommandFactory(toCommand, myMapping); <br/> </CODE> <br/> </div> On creation
 * the factory is provided with one or more Objects, on which the commands will
 * be executed. Additionally the mapping from command names to command classes
 * implementing the command must be provided to the <code>CommandFactory</code>.
 * This factory is used by {@link ConsoleThread}.
 * 
 * @author sven
 * @version 1.0.5
 */
public class CommandFactory {

	/**
	 * The Objects given to the Commands to execute commands on.
	 */
	protected Object[] toCommand;

	/**
	 * The mapping from command name to command class name.
	 */
	protected Map<String, String> commandMapping;

	/**
	 * Map containing already instantiated commands. Key: name of command, Value: instance of   {@link Command}  .
	 */
	protected Map<String, Command> instanceMap;

	/**
	 * The PrintStream to that all {@link Command}s print their outputs.
	 */
	protected PrintStream out;

	/**
	 * Creates a new instance of CommandFactory.
	 * 
	 * @param out
	 *            The PrintStream, to that all {@link Command}s created by this
	 *            factory, print their output to.
	 * @param toCommand
	 *            The Objects to execute commands on.
	 * @param commandMapping
	 *            The mapping from command name to command class. This must not
	 *            be <code>null</code> or have a size of zero!
	 */
	public CommandFactory(Object[] toCommand, PrintStream out,
			Map<String, String> commandMapping) {
		this.toCommand = new Object[toCommand.length]; 
		System.arraycopy(toCommand, 0, this.toCommand, 0, this.toCommand.length);
		this.out = out;
		this.commandMapping = commandMapping;
		this.instanceMap = new HashMap<String, Command>(); 
	}

	/**
	 * Add the given {@link Command} <code>cmd</code> to this command factory.
	 * 
	 * @param name
	 *            The name of the command to add.
	 * @param cmdClass The class name of {@link Command} to add.
	 */
	public void addCommand(String name, String cmdClass) {
		this.commandMapping.put(name, cmdClass);
	}

	/**
	 * Get the PrintStream, to that all {@link Command}s created by this
	 * factory, print their output to.
	 * 
	 * @return The PrintStream.
	 */
	public PrintStream getPrintStream() {
		return this.out;
	}

	/**
	 * Creates the {@link Command} instance corresponding to the given command
	 * name. Therefore the given command line is parsed with help of
	 * {@link CommandParser}, the command name and parameters extracted from
	 * it, the correspondig {@link Command} created and the parameters passed to
	 * the command. Then the command is returned.
	 * 
	 * @param commandLine
	 *            The command line entered into the console (See
	 *            {@link ConsoleThread}).
	 * @throws ConsoleException
	 *             Any Exception during creation of command.
	 * @return The instance of the command corresponding to given commandLine.
	 */
	public Command createCommand(String commandLine) throws ConsoleException {
		if (commandLine == null) {
			throw new IllegalArgumentException("commandLine must not be null!");
		}
		Command com = null;
		try {
			String command = CommandParser.parse(commandLine);
			/*
			 * TODO: check if parser generated with javacc can be made compliant
			 * to Java 5.0
			 */
			Map<String, String> parameters = CommandParser
					.parseParams(commandLine);

			/*
			 * Test if there is already an instance of that command, 
			 * that can be reused. 
			 */
			if (this.instanceMap.containsKey(command)) {
				com = this.instanceMap.get(command); 
			} else {
				// try to load command
				String commandClass = this.commandMapping.get(command);
				if (commandClass == null || commandClass.length() == 0) {
					throw new ConsoleException("Unknown command: '" + command
							+ "'");
				}
				Class comClass = null;
				// try {
				try {
					comClass = Class.forName(commandClass);
				} catch (ClassNotFoundException e) {
					throw new ConsoleException("Unknown command: " + command);
				}
				Class[] argtypes = new Class[2];
				argtypes[0] = Object[].class;
				argtypes[1] = java.io.PrintStream.class;
				Constructor comClassCons = null;
				try {
					comClassCons = comClass.getDeclaredConstructor(argtypes);
				} catch (SecurityException e) {
					throw new ConsoleException("Unknown command: " + command);
				} catch (NoSuchMethodException e) {
					throw new ConsoleException("Unknown command: " + command);
				}
				Object[] arg = new Object[2];
				arg[0] = this.toCommand;
				arg[1] = this.out;
				try {
					com = (Command) comClassCons.newInstance(arg);
				} catch (IllegalArgumentException e) {
					throw new ConsoleException("Unknown command: " + command);
				} catch (InstantiationException e) {
					throw new ConsoleException("Unknown command: " + command);
				} catch (IllegalAccessException e) {
					throw new ConsoleException("Unknown command: " + command);
				} catch (InvocationTargetException e) {
					throw new ConsoleException("Unknown command: " + command);
				}
				this.instanceMap.put(command, com); 
			}
			com.setParameters(parameters);
			return com;
		} catch (ParseException e) {
			throw new ConsoleException(
					"Command misspelled? Could not parse command.");
		}
	}

}
