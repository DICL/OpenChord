/***************************************************************************
 *                                                                         *
 *                              ShowNodes.java                             *
 *                            -------------------                          *
 *   date                 : 09.09.2004, 18:12                              *
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
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import de.uniba.wiai.lspi.chord.com.local.Registry;
import de.uniba.wiai.lspi.chord.com.local.ThreadEndpoint;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.util.console.Command;

/**
 * <p>
 * {@link Command} to show all nodes present in local chord network. 
 * </p>
 * To get a description of this command type <code>showNodes -help</code> 
 * into the {@link de.uniba.wiai.lspi.chord.console.Main console}.
 * 
 * @author sven
 * @version 1.0.5
 */
public class ShowNodes extends Command {

	/**
	 * Name of this commmand. 
	 */
	public static final String COMMAND_NAME = "show";

	/**
	 * 
	 */
	public static final String COUNT_PARAM = "count";

	/** Creates a new instance of ShowNodes 
	 * @param toCommand1 
	 * @param out1 */
	public ShowNodes(Object[] toCommand1, PrintStream out1) {
		super(toCommand1, out1);
	}

	public void exec() {
		Registry reg = (Registry) this.toCommand[0];
		Map eps = reg.lookupAll();
		Map<ID, ThreadEndpoint> temp = new HashMap<ID, ThreadEndpoint>();

		if (this.parameters.containsKey(COUNT_PARAM)) {
			this.out.println("No. of nodes currently running " + eps.size());
			return;
		}

		if (eps.size() != 0) {
			Iterator valueIterator = eps.values().iterator();
			ID[] ids = new ID[eps.size()];
			int index = 0;
			while (valueIterator.hasNext()) {
				ThreadEndpoint ep = (ThreadEndpoint) valueIterator.next();
				ids[index] = ep.getNodeID();
				temp.put(ids[index], ep);
				index++;
			}
			Arrays.sort(ids);
			this.out
					.println("Node list in the order as nodes are located on chord ring: ");
			for (int i = 0; i < ids.length; i++) {
				ThreadEndpoint ep = temp.get(ids[i]);
				this.out.println("Node " + ep.getURL().getHost() + " with id " + ids[i]);
			}
		} else {
			this.out.println("No nodes running.");
		}
	}

	public String getCommandName() {
		return COMMAND_NAME;
	}

	public void printOutHelp() {
		this.out.println("The " + COMMAND_NAME
				+ " command prints out a list of all "
				+ "nodes currently present in this JVM.");
		this.out
				.println("The nodes are listed in the same order, in that they are arranged "
						+ "on the chord ring.");
		this.out.println("If you want to know the number of nodes currently "
				+ "running just provide '" + COUNT_PARAM + "' parameter.");
	}

}
