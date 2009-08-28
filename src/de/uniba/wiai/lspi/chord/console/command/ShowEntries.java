/***************************************************************************
 *                                                                         *
 *                             ShowEntries.java                            *
 *                            -------------------                          *
 *   date                 : 09.09.2004                                     *
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
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.util.console.Command;
import de.uniba.wiai.lspi.util.console.ConsoleException;

/**
 * <p>
 * {@link Command} to show the entries of a node of local chord network.
 * </p>
 * To get a description of this command type <code>entries -help</code> into
 * the {@link de.uniba.wiai.lspi.chord.console.Main console}.
 * 
 * @author sven
 * @version 1.0.5
 */
public class ShowEntries extends Command {

	/**
	 * The name of this command.
	 */
	public static final String COMMAND_NAME = "entries";

	/**
	 * The name of the parameter, that defines the name of the node.
	 */
	public static final String NODE_PARAM = "node";

	/** Creates a new instance of ShowFingerTable 
	 * @param toCommand1 
	 * @param out1 */
	public ShowEntries(Object[] toCommand1, java.io.PrintStream out1) {
		super(toCommand1, out1);
	}

	public void exec() throws ConsoleException {

		String nodeName = this.parameters.get(NODE_PARAM);
		Registry reg = (Registry) this.toCommand[0];
		if ((nodeName == null) || (nodeName.length() == 0)) {
			// print out all nodes' entries
			for (ThreadEndpoint ep : reg.lookupAll().values()) {
				this.out.print("Node " + ep.getURL().getHost() + ": ");
				this.out.println(ChordImplAccess.fetchChordImplOfNode(ep.getNode())
						.printEntries());
			}
		} else {
			URL url = null; 
			try {
				url = new URL(URL.KNOWN_PROTOCOLS.get(URL.LOCAL_PROTOCOL) + "://" + nodeName + "/");
			} catch (MalformedURLException e1) {
				throw new ConsoleException(e1.getMessage());
			} 
			
			this.out.println("Retrieving node " + nodeName);
			ThreadEndpoint ep = reg.lookup(url);
			if (ep != null) {
				this.out.println(ChordImplAccess.fetchChordImplOfNode(ep.getNode())
						.printEntries());
			} else {
				this.out.println("Could not find node with name " + nodeName);
			}
		}
	}

	public String getCommandName() {
		return COMMAND_NAME;
	}

	public void printOutHelp() {
		this.out
				.println("This command displays the entries of a node. The name \n"
						+ " of the node must be provided with help of parameter '"
						+ NODE_PARAM
						+ "'");
	}

}