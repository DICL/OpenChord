/***************************************************************************
 *                                                                         *
 *                               Command.java                              *
 *                            -------------------                          *
 *   date                 : 16. Mai 2003, 18:30                            *
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
import java.io.PrintStream;

/**
 * @author sven
 * @version 1.0.5
 */
public abstract class Command {

	/**
	 * The instances to execute the commands on.
	 */
	protected Object[] toCommand;

	/**
	 * The parameters passed to this command are contained within this
	 * Hashtable.
	 */
	protected Map<String, String> parameters;

	/**
	 * Standard argument for displaying help of this command. If supplied all
	 * other parameters are ignored.
	 */
	protected final String HELP_ARG1 = "help";

	/**
	 * Alternative standard argument for displaying help of this command. If
	 * supplied all other parameters are ignored.
	 */
	protected final String HELP_ARG2 = "h";

	/**
	 * The PrintStream to print the command's output to.
	 */
	protected PrintStream out;

	/**
	 * Creates a new instance of Command.
	 * 
	 * @param out
	 *            The java.io.PrintStream to that the command's output is
	 *            printed.
	 * @param toCommand
	 *            The instance to execute the command on.
	 */
	public Command(Object[] toCommand, PrintStream out) {
		this.toCommand = new Object[toCommand.length]; 
		System.arraycopy(toCommand, 0, this.toCommand, 0, this.toCommand.length); 
		this.out = out;
		this.parameters = new HashMap<String, String>();
	}

	/**
	 * Set the <code>PrintStream</code>, to that this command prints its
	 * output.
	 * 
	 * @param out
	 *            The <code>PrintStream</code>.
	 */
	public void setPrintStream(PrintStream out) {
		this.out = out;
	}

	/**
	 * Add a command line parameter.
	 * 
	 * @param paramName
	 *            The parameters name. Cannot contain spaces.
	 * @param paramValue
	 *            The parameters value. Cannot contain spaces.
	 */
	public final void addParameter(String paramName, String paramValue) {
		this.parameters.put(paramName, paramValue);
	}

	/**
	 * Executes the command.
	 * 
	 * @throws ConsoleException
	 *             Exception during execution of command.
	 */
	public final void execute() throws ConsoleException {
		if (this.parameters.containsKey(this.HELP_ARG1)
				|| this.parameters.containsKey(this.HELP_ARG2)) {
			printOutHelp();
		} else {
			exec();
		}
	}

	/**
	 * To be overwritten by subclasses for command execution. The work of a
	 * command implementation is done in this method.
	 * 
	 * @throws ConsoleException
	 *             Exception during execution.
	 */
	public abstract void exec() throws ConsoleException;

	/**
	 * To be overwritten. Display the help text of the Command.
	 */
	public abstract void printOutHelp();

	/**
	 * Return the name of the command. Must not contain spaces. To be
	 * overwritten by subclasses.
	 * 
	 * @return The commands name. For example: <CODE>exit</CODE>.
	 */
	public abstract String getCommandName();

	/**
	 * Set the parameters for the Command.
	 * 
	 * @param parameters
	 *            Hashtable containing the parameter names as keys and the
	 *            parameter values as values. Both represented as Strings. Both
	 *            must not contain spaces.
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

}
