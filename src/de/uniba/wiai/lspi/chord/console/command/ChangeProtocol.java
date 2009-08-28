/***************************************************************************
 *                                                                         *
 *                             ChangeProtocol.java                                *
 *                            -------------------                          *
 *   date                 : 22.02.2008, 17:47:17                               *
 *   copyright            : (C) 2004-2008 Distributed and                  *
 *                              Mobile Systems Group                       *
 *                              Lehrstuhl fuer Praktische Informatik       *
 *                              Universitaet Bamberg                       *
 *                              http://www.uni-bamberg.de/pi/              *
 *   email                : {sven.kaffille}@uni-bamberg.de                 *
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

import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.util.console.Command;
import de.uniba.wiai.lspi.util.console.ConsoleException;

public class ChangeProtocol extends Command {

	private static final String RMI_PROTOCOL = URL.KNOWN_PROTOCOLS
			.get(URL.RMI_PROTOCOL);

	private static final String SOCKET_PROTOCOL = URL.KNOWN_PROTOCOLS
			.get(URL.SOCKET_PROTOCOL);

	public static final String COMMAND_NAME = "cprotocol";

	private static final String TYPE_PARAM = "t";

	private static final String STATUS_PARAM = "s";

	public ChangeProtocol(Object[] toCommand, PrintStream out) {
		super(toCommand, out);
	}

	@Override
	public void exec() throws ConsoleException {
		if (this.parameters.containsKey(TYPE_PARAM)) {
			String type = this.parameters.get(TYPE_PARAM);
			if (type != null && type.length() > 0) {
				if (type.equalsIgnoreCase(RMI_PROTOCOL)) {
					RemoteChordNetworkAccess.getUniqueInstance().protocolType = URL.RMI_PROTOCOL;
				} else if (type.equalsIgnoreCase(SOCKET_PROTOCOL)) {
					RemoteChordNetworkAccess.getUniqueInstance().protocolType = URL.SOCKET_PROTOCOL;
				}
			}
		}
		if (this.parameters.containsKey(STATUS_PARAM)) {
			int type = RemoteChordNetworkAccess.getUniqueInstance().protocolType;
			out.println("Current protocol: " + URL.KNOWN_PROTOCOLS.get(type));
		}
	}

	@Override
	public String getCommandName() {
		return COMMAND_NAME;
	}

	@Override
	public void printOutHelp() {
		out
				.println("Changes the protocol for remote chord networks used for this console.");
		out.println("Currently supported protocols: {ocsocket, ocrmi}");
	}

}
