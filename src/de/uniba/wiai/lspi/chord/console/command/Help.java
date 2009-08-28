/***************************************************************************
 *                                                                         *
 *                                Help.java                                *
 *                            -------------------                          *
 *   date                 : 10.09.2004, 12:00                              *
 *   copyright            : (C) 2004-2008 Distributed and                  *
 *                              Mobile Systems Group                       *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : sven.kaffille@uni-bamberg.de                   *
 *                          karsten.loesing@uni-bamberg.de                 *
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
 
package de.uniba.wiai.lspi.chord.console.command;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import de.uniba.wiai.lspi.util.console.Command;
import de.uniba.wiai.lspi.util.console.ConsoleThread;
//import de.uniba.wiai.lspi.util.logging.Logger;

/**
 * This command prints a list of available commands. 
 * Just type <code>help</code> 
 * into the {@link de.uniba.wiai.lspi.chord.console.Main console}.
 * 
 * @author sven
 * @version 1.0.5
 */
public class Help extends Command {

//	private static Logger logger = Logger.getLogger(Help.class.getName());

	/**
	 * The name of this {@link Command}. 
	 */
	public static final String COMMAND_NAME = "help";

	/** 
	 * Creates a new instance of Help 
	 * @param toCommand1 
	 * @param out1 
	 */
	public Help(Object[] toCommand1, PrintStream out1) {
		super(toCommand1, out1);
	}

	public void exec() {
		Object factory = ConsoleThread.getConsole().getCommandFactory();
		// out.println("Factory class " + factory.getClass());
		Field[] fields = factory.getClass().getDeclaredFields();
		// out.println("Number of factory fields " + fields.length);
		Field mapping = null;
		for (int i = 0; (i < fields.length) && (mapping == null); i++) {
			// out.println("Searching for commandMapping");
			if (fields[i].getType().equals(Map.class)) {
				this.out
						.println("For help with any command, type name of command plus '-h' or '-help'.");
				this.out
						.println("Parameters of commands are always passed to them in the format '-parametername parametervalue'.");
				this.out
						.println("Some parameters require no value, so only the parameter name has to be provided to the command.");
				this.out.println("Commands available from this console:");
				this.out.println("-----");
				mapping = fields[i];
				try {
					mapping.setAccessible(true); 
					
					Hashtable mappingValue = (Hashtable) mapping.get(factory);
					Enumeration cmds = mappingValue.keys();
					int count = 0;
					while (cmds.hasMoreElements()) {
						this.out.print(cmds.nextElement());
						count++;
						if (cmds.hasMoreElements()) {
							this.out.print(", ");
						}
						if ((count % 5) == 0) {
							this.out.println();
						}
					}
				} catch (IllegalAccessException e) {
					this.out.println("No access to commands.");
				}
				this.out.println();
				this.out.println("-----");
				this.out
						.println("Note: Commands and parameters are case sensitive.");
			}
		}

	}

	public String getCommandName() {
		return COMMAND_NAME;
	}

	public void printOutHelp() {
		this.out
				.println("Display a list of all commands available in this console.");
	}

}
