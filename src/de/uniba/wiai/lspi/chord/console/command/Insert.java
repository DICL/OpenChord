/***************************************************************************
 *                                                                         *
 *                               Insert.java                               *
 *                            -------------------                          *
 *   date                 : 15.09.2004, 10:14                              *
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

import java.net.MalformedURLException;

import de.uniba.wiai.lspi.chord.com.local.ChordImplAccess;
import de.uniba.wiai.lspi.chord.com.local.Registry;
import de.uniba.wiai.lspi.chord.com.local.ThreadEndpoint;
import de.uniba.wiai.lspi.chord.console.command.entry.Key;
import de.uniba.wiai.lspi.chord.console.command.entry.Value;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.util.console.ConsoleException;

/**
 * Command to insert a value from a specified node into the local chord network.
 * 
 * To get a description of this command type <code>insert -help</code> into
 * the {@link de.uniba.wiai.lspi.chord.console.Main console}.
 * 
 * @author sven
 * @version 1.0.5
 */
public class Insert extends de.uniba.wiai.lspi.util.console.Command {

	/**
	 * Name of this command.
	 */
	public static final String COMMAND_NAME = "insert";

	/**
	 * The name of the parameter, that defines the name of the node, from that
	 * the value should be inserted.
	 */
	protected static final String NODE_PARAM = "node";

	/**
	 * The name of the parameter, that defines the key for the value to insert.
	 */
	protected static final String KEY_PARAM = "key";

	/**
	 * The name of the parameter, that defines the value, which is inserted into
	 * the distributed hash table.
	 */
	protected static final String VALUE_PARAM = "value";

	/**
	 * Creates a new instance of Insert
	 * 
	 * @param toCommand1
	 * @param out1
	 */
	public Insert(Object[] toCommand1, java.io.PrintStream out1) {
		super(toCommand1, out1);
	}

	public void exec() throws ConsoleException {
		String node = this.parameters.get(NODE_PARAM);
		String key = this.parameters.get(KEY_PARAM);
		String value = this.parameters.get(VALUE_PARAM);
		if ((node == null) || (node.length() == 0)) {
			throw new ConsoleException("Not enough parameters! " + NODE_PARAM
					+ " is missing.");
		}
		if ((key == null) || (key.length() == 0)) {
			throw new ConsoleException("Not enough parameters! " + KEY_PARAM
					+ " is missing.");
		}
		if ((value == null) || (value.length() == 0)) {
			throw new ConsoleException("Not enough parameters! " + VALUE_PARAM
					+ " is missing.");
		}
		URL url = null; 
		try {
			url = new URL(URL.KNOWN_PROTOCOLS.get(URL.LOCAL_PROTOCOL) + "://" + node + "/");
		} catch (MalformedURLException e1) {
			throw new ConsoleException(e1.getMessage());
		} 

		Key keyObject = new Key(key);
		Value valueObject = new Value(value);

		ThreadEndpoint ep = Registry.getRegistryInstance().lookup(url);
		if (ep == null) {
			this.out.println("Node '" + node + "' does not exist!");
			return;
		}
		try {
			ChordImplAccess.fetchChordImplOfNode(ep.getNode()).insert(
					keyObject, valueObject);
		} catch (Throwable t) {
			ConsoleException e = new ConsoleException(
					"Exception during execution of command. " + t.getMessage());
			e.setStackTrace(t.getStackTrace());
			throw e;
		}
		this.out.println("Value '" + value + "' with key '" + key
				+ "' inserted " + "successfully from node '" + node + "'.");
	}

	public String getCommandName() {
		return COMMAND_NAME;
	}

	public void printOutHelp() {
		this.out
				.println("This command inserts a value with a provided key into the chord network.");
		this.out
				.println("The key is inserted starting from the node provided as parameter.");
		this.out.println("Required parameters: ");
		this.out.println("\t" + NODE_PARAM
				+ ": The name of the node, from where the key is inserted.");
		this.out.println("\t" + KEY_PARAM + ": The key for the value.");
		this.out.println("\t" + VALUE_PARAM + ": The value to insert.");
		this.out.println();
	}

}
