/***************************************************************************
 *                                                                         *
 *                               Remove.java                               *
 *                            -------------------                          *
 *   date                 : 15.09.2004, 10:15                              *
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
 * 
 * To get a description of this command type <code>remove -help</code> into
 * the {@link de.uniba.wiai.lspi.chord.console.Main console}.
 * 
 * @author sven
 * @version 1.0.5
 */
public class Remove extends de.uniba.wiai.lspi.util.console.Command {

	/**
	 * The name of this command.
	 */
	public static final String COMMAND_NAME = "remove";

	/**
	 * The name of the parameter, that defines the name of the node, from that
	 * the request to remove a key-value-pair from the dht, must be made.
	 */
	protected static final String NODE_PARAM = "node";

	/**
	 * The name of the parameter that defines the key of the value to remove.
	 */
	protected static final String KEY_PARAM = "key";

	/**
	 * The name of the parameter that defines the value to remove.
	 */
	protected static final String VALUE_PARAM = "value";

	/** Creates a new instance of Remove 
	 * @param toCommand1 
	 * @param out1 */
	public Remove(Object[] toCommand1, java.io.PrintStream out1) {
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
			ChordImplAccess.fetchChordImplOfNode(ep.getNode()).remove(keyObject,
					valueObject);
		} catch (Throwable t) {
			ConsoleException e = new ConsoleException(
					"Exception during execution of command. " + t.getMessage());
			e.setStackTrace(t.getStackTrace());
			throw e;
		}
		this.out.println("Value '" + value + "' with key '" + key
				+ "' removed " + "successfully from node '" + node + "'.");
	}

	public String getCommandName() {
		return COMMAND_NAME;
	}

	public void printOutHelp() {
		this.out
				.println("This command removes a value with a provided key from the chord network.");
		this.out
				.println("The key is removed starting from the node provided as parameter.");
		this.out.println("Required parameters: ");
		this.out.println("\t" + NODE_PARAM
				+ ": The name of the node, from where the key is removed.");
		this.out.println("\t" + KEY_PARAM + ": The key for the value.");
		this.out.println("\t" + VALUE_PARAM + ": The value to remove.");
		this.out.println();
	}

}
