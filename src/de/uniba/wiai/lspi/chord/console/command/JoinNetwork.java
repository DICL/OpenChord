/***************************************************************************
 *                                                                         *
 *                             JoinNetwork.java                            *
 *                            -------------------                          *
 *   date                 : 09.09.2004, 11:16                              *
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

import de.uniba.wiai.lspi.util.console.Command;
import de.uniba.wiai.lspi.util.console.ConsoleException;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.Chord;

import java.net.MalformedURLException;
import java.util.Iterator;

/**
 * {@link Command} to join a remote chord network. </br>
 * {@link Chord#join(URL, URL)}.
 * 
 * <p>
 * To get a description of this command type <code>joinN -help</code> into the
 * {@link de.uniba.wiai.lspi.chord.console.Main console}.
 * </p>
 * 
 * @author sven
 * @version 1.0.5
 */
public class JoinNetwork extends Command {

	/**
	 * The name of this {@link Command}.
	 */
	public static final String COMMAND_NAME = "joinN";

	/**
	 * The name of the parameter that defines the bootstrap node to use.
	 */
	public static final String BOOTSTRAP_PARAM = "bootstrap";

	/**
	 * The port on which the local node should listen.
	 */
	public static final String PORT_PARAM = "port";

	/**
	 * Creates a new instance of CreateNodes
	 * 
	 * @param toCommand11
	 * @param out1
	 */
	public JoinNetwork(Object[] toCommand11, PrintStream out1) {
		super(toCommand11, out1);
	}

	public void exec() throws ConsoleException {

		int port = -1;
		if (this.parameters.containsKey(PORT_PARAM)) {
			try {
				port = Integer.parseInt(this.parameters.remove(PORT_PARAM));
			} catch (NumberFormatException e) {
				throw new ConsoleException("Port is no integer value! "
						+ e.getMessage());
			}
		}

		if (!this.parameters.containsKey(BOOTSTRAP_PARAM)) {
			this.out.println("Creating new chord overlay network!");
		}
		String bootStrap = this.parameters.remove(BOOTSTRAP_PARAM);

		if (this.parameters.size() > 0) {
			StringBuilder msg = new StringBuilder();
			msg.append("Too many parameters. Unknown parameters: ");
			Iterator<String> params = this.parameters.keySet().iterator();
			while (params.hasNext()) {
				msg.append(params.next());
				msg.append(" ");
			}
			throw new ConsoleException(msg.toString());
		}

		URL bootstrapURL = null;
		if (bootStrap != null) {
			try {
				bootstrapURL = new URL(
						URL.KNOWN_PROTOCOLS.get(RemoteChordNetworkAccess
								.getUniqueInstance().protocolType)
								+ "://" + bootStrap + "/");
			} catch (MalformedURLException e) {
				throw new ConsoleException("URL " + bootStrap + " provided by "
						+ BOOTSTRAP_PARAM + " parameter is malformed!", e);
			}
			this.out.println("Trying to join chord network with boostrap URL "
					+ bootstrapURL);
		}
		RemoteChordNetworkAccess remote = (RemoteChordNetworkAccess) this.toCommand[1];
		try {
			remote.join(bootstrapURL, port);
		} catch (Exception e) {
			e.printStackTrace(this.out);
			throw new ConsoleException("Join/Creation of network failed. "
					+ "Reason: " + e.getMessage(), e);
		}
		this.out.println("URL of created chord node "
				+ remote.getChordInstance().getURL() + ".");
	}

	public String getCommandName() {
		return COMMAND_NAME;
	}

	public void printOutHelp() {
		this.out.println("The " + COMMAND_NAME
				+ " command creates a chord node \n"
				+ "to which remote nodes can connect.");
		this.out.println("______________");
		this.out.println("Parameters: ");
		this.out
				.println("'"
						+ BOOTSTRAP_PARAM
						+ "' takes a part of an URL of a remote chord \n"
						+ "node, that is then used as bootstrap node. \n"
						+ "If no bootstrap node is provided a new chord network "
						+ "is created. \n The parameter must be in the form hostname:port");
	}

}
